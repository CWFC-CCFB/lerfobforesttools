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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.hdrelationships.HDRelationshipTree;

public interface FrenchHDRelationshipTree extends HDRelationshipTree {

	public enum FrenchHdSpecies implements SpeciesTypeProvider {
		ALISIER_BLANC(SpeciesType.BroadleavedSpecies, "Sorbus aria", 39, true, 1),
		ALISIER_TORMINAL(SpeciesType.BroadleavedSpecies, "Sorbus torminalis", 41, true, 2),
		ARBOUSIER(SpeciesType.BroadleavedSpecies, "Arbutus unedo", 7, false, -1),
		AUBEPINE_MONOGYNE(SpeciesType.BroadleavedSpecies, "Crataegus monogyna", 12, true, 3),
		AULNE_GLUTINEUX(SpeciesType.BroadleavedSpecies, "Alnus glutinosa", 6, true, 4),
		BOULEAU_VERRUQUEUX(SpeciesType.BroadleavedSpecies, "Betula pendula", 8, true, 5),
		CHARME(SpeciesType.BroadleavedSpecies, "Carpinus betulus", 9, true, 6),
		CHATAIGNIER(SpeciesType.BroadleavedSpecies, "Castanea sativa", 10, true, 7),
		CHENE_LIEGE(SpeciesType.BroadleavedSpecies, "Quercus suber", 35, true, 14),
		CHENE_PEDONCULE(SpeciesType.BroadleavedSpecies, "Quercus robur", 33, true, 8),
		CHENE_PUBESCENT(SpeciesType.BroadleavedSpecies, "Quercus pubescens", 31, true, 9),
		CHENE_ROUGE(SpeciesType.BroadleavedSpecies, "Quercus rubra", 34, true, 10),
		CHENE_SESSILE(SpeciesType.BroadleavedSpecies, "Quercus petraea", 30, true, 11),
		CHENE_TAUZIN(SpeciesType.BroadleavedSpecies, "Quercus pyrenaica", 32, true, 12),
		CHENE_VERT(SpeciesType.BroadleavedSpecies, "Quercus ilex", 29, true, 13),
		DOUGLAS(SpeciesType.ConiferousSpecies, "Pseudotsuga menziesii", 28, true, 15),
		EPICEA_COMMUN(SpeciesType.ConiferousSpecies, "Picea abies", 17, true, 16),
		EPICEA_DE_SITKA(SpeciesType.ConiferousSpecies, "Picea sitchensis", 18, true, 17),
		ERABLE_A_FEUILLES_D_OBIER(SpeciesType.BroadleavedSpecies, "Acer opalus", 4, true, 18), 	
		ERABLE_CHAMPETRE(SpeciesType.BroadleavedSpecies, "Acer campestre", 2, true, 19),
		ERABLE_DE_MONTPELLIER(SpeciesType.BroadleavedSpecies, "Acer monspessulanum", 3, false, -1),
		ERABLE_SYCOMORE(SpeciesType.BroadleavedSpecies, "Acer pseudoplatanus", 5, true, 20),
		FRENE_COMMUN(SpeciesType.BroadleavedSpecies, "Fraxinus excelsior", 14, true, 21),
		HETRE(SpeciesType.BroadleavedSpecies, "Fagus sylvatica", 13, true, 22),
		HOUX(SpeciesType.BroadleavedSpecies, "Ilex aquifolium", 15, true, 23),
		MELEZE_D_EUROPE(SpeciesType.ConiferousSpecies, "Larix decidua", 16, true, 24),
		MERISIER(SpeciesType.BroadleavedSpecies, "Prunus avium", 27, true, 25),
		NOISETIER_COUDRIER(SpeciesType.BroadleavedSpecies, "Corylus avellana", 11, true, 26),
		ORME_CHAMPETRE(SpeciesType.BroadleavedSpecies, "Ulmus minor", 44, true, 27),
		PEUPLIER_CULTIVE(SpeciesType.BroadleavedSpecies, "Populus spp.", 25, true, 28),
		PIN_A_CROCHETS(SpeciesType.ConiferousSpecies, "Pinus uncinata", 24, true, 29), 
		PIN_D_ALEP(SpeciesType.ConiferousSpecies, "Pinus halepensis", 19, true, 30),
		PIN_LARICIO_DE_CORSE(SpeciesType.ConiferousSpecies, "Pinus nigra var. corsicana", 21, true, 31), 
		PIN_MARITIME(SpeciesType.ConiferousSpecies, "Pinus pinaster", 22, true, 32),
		PIN_NOIR_D_AUTRICHE(SpeciesType.ConiferousSpecies, "Pinus nigra", 20, true, 33), 	
		PIN_SYLVESTRE(SpeciesType.ConiferousSpecies, "Pinus sylvestris", 23, true, 34),
		ROBINIER_FAUX_ACACIA(SpeciesType.BroadleavedSpecies, "Robinia pseudoacacia", 36, true, 35), 	
		SAPIN_PECTINE(SpeciesType.ConiferousSpecies, "Abies alba", 1, true, 36),
		SAULE_CENDRE(SpeciesType.BroadleavedSpecies, "Salix cinerea", 38, false, -1),
		SAULE_MARSAULT(SpeciesType.BroadleavedSpecies, "Salix caprea", 37, true, 37),
		SORBIER_DES_OISELEURS(SpeciesType.BroadleavedSpecies, "Sorbus aucuparia", 40, true, 38), 	
		TILLEUL_A_GRANDES_FEUILLES(SpeciesType.BroadleavedSpecies, "Tilia platyphyllos", 43, true, 39), 	
		TILLEUL_A_PETITES_FEUILLES(SpeciesType.BroadleavedSpecies, "Tilia cordata", 42, true, 40),	
		TREMBLE(SpeciesType.BroadleavedSpecies, "Populus tremula", 26, true, 41),
;
		
