/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.simulation.covariateproviders.standlevel;

import java.util.HashMap;
import java.util.Map;

import lerfob.simulation.covariateproviders.standlevel.FrenchRegion2016Provider.FrenchRegion2016;

/**
 * This interface ensures that the stand instance can provide its department. 
 * @author Mathieu Fortin - Dec 2017
 */
public abstract interface FrenchDepartmentProvider {

	
	public static enum FrenchRegionPriorTo2016 {
		ALSACE(FrenchRegion2016.GRAND_EST),
		AQUITAINE(FrenchRegion2016.NOUVELLE_AQUITAINE),
		AUVERGNE(FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		BASSE_NORMANDIE(FrenchRegion2016.NORMANDIE),
		BOURGOGNE(FrenchRegion2016.BOURGOGNE_FRANCHE_COMTE),
		BRETAGNE(FrenchRegion2016.BRETAGNE),
		CENTRE(FrenchRegion2016.CENTRE_VAL_DE_LOIRE),
		CHAMPAGNE_ARDENNE(FrenchRegion2016.GRAND_EST),
		CORSE(FrenchRegion2016.CORSE),
		FRANCHE_COMTE(FrenchRegion2016.BOURGOGNE_FRANCHE_COMTE),
		HAUTE_NORMANDIE(FrenchRegion2016.NORMANDIE),
		ILE_DE_FRANCE(FrenchRegion2016.ILE_DE_FRANCE),
		LANGUEDOC_ROUSILLON(FrenchRegion2016.OCCITANIE),
		LIMOUSIN(FrenchRegion2016.NOUVELLE_AQUITAINE),
		LORRAINE(FrenchRegion2016.GRAND_EST),
		MIDI_PYRENEES(FrenchRegion2016.OCCITANIE),
		NORD_PAS_DE_CALAIS(FrenchRegion2016.HAUTS_DE_FRANCE),
		PAYS_DE_LA_LOIRE(FrenchRegion2016.PAYS_DE_LA_LOIRE),
		PICARDIE(FrenchRegion2016.HAUTS_DE_FRANCE),
		POITOU_CHARENTES(FrenchRegion2016.NOUVELLE_AQUITAINE),
		PROVENCE_ALPES_COTE_D_AZUR(FrenchRegion2016.PROVENCE_ALPES_COTE_D_AZUR),
		RHONE_ALPES(FrenchRegion2016.AUVERGNE_RHONE_ALPES);

		final FrenchRegion2016 newRegion;
		
		FrenchRegionPriorTo2016(FrenchRegion2016 newRegion) {
			this.newRegion = newRegion;
		}
		
		/**
		 * This method returns the new region as defined in 2016.
		 * @return a FrenchRegion2016 enum
		 */
		public FrenchRegion2016 getNewRegion() {return newRegion;}
	}
	
	
	
	
	public static enum FrenchDepartment implements FrenchRegion2016Provider {
		AIN("01", FrenchRegionPriorTo2016.RHONE_ALPES),
		AISNE("02", FrenchRegionPriorTo2016.PICARDIE),
		ALLIER("03", FrenchRegionPriorTo2016.AUVERGNE),
		ALPES_DE_HAUTE_PROVENCE("04", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		HAUTES_ALPES("05", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		ALPES_MARITIMES("06", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		ARDECHE("07", FrenchRegionPriorTo2016.RHONE_ALPES),
		ARDENNES("08", FrenchRegionPriorTo2016.CHAMPAGNE_ARDENNE),
		ARIEGE("09", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		AUBE("10", FrenchRegionPriorTo2016.CHAMPAGNE_ARDENNE),
		AUDE("11", FrenchRegionPriorTo2016.LANGUEDOC_ROUSILLON),
		AVEYRON("12", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		BOUCHES_DU_RHONE("13", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		CALVADOS("14", FrenchRegionPriorTo2016.BASSE_NORMANDIE),
		CANTAL("15", FrenchRegionPriorTo2016.AUVERGNE),
		CHARENTE("16", FrenchRegionPriorTo2016.POITOU_CHARENTES),
		CHARENTE_MARITIME("17", FrenchRegionPriorTo2016.POITOU_CHARENTES),
		CHER("18", FrenchRegionPriorTo2016.CENTRE),
		CORREZE("19", FrenchRegionPriorTo2016.LIMOUSIN),
		CORSE_DU_SUD("2A", FrenchRegionPriorTo2016.CORSE),
		HAUTE_CORSE("2B", FrenchRegionPriorTo2016.CORSE),
		COTE_D_OR("21", FrenchRegionPriorTo2016.BOURGOGNE),
		COTES_D_ARMOR("22", FrenchRegionPriorTo2016.BRETAGNE),
		CREUSE("23", FrenchRegionPriorTo2016.LIMOUSIN),
		DORDOGNE("24", FrenchRegionPriorTo2016.AQUITAINE),
		DOUBS("25", FrenchRegionPriorTo2016.FRANCHE_COMTE),
		DROME("26", FrenchRegionPriorTo2016.RHONE_ALPES),
		EURE("27", FrenchRegionPriorTo2016.HAUTE_NORMANDIE),
		EURE_ET_LOIR("28", FrenchRegionPriorTo2016.CENTRE),
		FINISTERE("29", FrenchRegionPriorTo2016.BRETAGNE),
		GARD("30", FrenchRegionPriorTo2016.LANGUEDOC_ROUSILLON),
		HAUTE_GARONNE("31", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		GERS("32", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		GIRONDE("33", FrenchRegionPriorTo2016.AQUITAINE),
		HERAULT("34", FrenchRegionPriorTo2016.LANGUEDOC_ROUSILLON),
		ILLE_ET_VILAINE("35", FrenchRegionPriorTo2016.BRETAGNE),
		INDRE("36", FrenchRegionPriorTo2016.CENTRE),
		INDRE_ET_LOIRE("37", FrenchRegionPriorTo2016.CENTRE),
		ISERE("38", FrenchRegionPriorTo2016.RHONE_ALPES),
		JURA("39", FrenchRegionPriorTo2016.FRANCHE_COMTE),
		LANDES("40", FrenchRegionPriorTo2016.AQUITAINE),
		LOIR_ET_CHER("41", FrenchRegionPriorTo2016.CENTRE),
		LOIRE("42", FrenchRegionPriorTo2016.RHONE_ALPES),
		HAUTE_LOIRE("43", FrenchRegionPriorTo2016.AUVERGNE),
		LOIRE_ATLANTIQUE("44", FrenchRegionPriorTo2016.PAYS_DE_LA_LOIRE),
		LOIRET("45", FrenchRegionPriorTo2016.CENTRE),
		LOT("46", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		LOT_ET_GARONNE("47", FrenchRegionPriorTo2016.AQUITAINE),
		LOZERE("48", FrenchRegionPriorTo2016.LANGUEDOC_ROUSILLON),
		MAINE_ET_LOIRE("49", FrenchRegionPriorTo2016.PAYS_DE_LA_LOIRE),
		MANCHE("50", FrenchRegionPriorTo2016.BASSE_NORMANDIE),
		MARNE("51", FrenchRegionPriorTo2016.CHAMPAGNE_ARDENNE),
		HAUTE_MARNE("52", FrenchRegionPriorTo2016.CHAMPAGNE_ARDENNE),
		MAYENNE("53", FrenchRegionPriorTo2016.PAYS_DE_LA_LOIRE),
		MEURTHE_ET_MOSELLE("54", FrenchRegionPriorTo2016.LORRAINE),
		MEUSE("55", FrenchRegionPriorTo2016.LORRAINE),
		MORBIHAN("56", FrenchRegionPriorTo2016.BRETAGNE),
		MOSELLE("57", FrenchRegionPriorTo2016.LORRAINE),
		NIEVRE("58", FrenchRegionPriorTo2016.BOURGOGNE),
		NORD("59", FrenchRegionPriorTo2016.NORD_PAS_DE_CALAIS),
		OISE("60", FrenchRegionPriorTo2016.PICARDIE),
		ORNE("61", FrenchRegionPriorTo2016.BASSE_NORMANDIE),
		PAS_DE_CALAIS("62", FrenchRegionPriorTo2016.NORD_PAS_DE_CALAIS),
		PUY_DE_DOME("63", FrenchRegionPriorTo2016.AUVERGNE),
		PYRENEES_ATLANTIQUES("64", FrenchRegionPriorTo2016.AQUITAINE),
		HAUTES_PYRENEES("65", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		PYRENEES_ORIENTALES("66", FrenchRegionPriorTo2016.LANGUEDOC_ROUSILLON),
		BAS_RHIN("67", FrenchRegionPriorTo2016.ALSACE),
		HAUT_RHIN("68", FrenchRegionPriorTo2016.ALSACE),
		RHONE("69", FrenchRegionPriorTo2016.RHONE_ALPES),
		HAUTE_SAONE("70", FrenchRegionPriorTo2016.FRANCHE_COMTE),
		SAONE_ET_LOIRE("71", FrenchRegionPriorTo2016.BOURGOGNE),
		SARTHE("72", FrenchRegionPriorTo2016.PAYS_DE_LA_LOIRE),
		SAVOIE("73", FrenchRegionPriorTo2016.RHONE_ALPES),
		HAUTE_SAVOIE("74", FrenchRegionPriorTo2016.RHONE_ALPES),
		PARIS("75", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		SEINE_MARITIME("76", FrenchRegionPriorTo2016.HAUTE_NORMANDIE),
		SEINE_ET_MARNE("77", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		YVELINES("78", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		DEUX_SEVRES("79", FrenchRegionPriorTo2016.POITOU_CHARENTES),
		SOMME("80", FrenchRegionPriorTo2016.PICARDIE),
		TARN("81", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		TARN_ET_GARONNE("82", FrenchRegionPriorTo2016.MIDI_PYRENEES),
		VAR("83", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		VAUCLUSE("84", FrenchRegionPriorTo2016.PROVENCE_ALPES_COTE_D_AZUR),
		VENDEE("85", FrenchRegionPriorTo2016.PAYS_DE_LA_LOIRE),
		VIENNE("86", FrenchRegionPriorTo2016.POITOU_CHARENTES),
		HAUTE_VIENNE("87", FrenchRegionPriorTo2016.LIMOUSIN),
		VOSGES("88", FrenchRegionPriorTo2016.LORRAINE),
		YONNE("89", FrenchRegionPriorTo2016.BOURGOGNE),
		TERRITOIRE_DE_BELFORT("90", FrenchRegionPriorTo2016.FRANCHE_COMTE),
		ESSONNE("91", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		HAUTS_DE_SEINE("92", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		SEINE_SAINT_DENIS("93", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		VAL_DE_MARNE("94", FrenchRegionPriorTo2016.ILE_DE_FRANCE),
		VAL_D_OISE("95", FrenchRegionPriorTo2016.ILE_DE_FRANCE)
		;
		
		
		static Map<String,FrenchDepartment> InnerMapCode;
		
		
		final String code;
		final FrenchRegionPriorTo2016 formerRegion;
		
		FrenchDepartment(String code, FrenchRegionPriorTo2016 formerRegion) {
			this.code = code.trim();
			this.formerRegion = formerRegion;
		}
		
		/**
		 * This method returns the code of the department. Typically, "01" stands for the Ain department and so on.
		 * @return a String
		 */
		public String getCode() {return code;}
		
		/**
		 * This method returns the new region as defined in 2016.
		 * @return a FrenchRegion2016 enum
		 */
		public FrenchRegion2016 getFrenchRegion2016() {
			return getFormerRegion().getNewRegion();
		}

		/**
		 * This method returns the region before 2016.
		 * @return a FrenchRegionPriorTo2016 enum
		 */
		public FrenchRegionPriorTo2016 getFormerRegion() {return formerRegion;}

		/**
		 * This method retrieves the department enum variable given the code (typically "01" for AIN).
		 * @param departmentCode a two-character code
		 * @return a FrenchDepartment enum or null if the department code does not exist
		 */
		public static FrenchDepartment getDepartment(String departmentCode) {
			if (InnerMapCode == null) {
				InnerMapCode = new HashMap<String, FrenchDepartment>();
				for (FrenchDepartment dep : FrenchDepartment.values()) {
					InnerMapCode.put(dep.getCode(), dep);
				}
			}
			String formattedCode = departmentCode.trim();
			if (InnerMapCode.containsKey(formattedCode)) {
				return InnerMapCode.get(formattedCode);
			} else {
				return null;
			}
				
		}
		
	}
	
	/**
	 * This method returns the French department in which the stand instance is located.
	 * @return a FrenchDepartment enum variable
	 */
	public FrenchDepartment getFrenchDepartment();
	
	
}
