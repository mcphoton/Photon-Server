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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;
import org.mcphoton.plugin.ClassSharer;
import org.mcphoton.plugin.SharedClassLoader;

public final class PluginClassLoader extends URLClassLoader implements SharedClassLoader {

	private final ClassSharer sharer;
	private final AtomicInteger useCount = new AtomicInteger();

	public PluginClassLoader(URL[] urls, ClassSharer sharer) {
		super(urls);
		this.sharer = sharer;
	}

	public PluginClassLoader(URL url, ClassSharer sharer) {
		super(new URL[] {url});
		this.sharer = sharer;
	}

	@Override
	public int decreaseUseCount() {
		return useCount.getAndDecrement();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, true);
	}

	@Override
	public Class<?> findClass(String name, boolean checkShared) throws ClassNotFoundException {
		if (checkShared) {
			Class<?> c = sharer.getClass(name);
			if (c != null) {
				return c;
			}
		}
		return super.findClass(name);

	}

	@Override
	public ClassSharer getSharer() {
		return sharer;
	}

	@Override
	public int getUseCount() {
		return useCount.get();
	}

	@Override
	public int increaseUseCount() {
		return useCount.getAndIncrement();
	}

}
