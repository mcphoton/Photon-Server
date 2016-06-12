/*
 * Copyright (c) 2016 MCPhoton <http://mcphoton.org> and contributors.
 *
 * This file is part of the Photon Server Implementation <https://github.com/mcphoton/Photon-Server>.
 *
 * The Photon Server Implementation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Photon Server Implementation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mcphoton;

import java.io.File;
import org.mcphoton.block.BlockRegistry;
import org.mcphoton.command.CommandsRegistry;
import org.mcphoton.entity.EntityRegistry;
import org.mcphoton.event.EventsManager;
import org.mcphoton.impl.block.PhotonBlockRegistry;
import org.mcphoton.impl.command.PhotonCommandsRegistry;
import org.mcphoton.impl.entity.PhotonEntityRegistry;
import org.mcphoton.impl.event.PhotonEventsManager;
import org.mcphoton.impl.item.PhotonItemRegistry;
import org.mcphoton.impl.plugin.JavaPluginLoader;
import org.mcphoton.impl.plugin.PhotonPluginsManager;
import org.mcphoton.impl.server.Main;
import org.mcphoton.impl.server.PhotonServer;
import org.mcphoton.impl.world.PhotonBiomeRegistry;
import org.mcphoton.item.ItemRegistry;
import org.mcphoton.network.PacketsManager;
import org.mcphoton.world.BiomeRegistry;

/**
 * Implementation of the Photon's class, which is the centralized API core.
 *
 * @author TheElectronWill
 */
public final class Photon {

	public static final File MAIN_DIR = new File(System.getProperty("user.dir")), PLUGINS_DIR = new File(MAIN_DIR, "plugins"), WORLDS_DIR = new File(MAIN_DIR, "worlds");
	private static final BlockRegistry BLOCK_REGISTRY = new PhotonBlockRegistry();
	private static final ItemRegistry ITEM_REGISTRY = new PhotonItemRegistry();
	private static final EntityRegistry ENTITY_REGISTRY = new PhotonEntityRegistry();
	private static final BiomeRegistry BIOME_REGISTRY = new PhotonBiomeRegistry();
	private static final CommandsRegistry CMD_REGISTRY = new PhotonCommandsRegistry();
	private static final EventsManager EVENTS_MANAGER = new PhotonEventsManager();
	private static final PhotonPluginsManager PLUGINS_MANAGER = new PhotonPluginsManager(new JavaPluginLoader());

	private Photon() {
	}

	public static PacketsManager getPacketsManager() {
		return Main.serverInstance.packetsManager;
	}

	public static PhotonPluginsManager getPluginsManager() {
		return PLUGINS_MANAGER;
	}

	public static EventsManager getEventsManager() {
		return EVENTS_MANAGER;
	}

	public static EntityRegistry getEntityRegistry() {
		return ENTITY_REGISTRY;
	}

	public static CommandsRegistry getCommandsRegistry() {
		return CMD_REGISTRY;
	}

	public static BlockRegistry getBlockRegistry() {
		return BLOCK_REGISTRY;
	}

	public static ItemRegistry getItemRegistry() {
		return ITEM_REGISTRY;
	}

	public static BiomeRegistry getBiomeRegistry() {
		return BIOME_REGISTRY;
	}

	public static boolean isClient() {
		return false;
	}

	public static boolean isServer() {
		return true;
	}

	public static String getVersion() {
		return "0.3.0-pre_alpha";
	}

	public static String getMinecraftVersion() {
		return "1.9.4";
	}

	public static File getMainDirectory() {
		return MAIN_DIR;
	}

	public static File getPluginsDirectory() {
		return PLUGINS_DIR;
	}
	
	public static PhotonServer getServer() {
		return Main.serverInstance;
	}

}
