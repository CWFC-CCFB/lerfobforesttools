package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2014;

import java.util.HashMap;
import java.util.Map;

class FrenchHDRelationship2014TreeImpl implements FrenchHDRelationship2014Tree {

	protected static boolean BlupPrediction = false;
	
	private static Map<String, String> oMap = new HashMap<String, String>();
	private static Map<String, FrenchHdSpecies> MatchMap = new HashMap<String, FrenchHdSpecies>();
	static {
		MatchMap.put("Alisier blanc", FrenchHdSpecies.ALISIER_BLANC);
		MatchMap.put("Alisier torminal", FrenchHdSpecies.ALISIER_TORMINAL);
		MatchMap.put("Aubepine monogyne", FrenchHdSpecies.AUBEPINE_MONOGYNE);
		MatchMap.put("Aulne glutineux", FrenchHdSpecies.AULNE_GLUTINEUX);
		MatchMap.put("Bouleau verruqueux", FrenchHdSpecies.BOULEAU_VERRUQUEUX);
		MatchMap.put("Charme", FrenchHdSpecies.CHARME);
		MatchMap.put("Chataignier", FrenchHdSpecies.CHATAIGNIER);
		MatchMap.put("Chene-liege", FrenchHdSpecies.CHENE_LIEGE);
		MatchMap.put("Chene pedoncule", FrenchHdSpecies.CHENE_PEDONCULE);
		MatchMap.put("Chene pubescent", FrenchHdSpecies.CHENE_PUBESCENT);
		MatchMap.put("Chene rouge", FrenchHdSpecies.CHENE_ROUGE);
		MatchMap.put("Chene sessile", FrenchHdSpecies.CHENE_SESSILE);
		MatchMap.put("Chene tauzin", FrenchHdSpecies.CHENE_TAUZIN);
		MatchMap.put("Chene vert", FrenchHdSpecies.CHENE_VERT);
		MatchMap.put("Douglas", FrenchHdSpecies.DOUGLAS);
		MatchMap.put("Epicea commun", FrenchHdSpecies.EPICEA_COMMUN);
		MatchMap.put("Epicea de sitka", FrenchHdSpecies.EPICEA_DE_SITKA);
		MatchMap.put("Erable a feuilles d'obier", FrenchHdSpecies.ERABLE_A_FEUILLES_D_OBIER);
		MatchMap.put("Erable champetre", FrenchHdSpecies.ERABLE_CHAMPETRE);
		MatchMap.put("Erable sycomore", FrenchHdSpecies.ERABLE_SYCOMORE);
		MatchMap.put("Frene commun", FrenchHdSpecies.FRENE_COMMUN);
		MatchMap.put("Hetre", FrenchHdSpecies.HETRE);
		MatchMap.put("Houx", FrenchHdSpecies.HOUX);
		MatchMap.put("Meleze d'europe", FrenchHdSpecies.MELEZE_D_EUROPE);
		MatchMap.put("Merisier", FrenchHdSpecies.MERISIER);
		MatchMap.put("Noisetier coudrier", FrenchHdSpecies.NOISETIER_COUDRIER);
		MatchMap.put("Orme champetre", FrenchHdSpecies.ORME_CHAMPETRE);
		MatchMap.put("Peuplier cultive", FrenchHdSpecies.PEUPLIER_CULTIVE);
		MatchMap.put("Pin a crochets", FrenchHdSpecies.PIN_A_CROCHETS);
		MatchMap.put("Pin d'alep", FrenchHdSpecies.PIN_D_ALEP);
		MatchMap.put("Pin laricio de corse", FrenchHdSpecies.PIN_LARICIO_DE_CORSE);
		MatchMap.put("Pin maritime", FrenchHdSpecies.PIN_MARITIME);
		MatchMap.put("Pin noir d'autriche", FrenchHdSpecies.PIN_NOIR_D_AUTRICHE);
		MatchMap.put("Pin sylvestre", FrenchHdSpecies.PIN_SYLVESTRE);
		MatchMap.put("Robinier faux acacia", FrenchHdSpecies.ROBINIER_FAUX_ACACIA);
		MatchMap.put("Sapin pectine", FrenchHdSpecies.SAPIN_PECTINE);
		MatchMap.put("Saule marsault", FrenchHdSpecies.SAULE_MARSAULT);
		MatchMap.put("Sorbier des oiseleurs", FrenchHdSpecies.SORBIER_DES_OISELEURS);
		MatchMap.put("Tilleul a grandes feuilles", FrenchHdSpecies.TILLEUL_A_GRANDES_FEUILLES);
		MatchMap.put("Tilleul a petites feuilles", FrenchHdSpecies.TILLEUL_A_PETITES_FEUILLES);
		MatchMap.put("Tremble", FrenchHdSpecies.TREMBLE);
	}
	
	
	private double heightM;
	private double dbhCm;
	private String species;
	private FrenchHDRelationship2014Stand stand;
	private double pred;
	private double gOther;
	
	FrenchHDRelationship2014TreeImpl(double heightM, double dbhCm, double gOther, String species, double pred, FrenchHDRelationship2014Stand stand) {
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = species;
		this.pred = pred;
		this.stand = stand;
		((FrenchHDRelationship2014StandImpl) this.stand).addTree(this); 
	}
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	@Override
	public double getHeightM() {
		if (BlupPrediction) {
			return heightM;
		} else {
			return -1d;
		}
	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCm = getLnDbhCmPlus1();
		return lnDbhCm * lnDbhCm;
	}

	@Override
	public FrenchHdSpecies getFrenchHDTreeSpecies() {
		if (MatchMap.containsKey(species)) {
			return MatchMap.get(species);
 		} else {
 			return null;
 		}
//		int index = FrenchHDRelationship2014PredictorTest.speciesList.indexOf(species);
////		return FrenchHdSpecies.values()[index];
//		FrenchHdSpecies sp = FrenchHdSpecies.getSpeciesIn2014().get(index);
//		if (!oMap.containsKey(sp.name())) {
//			oMap.put(sp.name(), species);
//			System.out.println("New match : " + sp.name() + " - " + species);
//		} else {
//			if (!oMap.get(sp.name()).equals(species)) {
//				System.out.println("Mismatch : " + sp.name() + " - " + species);
//			}
//		}
//		
//		return sp;
	}

	protected double getPred() {return pred;}
	protected double getGOther() {return gOther;}
	
	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return null;
	}

}
