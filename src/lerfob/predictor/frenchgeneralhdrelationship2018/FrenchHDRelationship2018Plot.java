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
package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.util.Collection;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.standlevel.MeanQuadraticDiameterCmProvider;
import repicea.simulation.covariateproviders.standlevel.SlopeInclinationPercentProvider;
import repicea.simulation.hdrelationships.HDRelationshipStand;


/**
 * This interface ensures the compatibility between an instance representing a particular plot
 * and the French general HD relationship.
 * @author Mathieu Fortin - May 2014
 */
public interface FrenchHDRelationship2018Plot extends HDRelationshipStand,
														MeanQuadraticDiameterCmProvider, 
														InterventionResultProvider,
														SlopeInclinationPercentProvider,
														GeographicalCoordinatesProvider {
	
	
	/**
	 * This method returns the basal area per hectare minus the basal area of the tree.
	 * @param tree a FrenchHDRelationship2014Tree instance
	 * @return the basal area (m2/ha)
	 */
	public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2018Tree tree);
	
	
	/**
	 * This method returns the trees that are available in the stand for height prediction. This method
	 * is called when the blups of the random effects are initially estimated:
	 * @return A collection of FrenchHDRelationship2014Tree instances
	 */
	public Collection<FrenchHDRelationship2018Tree> getTreesForFrenchHDRelationship();
	
}