		private static Map<String, String> AdaptationTo2014Map = new HashMap<String, String>();
		static {
			AdaptationTo2014Map.put("ERABLE_OBIER", "ERABLE_A_FEUILLES_D_OBIER");
			AdaptationTo2014Map.put("PIN_CROCHETS", "PIN_A_CROCHETS");
			AdaptationTo2014Map.put("PIN_ALEP", "PIN_D_ALEP");
			AdaptationTo2014Map.put("PIN_LARICIO", "PIN_LARICIO_DE_CORSE");
			AdaptationTo2014Map.put("PIN_NOIR", "PIN_NOIR_D_AUTRICHE");
			AdaptationTo2014Map.put("ROBINIER", "ROBINIER_FAUX_ACACIA");
			AdaptationTo2014Map.put("SORBIER_OISELEURS", "SORBIER_DES_OISELEURS");
			AdaptationTo2014Map.put("TILLEUL_GRANDES_FEUILLES", "TILLEUL_A_GRANDES_FEUILLES");
			AdaptationTo2014Map.put("TILLEUL_PETITES_FEUILLES", "TILLEUL_A_PETITES_FEUILLES");
		}
		
		
		
		public static List<FrenchHdSpecies> SpeciesIn2014;
		
		
		public final String latinName;
		final int index;
		final SpeciesType speciesType;
		final boolean availableIn2014;
		final int indexIn2014;
		
		FrenchHdSpecies(SpeciesType speciesType, String latinName, int index, boolean availableIn2014, int indexIn2014) {
			this.speciesType = speciesType;
			this.latinName = latinName;
			this.index = index;
			this.availableIn2014 = availableIn2014;
			this.indexIn2014 = indexIn2014;
		}
		
		public int getIndex() {return index;}
		public int getIndexIn2014() {return indexIn2014;}
		
		@Override
		public SpeciesType getSpeciesType() {return speciesType;}
		
		public static List<FrenchHdSpecies> getSpeciesIn2014() {
			if (SpeciesIn2014 == null) {
				SpeciesIn2014 = new ArrayList<FrenchHdSpecies>();
				for (FrenchHdSpecies species : FrenchHdSpecies.values()) {
					if (species.availableIn2014) {
						SpeciesIn2014.add(species);
					}
				}
			}
			return SpeciesIn2014;
		}
		
		
		public static FrenchHdSpecies getFrenchHdSpeciesFromThisString(String speciesName) {
			speciesName = speciesName.trim().toUpperCase().replace(" ", "_");
			speciesName = speciesName.replace("'", "_");
			speciesName = speciesName.replace("-", "_");
			if (AdaptationTo2014Map.containsKey(speciesName)) {
				speciesName = AdaptationTo2014Map.get(speciesName);
			}
			FrenchHdSpecies species = FrenchHdSpecies.valueOf(speciesName);
			return species;
		}
		
	}	

}
