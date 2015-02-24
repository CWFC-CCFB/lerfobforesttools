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

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.SquaredLnDbhCmPlus1Provider;

/**
 * The HeightableTree interface ensures the compatibility with the French general HD relationship.
 * @author Mathieu Fortin - May 2014
 */
public interface FrenchHDRelationship2014Tree extends MonteCarloSimulationCompliantObject, 
										HeightMProvider,
										DbhCmProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider {

	public enum FrenchHdSpecies {
		ALISIER_BLANC,
		ALISIER_TORMINAL,
		AUBEPINE_MONOGYNE,
		AULNE_GLUTINEUX,
		BOULEAU_VERRUQUEUX,
		CHARME,
		CHATAIGNIER,
		CHENE_PEDONCULE,
		CHENE_PUBESCENT,
		CHENE_ROUGE,
		CHENE_SESSILE,
		CHENE_TAUZIN,
		CHENE_VERT,
		CHENE_LIEGE,
		DOUGLAS,
		EPICEA_COMMUN,
		EPICEA_DE_SITKA,
		ERABLE_OBIER,
		ERABLE_CHAMPETRE,
		ERABLE_SYCOMORE,
		FRENE_COMMUN,
		HETRE,
		HOUX,
		MELEZE_EUROPE,
		MERISIER,
		NOISETIER_COUDRIER,
		ORME_CHAMPETRE,
		PEUPLIER_CULTIVE,
		PIN_CROCHETS,
		PIN_ALEP,
		PIN_LARICIO,
		PIN_MARITIME,
		PIN_NOIR,
		PIN_SYLVESTRE,
		ROBINIER,
		SAPIN_PECTINE,
		SAULE_MARSAULT,
		SORBIER_OISELEURS,
		TILLEUL_GRANDES_FEUILLES,
		TILLEUL_PETITES_FEUILLES,
		TREMBLE;
	}	
	
	
	/**
	 * This method ensures the species compatibility with the hd relationship.
	 * @return a FrenchHdSpecies enum instance
	 */
	public FrenchHdSpecies getFrenchHDTreeSpecies();
	
	/**
	 * This method returns the year of the measurement or prediction.
	 * @return an Integer
	 */
	public int getYear();
	
	
}
