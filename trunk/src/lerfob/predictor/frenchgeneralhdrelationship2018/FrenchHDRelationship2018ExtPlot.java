/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.frenchgeneralhdrelationship2018;

/**
 * The FrenchHDRelationship2018ExtStand interface is an extension of the FrenchHDRelationship2018Stand interface.
 * It ensures the stand instance can provide its mean seasonal temperature and precipitation.
 * @author Mathieu Fortin - December 2018
 */
public interface FrenchHDRelationship2018ExtPlot extends FrenchHDRelationship2018Plot {

	
	/**
	 * This method returns the mean temperature from March to September for the period 1961-1990.
	 * @return a value in Celsius degree (double)
	 */
	public double getMeanTemperatureOfGrowingSeason();

	/**
	 * This method returns the mean precipitation from March to September for the period 1961-1990.
	 * @return a value in mm (double)
	 */
	public double getMeanPrecipitationOfGrowingSeason();

}
