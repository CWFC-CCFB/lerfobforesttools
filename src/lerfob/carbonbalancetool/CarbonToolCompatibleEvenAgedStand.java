/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for AgroParisTech/INRA UMR LERFoB,
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.carbonbalancetool;

import repicea.simulation.covariateproviders.standlevel.AgeYrProvider;


public interface CarbonToolCompatibleEvenAgedStand extends CarbonToolCompatibleStand, AgeYrProvider {

	/**
	 * This method returns a CarbonToolCompatibleStand with all its trees
	 * set to cut.
	 * @return a CarbonToolCompatibleStand stand
	 */
	public CarbonToolCompatibleStand getHarvestedStand();

}
