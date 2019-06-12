package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.util.List;

class FrenchHDRelationship2018PlotImplForTest extends FrenchHDRelationship2018PlotImpl implements Comparable<FrenchHDRelationship2018PlotImplForTest> {

	private final int index;
	final List<FrenchHDRelationship2018PlotImplForTest> standList;
	
	
	
	FrenchHDRelationship2018PlotImplForTest(int index, 
			int idp, 
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years,
			double xCoord,
			double yCoord,
			int dateYr,
			List<FrenchHDRelationship2018PlotImplForTest> standList) {
		super(((Integer) idp).toString(), pent2, xCoord, yCoord, hasBeenHarvestedInLast5Years == 1d, 0, mqd, dateYr);
		this.index = index;
		this.standList = standList; 
	}
		
	@Override
	public int compareTo(FrenchHDRelationship2018PlotImplForTest o) {
		if (index < o.index) {
			return -1;
		} else if (index == o.index) {
			return 0;
		} else {
			return 1;
		}
	}
	
}
