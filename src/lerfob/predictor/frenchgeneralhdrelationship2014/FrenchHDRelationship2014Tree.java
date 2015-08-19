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

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.SquaredLnDbhCmPlus1Provider;
import repicea.simulation.hdrelationships.HDRelationshipTree;

/**
 * The HeightableTree interface ensures the compatibility with the French general HD relationship.
 * @author Mathieu Fortin - May 2014
 */
public interface FrenchHDRelationship2014Tree extends HDRelationshipTree, 
										HeightMProvider,
										DbhCmProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider {

	public enum FrenchHdSpecies {
		ALISIER_BLANC(SpeciesType.BroadleavedSpecies),
		ALISIER_TORMINAL(SpeciesType.BroadleavedSpecies),
		AUBEPINE_MONOGYNE(SpeciesType.BroadleavedSpecies),
		AULNE_GLUTINEUX(SpeciesType.BroadleavedSpecies),
		BOULEAU_VERRUQUEUX(SpeciesType.BroadleavedSpecies),
		CHARME(SpeciesType.BroadleavedSpecies),
		CHATAIGNIER(SpeciesType.BroadleavedSpecies),
		CHENE_PEDONCULE(SpeciesType.BroadleavedSpecies),
		CHENE_PUBESCENT(SpeciesType.BroadleavedSpecies),
		CHENE_ROUGE(SpeciesType.BroadleavedSpecies),
		CHENE_SESSILE(SpeciesType.BroadleavedSpecies),
		CHENE_TAUZIN(SpeciesType.BroadleavedSpecies),
		CHENE_VERT(SpeciesType.BroadleavedSpecies),
		CHENE_LIEGE(SpeciesType.BroadleavedSpecies),
		DOUGLAS(SpeciesType.ConiferousSpecies),
		EPICEA_COMMUN(SpeciesType.ConiferousSpecies),
		EPICEA_DE_SITKA(SpeciesType.ConiferousSpecies),
		ERABLE_OBIER(SpeciesType.BroadleavedSpecies),
		ERABLE_CHAMPETRE(SpeciesType.BroadleavedSpecies),
		ERABLE_SYCOMORE(SpeciesType.BroadleavedSpecies),
		FRENE_COMMUN(SpeciesType.BroadleavedSpecies),
		HETRE(SpeciesType.BroadleavedSpecies),
		HOUX(SpeciesType.BroadleavedSpecies),
		MELEZE_EUROPE(SpeciesType.ConiferousSpecies),
		MERISIER(SpeciesType.BroadleavedSpecies),
		NOISETIER_COUDRIER(SpeciesType.BroadleavedSpecies),
		ORME_CHAMPETRE(SpeciesType.BroadleavedSpecies),
		PEUPLIER_CULTIVE(SpeciesType.BroadleavedSpecies),
		PIN_CROCHETS(SpeciesType.ConiferousSpecies),
		PIN_ALEP(SpeciesType.ConiferousSpecies),
		PIN_LARICIO(SpeciesType.ConiferousSpecies),
		PIN_MARITIME(SpeciesType.ConiferousSpecies),
		PIN_NOIR(SpeciesType.ConiferousSpecies),
		PIN_SYLVESTRE(SpeciesType.ConiferousSpecies),
		ROBINIER(SpeciesType.BroadleavedSpecies),
		SAPIN_PECTINE(SpeciesType.ConiferousSpecies),
		SAULE_MARSAULT(SpeciesType.BroadleavedSpecies),
		SORBIER_OISELEURS(SpeciesType.BroadleavedSpecies),
		TILLEUL_GRANDES_FEUILLES(SpeciesType.BroadleavedSpecies),
		TILLEUL_PETITES_FEUILLES(SpeciesType.BroadleavedSpecies),
		TREMBLE(SpeciesType.BroadleavedSpecies);
		
		SpeciesType type;
		
		FrenchHdSpecies(SpeciesType type) {
			this.type = type;
		}
		
		public SpeciesType getSpeciesType() {return type;}
		
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
