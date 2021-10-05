package lerfob.carbonbalancetool;

import java.util.LinkedHashMap;
import java.util.List;

class CATIntermediateBiomassCarbonMap extends LinkedHashMap<CATCompatibleStand, Double> {

	final CATTimeTable timeTable;
	final CATCompartment carbonCompartment;
	
	CATIntermediateBiomassCarbonMap(CATTimeTable timeTable, CATCompartment carbonCompartment) {
		this.timeTable = timeTable;
		this.carbonCompartment = carbonCompartment;
	}
	
	
	void interpolateIfNeeded() {
		List<List<CATCompatibleStand>> segments = timeTable.getSegments();
		for (List<CATCompatibleStand> segment : segments) {
			CATCompatibleStand previousStand = null;
			for (CATCompatibleStand s : segment) {
				int currentIndex = timeTable.getIndexOfThisStandOnTheTimeTable(s);
				double currentValue = this.get(s);
				if (previousStand != null) {
					int previousIndex = timeTable.getIndexOfThisStandOnTheTimeTable(previousStand);
					int previousDateYr = timeTable.getDateYrAtThisIndex(previousIndex);
					double previousValue = this.get(previousStand);
					int currentDateYr = timeTable.getDateYrAtThisIndex(currentIndex);
					double slope = (currentValue - previousValue) / (currentDateYr - previousDateYr);
					for (int i = previousIndex + 1; i < currentIndex; i++) {
						double interpolatedValue = previousValue + (timeTable.getDateYrAtThisIndex(i) - previousDateYr) * slope;
						carbonCompartment.setCarbonIntoArray(i, interpolatedValue);
					}
				}
				previousStand = s;
				carbonCompartment.setCarbonIntoArray(currentIndex, currentValue);
			}
		}
	}
	
	
	
	
}
