package lerfob.predictor.frenchgeneralhdrelationship2014;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repicea.simulation.HierarchicalLevel;

class FrenchHDRelationship2014StandImpl implements FrenchHDRelationship2014Stand, Comparable<FrenchHDRelationship2014StandImpl> {

	private int monteCarloId;
	private int idp;
	private double mqd;
	private double pent2;
	private double hasBeenHarvestedInLast5Years;
	private final int index;
	final List<FrenchHDRelationship2014StandImpl> standList;
	
	private List<FrenchHDRelationship2014Tree> treeList;
	
	
	FrenchHDRelationship2014StandImpl(int index, int idp, double mqd, double pent2, double hasBeenHarvestedInLast5Years, List<FrenchHDRelationship2014StandImpl> standList) {
		this.index = index;
		this.idp = idp;
		this.mqd = mqd;
		this.pent2 = pent2;
		this.hasBeenHarvestedInLast5Years = hasBeenHarvestedInLast5Years;
		treeList = new ArrayList<FrenchHDRelationship2014Tree>();
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
	public Collection<FrenchHDRelationship2014Tree> getTreesForFrenchHDRelationship() {return treeList;}

	@Override
	public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2014Tree tree) {
		return ((FrenchHDRelationship2014TreeImpl) tree).getGOther();
	}

	@Override
	public double getSlopeInclinationPercent() {return pent2;}
	
	protected void addTree(FrenchHDRelationship2014TreeImpl tree) {
		treeList.add(tree);
	}


	@Override
	public int compareTo(FrenchHDRelationship2014StandImpl o) {
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

}
