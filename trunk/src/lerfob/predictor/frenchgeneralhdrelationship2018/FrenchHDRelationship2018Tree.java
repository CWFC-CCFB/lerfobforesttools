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
public interface FrenchHDRelationship2018Tree extends HDRelationshipTree, 
										HeightMProvider,
										DbhCmProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider {

	public static FrenchHd2018Species getFrenchHd2018SpeciesFromThisString(String speciesName) {
		speciesName = speciesName.trim().toUpperCase().replace(" ", "_");
		speciesName = speciesName.replace("'", "_");
		speciesName = speciesName.replace("-", "_");
		FrenchHd2018Species species = FrenchHd2018Species.valueOf(speciesName);
		return species;
	}
	
	
	public enum FrenchHd2018Species {
		ALISIER_BLANC(SpeciesType.BroadleavedSpecies, "Sorbus aria", 39),
		ALISIER_TORMINAL(SpeciesType.BroadleavedSpecies, "Sorbus torminalis", 41),
		ARBOUSIER(SpeciesType.BroadleavedSpecies, "Arbutus unedo", 7),
		AUBEPINE_MONOGYNE(SpeciesType.BroadleavedSpecies, "Crataegus monogyna", 12),
		AULNE_GLUTINEUX(SpeciesType.BroadleavedSpecies, "Alnus glutinosa", 6),
		BOULEAU_VERRUQUEUX(SpeciesType.BroadleavedSpecies, "Betula pendula", 8),
		CHARME(SpeciesType.BroadleavedSpecies, "Carpinus betulus", 9),
		CHATAIGNIER(SpeciesType.BroadleavedSpecies, "Castanea sativa", 10),
		CHENE_LIEGE(SpeciesType.BroadleavedSpecies, "Quercus suber", 35),
		CHENE_PEDONCULE(SpeciesType.BroadleavedSpecies, "Quercus robur", 33),
		CHENE_PUBESCENT(SpeciesType.BroadleavedSpecies, "Quercus pubescens", 31),
		CHENE_ROUGE(SpeciesType.BroadleavedSpecies, "Quercus rubra", 34),
		CHENE_SESSILE(SpeciesType.BroadleavedSpecies, "Quercus petraea", 30),
		CHENE_TAUZIN(SpeciesType.BroadleavedSpecies, "Quercus pyrenaica", 32),
		CHENE_VERT(SpeciesType.BroadleavedSpecies, "Quercus ilex", 29),
		DOUGLAS(SpeciesType.ConiferousSpecies, "Pseudotsuga menziesii", 28),
		EPICEA_COMMUN(SpeciesType.ConiferousSpecies, "Picea abies", 17),
		EPICEA_DE_SITKA(SpeciesType.ConiferousSpecies, "Picea sitchensis", 18),
		ERABLE_A_FEUILLES_D_OBIER(SpeciesType.BroadleavedSpecies, "Acer opalus", 4),
		ERABLE_CHAMPETRE(SpeciesType.BroadleavedSpecies, "Acer campestre", 2),
		ERABLE_DE_MONTPELLIER(SpeciesType.BroadleavedSpecies, "Acer monspessulanum", 3),
		ERABLE_SYCOMORE(SpeciesType.BroadleavedSpecies, "Acer pseudoplatanus", 5),
		FRENE_COMMUN(SpeciesType.BroadleavedSpecies, "Fraxinus excelsior", 14),
		HETRE(SpeciesType.BroadleavedSpecies, "Fagus sylvatica", 13),
		HOUX(SpeciesType.BroadleavedSpecies, "Ilex aquifolium", 15),
		MELEZE_D_EUROPE(SpeciesType.ConiferousSpecies, "Larix decidua", 16),
		MERISIER(SpeciesType.BroadleavedSpecies, "Prunus avium", 27),
		NOISETIER_COUDRIER(SpeciesType.BroadleavedSpecies, "Corylus avellana", 11),
		ORME_CHAMPETRE(SpeciesType.BroadleavedSpecies, "Ulmus minor", 44),
		PEUPLIER_CULTIVE(SpeciesType.BroadleavedSpecies, "Populus spp.", 25),
		PIN_A_CROCHETS(SpeciesType.ConiferousSpecies, "Pinus uncinata", 24),
		PIN_D_ALEP(SpeciesType.ConiferousSpecies, "Pinus halepensis", 19),
		PIN_LARICIO_DE_CORSE(SpeciesType.ConiferousSpecies, "Pinus nigra var. corsicana", 21),
		PIN_MARITIME(SpeciesType.ConiferousSpecies, "Pinus pinaster", 22),
		PIN_NOIR_D_AUTRICHE(SpeciesType.ConiferousSpecies, "Pinus nigra", 20),
		PIN_SYLVESTRE(SpeciesType.ConiferousSpecies, "Pinus sylvestris", 23),
		ROBINIER_FAUX_ACACIA(SpeciesType.BroadleavedSpecies, "Robinia pseudoacacia", 36),
		SAPIN_PECTINE(SpeciesType.ConiferousSpecies, "Abies alba", 1),
		SAULE_CENDRE(SpeciesType.BroadleavedSpecies, "Salix cinerea", 38),
		SAULE_MARSAULT(SpeciesType.BroadleavedSpecies, "Salix caprea", 37),
		SORBIER_DES_OISELEURS(SpeciesType.BroadleavedSpecies, "Sorbus aucuparia", 40),
		TILLEUL_A_GRANDES_FEUILLES(SpeciesType.BroadleavedSpecies, "Tilia platyphyllos", 43),
		TILLEUL_A_PETITES_FEUILLES(SpeciesType.BroadleavedSpecies, "Tilia cordata", 42),
		TREMBLE(SpeciesType.BroadleavedSpecies, "Populus tremula", 26),
;
		final String latinName;
		final int index;
		final SpeciesType speciesType;
		
		FrenchHd2018Species(SpeciesType speciesType, String latinName, int index) {
			this.speciesType = speciesType;
			this.latinName = latinName;
			this.index = index;
		}
		
		protected int getIndex() {return index;}

		protected SpeciesType getSpeciesType() {return speciesType;}
	}	
	
	
	/**
	 * This method ensures the species compatibility with the hd relationship.
	 * @return a FrenchHdSpecies enum instance
	 */
	public FrenchHd2018Species getFrenchHDTreeSpecies();
	
}
