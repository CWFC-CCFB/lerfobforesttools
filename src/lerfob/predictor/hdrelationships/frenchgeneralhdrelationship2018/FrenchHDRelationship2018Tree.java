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
package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;

/**
 * The HeightableTree interface ensures the compatibility with the French general HD relationship.
 * @author Mathieu Fortin - May 2014
 */
public interface FrenchHDRelationship2018Tree extends FrenchHDRelationshipTree, 
										HeightMProvider,
										DbhCmProvider {

	/**
	 * This method returns ln(dbh + 1) with the dbh in cm.
	 * @return a double
	 */
	public default double getLnDbhCmPlus1() {
		return Math.log(getDbhCm() + 1);
	}

	/**
	 * This method returns (ln(dbh + 1))^2 with the dbh in cm.
	 * @return a double
	 */
	public default double getSquaredLnDbhCmPlus1() {
		double lnDbhCmPlus1 = getLnDbhCmPlus1();
		return lnDbhCmPlus1 * lnDbhCmPlus1;
	}

	
	/**
	 * This method ensures the species compatibility with the hd relationship.
	 * @return a FrenchHdSpecies enum instance
	 */
	public FrenchHdSpecies getFrenchHDTreeSpecies();
	
}
