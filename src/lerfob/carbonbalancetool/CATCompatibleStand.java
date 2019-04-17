/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for AgroParisTech/INRA UMR LERFoB,
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

import repicea.simulation.ApplicationScaleProvider;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.standlevel.ManagementTypeProvider;
import repicea.simulation.covariateproviders.standlevel.TreeStatusCollectionsProvider;

/**
 * This method ensures the stand is compatible with LERFoB-CAT
 * @author Mathieu Fortin - August 2013
 */
public interface CATCompatibleStand extends AreaHaProvider, 
											TreeStatusCollectionsProvider, 
											InterventionResultProvider,
											ManagementTypeProvider,
											ApplicationScaleProvider {

	
	/**
	 * This method returns a CarbonToolCompatibleStand with all its trees
	 * set to cut. It is called only if canBeRunInInfiniteSequence returns true.
	 * @return a CarbonToolCompatibleStand stand
	 */
	public default CATCompatibleStand getHarvestedStand() {return null;}

	
	/**
	 * This method returns the identification of the stand.
	 * @return a String
	 */
	public String getStandIdentification();
	
	
	/**
	 * This method returns the date at which the plot was measured in years.
	 * @return an integer
	 */
	public int getDateYr();


	/**
	 * This method returns true if the carbon balance can be calculated in infinite sequence. 
	 * This is possible when the management type is even-aged and the application scale is at the
	 * stand level.
	 * @return a boolean
	 */
	public default boolean canBeRunInInfiniteSequence() {
		return getManagementType() == ManagementType.EvenAged && getApplicationScale() == ApplicationScale.Stand;
	}
	
}
