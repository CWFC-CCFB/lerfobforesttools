package lerfob.simulation.covariateproviders.standlevel;

public abstract interface FrenchDepartmentProvider {

	public static enum FrenchRegion2016 {
		AUVERGNE_RHONE_ALPES,
		BOURGOGNE_FRANCHE_COMTE,
		BRETAGNE,
		CENTRE_VAL_DE_LOIRE,
		CORSE,
		GRAND_EST,
		HAUTS_DE_FRANCE,
		ILE_DE_FRANCE,
		NORMANDIE,
		NOUVELLE_AQUITAINE,
		PAYS_DE_LA_LOIRE,
		PROVENCE_ALPES_COTE_D_AZUR,
		OCCITANIE;
	}
	
	
	
	
	public static enum FrenchDepartment {
		AIN("01", "", FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		AISNE("02", "", FrenchRegion2016.HAUTS_DE_FRANCE),
		ALLIER("03", "", FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		ALPES_DE_HAUTE_PROVENCE("04", "", FrenchRegion2016.PROVENCE_ALPES_COTE_D_AZUR),
		HAUTES_ALPES("05", "", FrenchRegion2016.PROVENCE_ALPES_COTE_D_AZUR),
		ALPES_MARITIMES("06", "", FrenchRegion2016.PROVENCE_ALPES_COTE_D_AZUR),
		ARDECHE("07", "", FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		ARDENNES("08", "", FrenchRegion2016.GRAND_EST),
		ARIEGE("09", "", FrenchRegion2016.OCCITANIE),
		AUBE("10", "", FrenchRegion2016.GRAND_EST),
		AUDE("11", "", FrenchRegion2016.OCCITANIE),
		AVEYRON("12", "", FrenchRegion2016.OCCITANIE),
		BOUCHES_DU_RHONE("13", "", FrenchRegion2016.PROVENCE_ALPES_COTE_D_AZUR),
		CALVADOS("14", "", FrenchRegion2016.NORMANDIE),
		CANTAL("15", "", FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		CHARENTE("16", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		CHARENTE_MARITIME("17", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		CHER("18", "", FrenchRegion2016.CENTRE_VAL_DE_LOIRE),
		CORREZE("19", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		CORSE_DU_SUD("2A", "", FrenchRegion2016.CORSE),
		HAUTE_CORSE("2B", "", FrenchRegion2016.CORSE),
		COTE_D_OR("21", "", FrenchRegion2016.BOURGOGNE_FRANCHE_COMTE),
		COTES_D_ARMOR("22", "", FrenchRegion2016.BRETAGNE),
		CREUSE("23", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		DORDOGNE("24", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		DOUBS("25", "", FrenchRegion2016.BOURGOGNE_FRANCHE_COMTE),
		DROME("26", "", FrenchRegion2016.AUVERGNE_RHONE_ALPES),
		EURE("27", "", FrenchRegion2016.NORMANDIE),
		EURE_ET_LOIR("28", "", FrenchRegion2016.CENTRE_VAL_DE_LOIRE),
		FINISTERE("29", "", FrenchRegion2016.BRETAGNE),
		GARD("30", "", FrenchRegion2016.OCCITANIE),
		HAUTE_GARONNE("31", "", FrenchRegion2016.OCCITANIE),
		GERS("32", "", FrenchRegion2016.OCCITANIE),
		GIRONDE("33", "", FrenchRegion2016.NOUVELLE_AQUITAINE),
		HERAULT("34", "", FrenchRegion2016.OCCITANIE),
		ILLE_ET_VILAINE("35", "", FrenchRegion2016.BRETAGNE),
		INDRE("36", "", FrenchRegion2016.CENTRE_VAL_DE_LOIRE),
		INDRE_ET_LOIRE("37", "", FrenchRegion2016.CENTRE_VAL_DE_LOIRE),
		
		
		;
		
		final String code;
		final String formerRegion;
		final FrenchRegion2016 newRegion;
		
		FrenchDepartment(String code, String formerRegion, FrenchRegion2016 newRegion) {
			this.code = code;
			this.formerRegion = formerRegion;
			this.newRegion = newRegion;
		}
		
		
		
		
	}
	
	
	
	
}
