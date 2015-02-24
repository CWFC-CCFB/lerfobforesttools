/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.frenchgeneralhdrelationship2014;

import java.util.Collection;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.MeanQuadraticDiameterCmProvider;


/**
 * This interface ensures the compatibility between an instance representing a particular stand
 * and the French general HD relationship.
 * @author Mathieu Fortin - May 2014
 */
public interface FrenchHDRelationship2014Stand extends MonteCarloSimulationCompliantObject,	MeanQuadraticDiameterCmProvider {
	
	/**
	 * This method returns a collection of trees that belong to the stand. Those trees do not have to implement
	 * the HeightableTree interface. The GeneralHeightPredictor already includes a method to filter the trees.
	 * @return a Collection instance
	 */
	@SuppressWarnings("rawtypes")
	public Collection getTrees();
	
	/**
	 * This method returns the basal area per hectare minus the basal area of the tree.
	 * @param tree a FrenchHDRelationship2014Tree instance
	 * @return the basal area (m2/ha)
	 */
	public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2014Tree tree);
	
	/**
	 * This method returns the slope of the plot in percentage.
	 * @return a double
	 */
	public double getSlopePercent();
}
