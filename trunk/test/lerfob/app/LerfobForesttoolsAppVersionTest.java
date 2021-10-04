/*
 * This file is part of the lerfob-foresttools library
 *
 * Author Mathieu Fortin - Canadian Forest Service
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package lerfob.app;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.JarUtility;

public class LerfobForesttoolsAppVersionTest {

	@Test
	public void compileVersionAndRetrieve() {
		String build = LerfobForesttoolsAppVersion.getInstance().getBuild();
		System.out.println("Build is: " + build);
		String version = LerfobForesttoolsAppVersion.getInstance().getVersion();
		System.out.println("Version is: " + version);
		if (JarUtility.isEmbeddedInJar(LerfobForesttoolsAppVersion.class)) {
			try {
				Integer.parseInt(build);
			} catch (NumberFormatException e) {
				Assert.fail("The revision cannot be parsed to an integer!");
			}
			try {
				Double.parseDouble(version);
			} catch (NumberFormatException e) {
				Assert.fail("The version cannot be parsed to a double!");
			}
		} else {
			Assert.assertEquals("Unknown", build);
		}
	}

}
