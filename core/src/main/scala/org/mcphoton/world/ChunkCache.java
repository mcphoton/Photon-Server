package org.mcphoton.world;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mcphoton.runtime.TaskSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory-sensitive cache that uses {@link SoftReference} to store the chunks of one world.
 *
 * @author TheElectronWill
 */
public final class ChunkCache {
	private static final Logger log = LoggerFactory.getLogger(ChunkCache.class);
	private final World world;

	public ChunkCache(World world) {
		this.world = world;
	}

	private final ConcurrentMap<ChunkCoordinates, CacheValue> chunksMap = new ConcurrentHashMap<>(512);
	private final ReferenceQueue<ChunkColumnImpl> collectedChunks = new ReferenceQueue<>();

	private static final class CacheValue extends SoftReference<ChunkColumnImpl> {
		private final ChunkCoordinates key;// Used to remove the value from the map when collected
		private final ChunkColumnImpl.Data libColumn;// Used to save the chunk's data when collected

		CacheValue(ChunkCoordinates key, ChunkColumnImpl value,
				   ReferenceQueue<? super ChunkColumnImpl> queue) {
			super(value, queue);
			this.key = key;
			this.libColumn = value.getData();
		}
	}

	private void cleanCollectedChunks() {
		for (CacheValue value; (value = (CacheValue)collectedChunks.poll()) != null; ) {
			/*
			If the chunk has been modified, put it back into the map, so that, if it's
			requested while the ChunkIO is saving it, we can get it immediately without waiting
			for the IO to terminates. That way we avoid some problems. In particular, a
			situation where ChunkCache.get() reads some outdated data before the chunk is saved
			cannot occur.

			If the chunk isn't needed anymore then it will be collected again, and since it
			won't have been modified it won't be written to the disk. Eventually it will be
			discarded by the GC.
			*/
			if (value.libColumn.hasChanged()) {
				ChunkColumnImpl chunkColumn = new ChunkColumnImpl(world, value.libColumn);
				ChunkCoordinates coords = value.key;
				CacheValue newValue = new CacheValue(coords, chunkColumn, collectedChunks);
				chunksMap.put(value.key, newValue);
				CompletionHandler<ChunkColumnImpl, ChunkCoordinates> completionHandler = new CompletionHandler<ChunkColumnImpl, ChunkCoordinates>() {
					@Override
					public void completed(ChunkColumnImpl result, ChunkCoordinates coords) {
						log.debug("Chunk saved: world {}, x={}, z={}", world, coords.x, coords.z);
						//Let the reference go if the chunk isn't used
					}

					@Override
					public void failed(Throwable exc, ChunkCoordinates coords) {
						log.error("Failed to save chunk in world {}, x={}, z={}", world.name(),
								  coords.x, coords.z, exc);
					}
				};
				world.chunkIO().writeChunk(chunkColumn, coords, completionHandler);
			}
		}
	}

	/**
	 * Returns a chunk column from the cache.
	 *
	 * @param x the chunk X coordinate
	 * @param z the chunk Z coordinate
	 * @return the chunk with the given coordinates, or {@code null} if it's not in the cache
	 */
	public ChunkColumnImpl getCached(int x, int z) {
		log.debug("In world {}: ChunkCache.getCached(x={}, z={})", world.name(), x, z);
		cleanCollectedChunks();// Processes the collected references
		ChunkCoordinates coords = new ChunkCoordinates(x, z);
		CacheValue ref = chunksMap.get(coords);
		return (ref == null) ? null : ref.get();
	}

	/**
	 * Synchronously gets a chunk column. If it isn't in the cache then it is either read or
	 * generated, and this methods blocks until the operation completes.
	 * <b>This method should be used only when necessary.</b> Prefer
	 * {@link #getCached(int, int)} or {@link #getAsync(int, int, Object, CompletionHandler)}
	 *
	 * @param x the chunk X coordinate
	 * @param z the chunk Z coordinate
	 * @return the chunk with the given coordinates, not null
	 */
	public ChunkColumnImpl getSync(int x, int z) {
		log.debug("In world {}: ChunkCache.getSync(x={}, z={})", world.name(), x, z);
		cleanCollectedChunks();// Processes the collected references
		ChunkCoordinates coords = new ChunkCoordinates(x, z);
		CacheValue ref = chunksMap.get(coords);
		ChunkColumnImpl chunk;
		if (ref != null && (chunk = ref.get()) != null) {// Chunk in cache
			return chunk;
		} else {// Chunk not in cache
			ChunkIO chunkIO = world.chunkIO();
			if (chunkIO.isChunkOnDisk(x, z)) {// Chunk on disk
				try {
					return chunkIO.readChunkNow(x, z);//sync read
				} catch (IOException e) {
					throw new RuntimeException("Failed to read chunk at " + coords);
				}
			} else {// Chunk needs to be generated
				ChunkColumnImpl generatedChunk = (ChunkColumnImpl)world.chunkGenerator().generate(x, z);
				chunksMap.put(coords, new CacheValue(coords, generatedChunk, collectedChunks));
				return generatedChunk;
			}
		}
	}

	/**
	 * Asynchronously gets a chunk column. The completionHandler is called when the chunk is
	 * available or when there is an error.
	 * <p>
	 * If the chunk exists in the cache, the completionHandler is called immediately with
	 * that chunk. If the chunk doesn't exist in the cache, then it is either read from the
	 * disk or generated with the world's ChunkGenerator, and the completionHandler is
	 * notified later, <b>from another thread</b>.
	 *
	 * @param x                 the chunk X coordinate
	 * @param z                 the chunk Z coordinate
	 * @param attachment        an object to give to the completionHandler
	 * @param completionHandler the handler that will be notified of the success or failure of
	 *                          the operation
	 */
	public <A> void getAsync(int x, int z, A attachment,
							 CompletionHandler<ChunkColumnImpl, A> completionHandler) {
		log.debug("In world {}: ChunkCache.getAsync(x={}, z={}, attachment={})", world.name(), x,
				  z, attachment);
		cleanCollectedChunks();// Processes the collected references
		ChunkCoordinates coords = new ChunkCoordinates(x, z);
		CacheValue ref = chunksMap.get(coords);
		ChunkColumnImpl chunk;
		if (ref != null && (chunk = ref.get()) != null) {// Chunk in cache
			completionHandler.completed(chunk, attachment);
		} else {// Chunk not in cache
			ChunkIO chunkIO = world.chunkIO();
			if (chunkIO.isChunkOnDisk(x, z)) {// Chunk on disk
				chunkIO.readChunk(x, z, attachment, completionHandler);//async read
			} else {// Chunk needs to be generated
				Runnable task = () -> {
					ChunkColumnImpl generatedChunk = (ChunkColumnImpl)world.chunkGenerator().generate(x, z);
					chunksMap.put(coords, new CacheValue(coords, generatedChunk, collectedChunks));
					completionHandler.completed(generatedChunk, attachment);
				};
				TaskSystem.execute(task);//async generation
			}
		}
	}
}