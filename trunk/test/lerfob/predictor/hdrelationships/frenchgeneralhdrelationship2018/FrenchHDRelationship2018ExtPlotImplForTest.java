package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.util.List;

class FrenchHDRelationship2018ExtPlotImplForTest extends FrenchHDRelationship2018PlotImpl 
							implements FrenchHDRelationship2018ExtPlot, 
							Comparable<FrenchHDRelationship2018ExtPlotImplForTest> {

	private final int index;
	double meanTemp;
	double meanPrec;
	final List<FrenchHDRelationship2018ExtPlotImplForTest> plotList;
	
	
	FrenchHDRelationship2018ExtPlotImplForTest(int index, 
			int idp, 
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years,
			double meanTemp,
			double meanPrec,
			List<FrenchHDRelationship2018ExtPlotImplForTest> standList) {
		super(((Integer) idp).toString(), pent2, 0d, 0d, hasBeenHarvestedInLast5Years == 1d, 0, mqd, 2010);
		this.index = index;
		this.plotList = standList; 
		this.meanTemp = meanTemp;
		this.meanPrec = meanPrec;
	}
		
	@Override
	public int compareTo(FrenchHDRelationship2018ExtPlotImplForTest o) {
		if (index < o.index) {
			return -1;
		} else if (index == o.index) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public double getMeanTemperatureOfGrowingSeason() {return meanTemp;}

	@Override
	public double getMeanPrecipitationOfGrowingSeason() {return meanPrec;}

}
