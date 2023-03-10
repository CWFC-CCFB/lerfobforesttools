package lerfob.predictor.mathilde.recruitment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;

class MathildeRecruitmentStandImpl implements MathildeRecruitmentStand {

	
	private final String idp;
	private final double basalAreaM2Ha;
	private Map<MathildeTreeSpecies, Double> basalAreaSpeciesMap = new HashMap<MathildeTreeSpecies, Double>();
	protected List<MathildeTreeImpl> treeList;
	
	MathildeRecruitmentStandImpl(String idp, double basalAreaM2Ha) {
		this.idp = idp;
		this.basalAreaM2Ha = basalAreaM2Ha;
		treeList = new ArrayList<MathildeTreeImpl>();
	}
	
	@Override
	public String getSubjectId() {return idp;}

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
	
	protected void setBasalAreaM2HaOfThisSpecies(MathildeTreeSpecies species, double basalAreaForThisSpecies) {
		basalAreaSpeciesMap.put(species, basalAreaForThisSpecies);
	}

	protected List<MathildeTreeImpl> getTrees() {
		return treeList;
	}
}
