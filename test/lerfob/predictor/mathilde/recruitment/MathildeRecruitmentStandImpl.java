package lerfob.predictor.mathilde.recruitment;

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.simulation.HierarchicalLevel;

class MathildeRecruitmentStandImpl implements MathildeRecruitmentStand {

	
	private final String idp;
	private final double basalAreaM2Ha;
	private Map<MathildeTreeSpecies, Double> basalAreaSpeciesMap = new HashMap<MathildeTreeSpecies, Double>();
	
	MathildeRecruitmentStandImpl(String idp, double basalAreaM2Ha) {
		this.idp = idp;
		this.basalAreaM2Ha = basalAreaM2Ha;
	}
	
	@Override
	public String getSubjectId() {return idp;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return 5;}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public boolean isGoingToBeHarvested() {return false;}

	@Override
	public double getBasalAreaM2HaOfThisSpecies(MathildeTreeSpecies species) {
		return basalAreaSpeciesMap.get(species);
	}
	
	protected void setSpecies(MathildeTreeSpecies species, double basalAreaForThisSpecies) {
		basalAreaSpeciesMap.put(species, basalAreaForThisSpecies);
	}

	
}
