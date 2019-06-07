/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2019 Mathieu Fortin for Canadian Forest Service, 
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
package lerfob.predictor.hdrelationships;

import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree.FrenchHdSpecies;
import repicea.simulation.hdrelationships.HDRelationshipStand;
import repicea.simulation.hdrelationships.HDRelationshipTree;
import repicea.simulation.hdrelationships.HeightPredictor;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;

public interface FrenchHeightPredictor<Stand extends HDRelationshipStand, Tree extends HDRelationshipTree> extends HeightPredictor<Stand, Tree> {

	/**
	 * Returns the species for the internal species-specific height predictor. 
	 * @return a FrenchHdSpecies instance
	 */
	public FrenchHdSpecies getSpecies();

	/**
	 * Returns the estimated blups for the plot.
	 * @param stand a HDRelationshipStand instance that stands for the plot.
	 * @return an Estimate instance
	 */
	public Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(Stand stand);

	
}
