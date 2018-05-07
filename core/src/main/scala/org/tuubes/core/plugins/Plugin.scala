package org.tuubes.core.plugins

import java.util.concurrent.CopyOnWriteArrayList

import better.files.File
import com.typesafe.scalalogging.Logger
import org.tuubes.core.TuubesServer
import org.tuubes.core.engine.World

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * A dynamically loaded plugin.
 *
 * @author TheElectronWill
 */
trait Plugin {
	val name: String
	val version: String
	val requiredDeps: Iterable[String] = Nil
	val optionalDeps: Iterable[String] = Nil
	final lazy val directory: File = TuubesServer.DirPlugins / name
	final lazy val logger = Logger(name)

	/**
	 * The plugin's state, which follows this cycle:
	 * LOADED -> onLoad() -> ENABLED -> ... -> onUnload() -> DISABLED
	 */
	@volatile private[plugins] final var state = PluginState.LOADED

	/** @return true if the plugin is enabled */
	def isEnabled: Boolean = (state == PluginState.ENABLED)

	private[plugins] final val worldsBuffer: mutable.Buffer[World] = new CopyOnWriteArrayList[World].asScala

	/** @return the worlds where the plugin is enabled */
	final def worlds: Iterable[World] = worldsBuffer

	/**
	 * Called just after the plugin is loaded, before it is enabled in the worlds.
	 */
	def onLoad(): Unit

	/**
	 * Called just before the plugin is unloaded, after it is disabled in the worlds.
	 */
	def onUnload(): Unit

	/**
	 * Called when the plugin is enabled in a world.
	 */
	def onEnable(world: World): Unit

	/**
	 * Called when the plugin is disabled in a world.
	 */
	def onDisable(world: World): Unit
}