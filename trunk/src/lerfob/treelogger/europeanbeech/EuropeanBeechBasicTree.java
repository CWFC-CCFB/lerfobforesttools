/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.treelogger.europeanbeech;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmStandardDeviationProvider;
import repicea.simulation.treelogger.LoggableTree;

public interface EuropeanBeechBasicTree extends LoggableTree, DbhCmProvider, DbhCmStandardDeviationProvider {

}
