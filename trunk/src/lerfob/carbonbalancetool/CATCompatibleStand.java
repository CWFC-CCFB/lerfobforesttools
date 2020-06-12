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
import repicea.simulation.covariateproviders.plotlevel.AgeYrProvider;
import repicea.simulation.covariateproviders.plotlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.plotlevel.ManagementTypeProvider;
import repicea.simulation.covariateproviders.plotlevel.TreeStatusCollectionsProvider;

/**
 * This method ensures the stand is compatible with LERFoB-CAT. The interface DateYrProvider is
 * used to retrieve the current date at which the stand was measured. The AgeYrProvider interface is
 * used only in the case of a even-aged stand.
 * @author Mathieu Fortin - August 2013
 */
public interface CATCompatibleStand extends AreaHaProvider, 
											TreeStatusCollectionsProvider, 
											InterventionResultProvider,
											ManagementTypeProvider,
											ApplicationScaleProvider,
											DateYrProvider,
											AgeYrProvider {

	
	/**
	 * This method returns a CarbonToolCompatibleStand with all its trees
	 * set to cut. It is called only if canBeRunInInfiniteSequence returns true.
	 * Otherwise the method can return null.
	 * @return a CarbonToolCompatibleStand stand
	 */
	public CATCompatibleStand getHarvestedStand();

	
	/**
	 * This method returns the identification of the stand.
	 * @return a String
	 */
	public String getStandIdentification();
	
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
