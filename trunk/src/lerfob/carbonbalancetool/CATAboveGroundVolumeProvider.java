/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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

/**
 * This interface ensures the instance can provide its above ground volume.
 * @author Mathieu Fortin - August 2013
 */
public interface CATAboveGroundVolumeProvider {

	/**
	 * This method returns the total above ground volume (m3) , INCLUDING bark and WITHOUT any expansion factor.
	 * @return a double
	 */
	public double getAboveGroundVolumeM3();

	/**
	 * If the predictor benefits from a stochastic implementation, then the sensitivity analysis is enabled.
	 * @return a boolean
	 */
	public default boolean isAboveGroundVolumePredictorStochastic() {return false;}

}
