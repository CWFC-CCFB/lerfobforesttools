/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.windstormdamagemodels.bockwindddamagemodel;

import repicea.simulation.covariateproviders.standlevel.DominantHeightMProvider;

/**
 * This interface ensures the compatibility of a Stand instance with 
 * Bock et al.'s wind damage model.
 * @author Mathieu Fortin - October 2010
 */
public interface BWDStand extends DominantHeightMProvider {

	/**
	 * This method returns true if there is a compact clay layer in the first 50 cm in depth.
	 * @return a boolean
	 */
	public boolean isCompactClayInFirst50cm();

	/**
	 * This method returns true if there are some rocks in the first 50 cm in depth.
	 * @return a boolean
	 */
	public boolean isRockInTheFirst50cm();

	/**
	 * This method returns true if the stand is located on flat land. 
	 * @return a boolean
	 */
	public boolean isFlatLand();
	
}
