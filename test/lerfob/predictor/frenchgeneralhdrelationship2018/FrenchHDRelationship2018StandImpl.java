package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class FrenchHDRelationship2018StandImpl implements FrenchHDRelationship2018Stand, Comparable<FrenchHDRelationship2018StandImpl> {

	private int monteCarloId;
	private int idp;
	private double mqd;
	private double pent2;
	private double hasBeenHarvestedInLast5Years;
	private final int index;
	double xCoord;
	double yCoord;
	final List<FrenchHDRelationship2018StandImpl> standList;
	
	List<FrenchHDRelationship2018Tree> treeList;
	
	
	FrenchHDRelationship2018StandImpl(int index, 
			int idp, 
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years,
			double xCoord,
			double yCoord,
			List<FrenchHDRelationship2018StandImpl> standList) {
		this.index = index;
		this.idp = idp;
		this.mqd = mqd;
		this.pent2 = pent2;
		this.hasBeenHarvestedInLast5Years = hasBeenHarvestedInLast5Years;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		treeList = new ArrayList<FrenchHDRelationship2018Tree>();
		this.standList = standList; 
	}
		
	
	@Override
	public String getSubjectId() {return ((Integer) idp).toString();}


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
	public int compareTo(FrenchHDRelationship2018StandImpl o) {
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
	public double getLatitudeDeg() {return yCoord;}


	@Override
	public double getLongitudeDeg() {return xCoord;}


	@Override
	public double getElevationM() {return 0;}

	
}
