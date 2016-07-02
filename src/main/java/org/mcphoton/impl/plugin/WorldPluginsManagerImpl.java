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
package org.mcphoton.impl.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mcphoton.impl.plugin.DependancyResolver.Solution;
import org.mcphoton.impl.server.Main;
import org.mcphoton.plugin.ClassSharer;
import org.mcphoton.plugin.Plugin;
import org.mcphoton.plugin.PluginLoader;
import org.mcphoton.plugin.PluginLoadingException;
import org.mcphoton.plugin.ServerPlugin;
import org.mcphoton.plugin.WorldPlugin;
import org.mcphoton.plugin.WorldPluginsManager;
import org.mcphoton.world.World;

/**
 * Implementation of WorldPluginsManager.
 *
 * @author TheElectronWill
 */
public final class WorldPluginsManagerImpl implements WorldPluginsManager {

	private final Map<String, Plugin> loadedPlugins = new ConcurrentHashMap<>();
	private final ClassSharer classSharer = new ClassSharerImpl();
	private volatile PluginLoader defaultPluginLoader;
	private final World world;

	public WorldPluginsManagerImpl(World world) {
		this.world = world;
		this.defaultPluginLoader = new JavaPluginLoader(classSharer);
	}

	@Override
	public Plugin getPlugin(String name) {
		return loadedPlugins.get(name);
	}

	@Override
	public boolean isPluginLoaded(String name) {
		return loadedPlugins.containsKey(name);
	}

	@Override
	public <T extends Plugin> T loadPlugin(File file, PluginLoader<T> loader) throws PluginLoadingException {
		T plugin = loader.loadPlugin(file);
		initialize(plugin, loader);
		plugin.onLoad();
		loadedPlugins.put(plugin.getName(), plugin);
		return plugin;
	}

	@Override
	public <T extends Plugin> List<T> loadPlugins(File[] files, PluginLoader<T> loader) {
		List<T> plugins = loader.loadPlugins(files);
		Map<String, T> namesMap = new HashMap<>();
		DependancyResolver dependancyResolver = new DependancyResolver();
		for (T p : plugins) {
			namesMap.put(p.getName(), p);
			dependancyResolver.add(p);
		}
		Solution solution = dependancyResolver.resolve();
		List<T> result = new ArrayList<>();
		for (String name : solution.resolvedOrder) {
			T plugin = namesMap.get(name);
			try {
				initialize(plugin, loader);
				plugin.onLoad();
				result.add(plugin);
				loadedPlugins.put(name, plugin);
			} catch (Throwable t) {// do not crash!
				t.printStackTrace();
			}
		}
		return result;
	}

	private void initialize(Plugin plugin, PluginLoader loader) {
		if (plugin instanceof WorldPlugin) {//WorldPlugin loaded in this world
			((WorldPlugin) plugin).init(loader, world);
		} else if (plugin instanceof ServerPlugin) {//ServerPlugin loaded in a single world
			((ServerPlugin) plugin).init(loader, Collections.singletonList(world));
		}
	}

	@Override
	public void registerPlugin(Plugin plugin) {
		loadedPlugins.put(plugin.getName(), plugin);
	}

	@Override
	public void unregisterPlugin(Plugin plugin) {
		loadedPlugins.remove(plugin.getName(), plugin);
	}

	@Override
	public void unloadPlugin(Plugin plugin) {
		try {
			plugin.onUnload();
		} finally {
			plugin.getLoader().unloadPlugin(plugin);
			loadedPlugins.remove(plugin.getName());
		}
	}

	@Override
	public void unloadPlugin(String name) {
		unloadPlugin(getPlugin(name));
	}

	public void unloadAllPlugins() {
		Iterator<Plugin> it = loadedPlugins.values().iterator();
		while (it.hasNext()) {
			Plugin next = it.next();
			try {
				unloadPlugin(next);
			} catch (Exception e) {
				Main.serverInstance.logger.error("Error while unloading plugin {}", e, next.getName());
			}
		}
	}

	@Override
	public PluginLoader getDefaultPluginLoader() {
		return defaultPluginLoader;
	}

	@Override
	public void setDefaultPluginLoader(PluginLoader loader) {
		this.defaultPluginLoader = loader;
	}

	@Override
	public ClassSharer getClassSharer() {
		return classSharer;
	}

}
