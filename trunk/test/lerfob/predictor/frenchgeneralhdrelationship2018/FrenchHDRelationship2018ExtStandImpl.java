package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repicea.simulation.HierarchicalLevel;

class FrenchHDRelationship2018ExtStandImpl implements FrenchHDRelationship2018ExtStand, Comparable<FrenchHDRelationship2018ExtStandImpl> {

	private int monteCarloId;
	private int idp;
	private double mqd;
	private double pent2;
	private double hasBeenHarvestedInLast5Years;
	private final int index;
	double meanTemp;
	double meanPrec;
	final List<FrenchHDRelationship2018ExtStandImpl> standList;
	
	List<FrenchHDRelationship2018Tree> treeList;
	
	
	FrenchHDRelationship2018ExtStandImpl(int index, 
			int idp, 
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years,
			double meanTemp,
			double meanPrec,
			List<FrenchHDRelationship2018ExtStandImpl> standList) {
		this.index = index;
		this.idp = idp;
		this.mqd = mqd;
		this.pent2 = pent2;
		this.hasBeenHarvestedInLast5Years = hasBeenHarvestedInLast5Years;
		this.meanTemp = meanTemp;
		this.meanPrec = meanPrec;
		treeList = new ArrayList<FrenchHDRelationship2018Tree>();
		this.standList = standList; 
	}
		
	
	@Override
	public String getSubjectId() {return ((Integer) idp).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}


	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	@Override
	public double getMeanQuadraticDiameterCm() {return mqd;}

	@Override
	public Collection<FrenchHDRelationship2018Tree> getTreesForFrenchHDRelationship() {return treeList;}

	@Override
	public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2018Tree tree) {
		return ((FrenchHDRelationship2018TreeImpl) tree).getGOther();
	}

	@Override
	public double getSlopeInclinationPercent() {return pent2;}
	
	protected void addTree(FrenchHDRelationship2018TreeImpl tree) {
		treeList.add(tree);
	}


	@Override
	public int compareTo(FrenchHDRelationship2018ExtStandImpl o) {
		if (index < o.index) {
			return -1;
		} else if (index == o.index) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public boolean isInterventionResult() {return this.hasBeenHarvestedInLast5Years == 1d;}


//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public List<HDRelationshipStand> getAllHDStands() {
//		return (List) standList;
//	}


	@Override
	public double getMeanTemperatureOfGrowingSeason() {
		return meanTemp;
	}


	@Override
	public double getMeanPrecipitationOfGrowingSeason() {
		return meanPrec;
	}


	@Override
	public double getLatitudeDeg() {
		return 0;
	}


	@Override
	public double getLongitudeDeg() {
		return 0;
	}


	@Override
	public double getElevationM() {
		return 0;
	}

	
}
