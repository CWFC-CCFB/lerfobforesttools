package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.frenchcommercialvolume2014.FrenchCommercialVolume2014Tree;
import repicea.simulation.HierarchicalLevel;

class FrenchHDRelationship2018TreeImpl implements FrenchHDRelationship2018Tree, FrenchCommercialVolume2014Tree {

	protected static final Map<FrenchHd2018Species, FrenchCommercialVolume2014TreeSpecies> MatchTable = new HashMap<FrenchHd2018Species, FrenchCommercialVolume2014TreeSpecies>();
	static {
		MatchTable.put(FrenchHd2018Species.ALISIER_BLANC, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ALISIER_TORMINAL, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ARBOUSIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.AUBEPINE_MONOGYNE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.AULNE_GLUTINEUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.BOULEAU_VERRUQUEUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.CHARME, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.CHATAIGNIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.CHENE_LIEGE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		MatchTable.put(FrenchHd2018Species.CHENE_PEDONCULE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		MatchTable.put(FrenchHd2018Species.CHENE_PUBESCENT, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		MatchTable.put(FrenchHd2018Species.CHENE_ROUGE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_RUBRA);
		MatchTable.put(FrenchHd2018Species.CHENE_SESSILE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_PETRAEA);
		MatchTable.put(FrenchHd2018Species.CHENE_TAUZIN, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		MatchTable.put(FrenchHd2018Species.CHENE_VERT, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		MatchTable.put(FrenchHd2018Species.DOUGLAS, FrenchCommercialVolume2014TreeSpecies.PSEUDOTSUGA_MENZIESII);
		MatchTable.put(FrenchHd2018Species.EPICEA_COMMUN, FrenchCommercialVolume2014TreeSpecies.PICEA_ABIES);
		MatchTable.put(FrenchHd2018Species.EPICEA_DE_SITKA, FrenchCommercialVolume2014TreeSpecies.PICEA_ABIES);
		MatchTable.put(FrenchHd2018Species.ERABLE_CHAMPETRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ERABLE_DE_MONTPELLIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ERABLE_A_FEUILLES_D_OBIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ERABLE_SYCOMORE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.FRENE_COMMUN, FrenchCommercialVolume2014TreeSpecies.FRAXINUS_EXCELSIOR);
		MatchTable.put(FrenchHd2018Species.HETRE, FrenchCommercialVolume2014TreeSpecies.FAGUS_SYLVATICA);
		MatchTable.put(FrenchHd2018Species.HOUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.MELEZE_D_EUROPE, FrenchCommercialVolume2014TreeSpecies.LARIX_DECIDUA);
		MatchTable.put(FrenchHd2018Species.MERISIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.NOISETIER_COUDRIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.ORME_CHAMPETRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.PEUPLIER_CULTIVE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.PIN_D_ALEP, FrenchCommercialVolume2014TreeSpecies.PINUS_HALEPENSIS);
		MatchTable.put(FrenchHd2018Species.PIN_A_CROCHETS, FrenchCommercialVolume2014TreeSpecies.PINUS_UNCINATA);
		MatchTable.put(FrenchHd2018Species.PIN_LARICIO_DE_CORSE, FrenchCommercialVolume2014TreeSpecies.PINUS_LARICIO);
		MatchTable.put(FrenchHd2018Species.PIN_MARITIME, FrenchCommercialVolume2014TreeSpecies.PINUS_PINASTER);
		MatchTable.put(FrenchHd2018Species.PIN_NOIR_D_AUTRICHE, FrenchCommercialVolume2014TreeSpecies.PINUS_NIGRA);
		MatchTable.put(FrenchHd2018Species.PIN_SYLVESTRE, FrenchCommercialVolume2014TreeSpecies.PINUS_SYLVESTRIS);
		MatchTable.put(FrenchHd2018Species.ROBINIER_FAUX_ACACIA, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.SAPIN_PECTINE, FrenchCommercialVolume2014TreeSpecies.ABIES_ALBA);
		MatchTable.put(FrenchHd2018Species.SAULE_CENDRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.SAULE_MARSAULT, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.SORBIER_DES_OISELEURS, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.TILLEUL_A_GRANDES_FEUILLES, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.TILLEUL_A_PETITES_FEUILLES, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		MatchTable.put(FrenchHd2018Species.TREMBLE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
	}
	

	
	protected static boolean BlupPrediction = false;
	protected static boolean TrueHeight = false;
	
	double heightM;
	private double dbhCm;
	final FrenchHd2018Species species;
	private FrenchHDRelationship2018Stand stand;
	double reference;
	private double gOther;
	boolean knownHeight;
	double predictedVolDm3;
	double obsVolDm3;
	
	FrenchHDRelationship2018TreeImpl(double heightM, double dbhCm, double gOther, String speciesName, double pred, FrenchHDRelationship2018Stand stand) {
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = FrenchHDRelationship2018Tree.getFrenchHd2018SpeciesFromThisString(speciesName);
		this.reference = pred;
		this.stand = stand;
		((FrenchHDRelationship2018StandImpl) this.stand).addTree(this); 
	}
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	@Override
	public double getHeightM() {
		if (TrueHeight) {
			return reference;
		} else if (BlupPrediction) {
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
	public FrenchHd2018Species getFrenchHDTreeSpecies() {
		return species;
	}

	protected double getPred() {return reference;}
	protected double getGOther() {return gOther;}
	
	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return null;
	}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies() {
		return MatchTable.get(getFrenchHDTreeSpecies());
	}

}
