package org.tuubes.core

import TuubesServer._
import org.tuubes.core.network.NetworkSystem

object ShutdownHandler extends Runnable {
  override def run(): Unit = {
    logger.info("Unloading the plugins...")
    PluginLoader.unloadAll()

    logger.info("Stopping the NetworkSystem...")
    NetworkSystem.stop()

    logger.info(s"Tuubes $Version shuts down.")
  }
}
