package org.mcphoton.world;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.io.buffer.ByteBufferNetInput;
import com.github.steveice10.packetlib.io.stream.StreamNetOutput;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Reads and writes chunks (columns) asynchronously. There is one instance of ChunkIO per world.
 *
 * @author TheElectronWill
 */
public final class ChunkIO {
	private final World world;
	private final Path chunksDirectory;

	public ChunkIO(World world) {
		this.world = world;
		File dir = new File(world.dir().toJava(), "chunks");
		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		this.chunksDirectory = dir.toPath();
	}

	/**
	 * Gets the Path to the file containing the data of a particular chunk.
	 *
	 * @param x the chunk X coordinate
	 * @param z the chunk Z coordinate
	 * @return the Path of the chunk's file
	 */
	private Path getChunkPath(int x, int z) {
		return chunksDirectory.resolve(x + "_" + z + ".chunk");
	}

	/**
	 * Checks if a chunk is present on the disk.
	 *
	 * @param x the chunk X coordinate
	 * @param z the chunk Z coordinate
	 * @return {@code true} iff it is on the disk
	 */
	boolean isChunkOnDisk(int x, int z) {
		return Files.exists(getChunkPath(x, z));
	}

	/**
	 * Deletes the data of a chunk.
	 *
	 * @param x the chunk X coordinate
	 * @param z the chunk Z coordinate
	 * @throws IOException if an I/O error occurs.
	 */
	void deleteChunk(int x, int z) throws IOException {
		Files.deleteIfExists(getChunkPath(x, z));
	}

	/**
	 * Writes a chunk, asynchronously. This method returns (almost) immediately and executes the IO
	 * operations in the background. The completionHandler is notified when the operations are
	 * completed.
	 *
	 * @param chunk             the chunk to write
	 * @param attachment        an object that will be given to the completionHandler
	 * @param completionHandler handles the completion or failure of the operations
	 * @param <A>               the attachment's type
	 */
	<A> void writeChunk(ChunkColumnImpl chunk, A attachment,
						CompletionHandler<ChunkColumnImpl, A> completionHandler) {
		Path chunkPath = getChunkPath(chunk.getData().getX(), chunk.getData().getZ());
		try {
			AsynchronousFileChannel channel = AsynchronousFileChannel.open(chunkPath,
																		   StandardOpenOption.WRITE);
			// Writes the data to an extensible Stream
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(8192);
			NetOutput netOutput = new StreamNetOutput(byteStream);
			chunk.getData().save(netOutput);

			// Copies the data to a ByteBuffer
			ByteBuffer buffer = ByteBuffer.wrap(
					byteStream.toByteArray());//TODO avoid copying: use array directly
			// Creates a custom handler to hide IO details from the caller
			CompletionHandler<Integer, A> lowLevelhandler = new CompletionHandler<Integer, A>() {
				@Override
				public void completed(Integer result, A attachment) {
					completionHandler.completed(chunk, attachment);
					try {
						channel.close();
					} catch (IOException e) {
						//TODO how to handle this?
						e.printStackTrace();
					}
				}

				@Override
				public void failed(Throwable exc, A attachment) {
					completionHandler.failed(exc, attachment);
				}
			};
			// Asynchronously writes the ByteBuffer to the channel
			channel.write(buffer, 0, attachment, lowLevelhandler);
		} catch (IOException e) {
			completionHandler.failed(e, attachment);
		}
	}

	/**
	 * Reads a chunk, asynchronously. This method returns (almost) immediately and executes the IO
	 * operations in the background. The completionHandler is notified when the operations are
	 * completed.
	 *
	 * @param x                 the X chunk coordinate
	 * @param z                 the Z chunk coordinate
	 * @param attachment        an object that will be given to the completionHandler
	 * @param completionHandler handles the completion or failure of the operations
	 * @param <A>               the attachment's type
	 */
	<A> void readChunk(int x, int z, A attachment,
					   CompletionHandler<ChunkColumnImpl, A> completionHandler) {
		Path chunkPath = getChunkPath(x, z);
		if (!Files.exists(chunkPath)) {
			completionHandler.failed(new NoSuchFileException("The chunk file doesn't exist."),
									 attachment);
		}
		try {
			AsynchronousFileChannel channel = AsynchronousFileChannel.open(chunkPath,
																		   StandardOpenOption.READ);
			// Allocates a ByteBuffer of the size of the chunk file
			int fileSize = (int)channel.size();
			ByteBuffer buffer = ByteBuffer.allocate(fileSize);
			// Creates a custom handler to hide IO details from the caller and to parse the chunk
			CompletionHandler<Integer, A> lowLevelHandler = new CompletionHandler<Integer, A>() {
				@Override
				public void completed(Integer result, A attachment) {
					try {
						NetInput netInput = new ByteBufferNetInput(buffer);
						ChunkColumnImpl.Data data = ChunkColumnImpl.Data.read(netInput, x, z);
						ChunkColumnImpl chunk = new ChunkColumnImpl(world, data);
						completionHandler.completed(chunk, attachment);
					} catch (IOException ex) {
						completionHandler.failed(ex, attachment);
					}
				}

				@Override
				public void failed(Throwable exc, A attachment) {
					completionHandler.failed(exc, attachment);
				}
			};
			// Asynchronously reads the file into the ByteBuffer and parses it with the handler
			channel.read(buffer, 0, attachment, lowLevelHandler);
		} catch (IOException e) {
			completionHandler.failed(e, attachment);
		}
	}

	/**
	 * Reads a chunk, and wait for the read to complete.
	 *
	 * @param x the X chunk coordinate
	 * @param z the Z chunk coordinate
	 * @return the chunk that has been read
	 *
	 * @throws IOException if an IO error occurs
	 */
	ChunkColumnImpl readChunkNow(int x, int z) throws IOException {
		Path chunkPath = getChunkPath(x, z);
		if (!Files.exists(chunkPath)) {
			throw new NoSuchFileException("The chunk file doesn't exist.");
		}
		try (FileChannel channel = FileChannel.open(chunkPath, StandardOpenOption.READ)) {
			// Allocates a ByteBuffer of the size of the chunk file
			int fileSize = (int)channel.size();
			ByteBuffer buffer = ByteBuffer.allocate(fileSize);
			channel.read(buffer);
			NetInput netInput = new ByteBufferNetInput(buffer);
			ChunkColumnImpl.Data data = ChunkColumnImpl.Data.read(netInput, x, z);
			return new ChunkColumnImpl(world, data);
		}
	}
}