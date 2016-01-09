package lerfob.carbonbalancetool.pythonaccess;

import repicea.treelogger.europeanbeech.EuropeanBeechBasicTree;

public class PythonEuropeanBeechTree extends PythonCarbonToolCompatibleTree implements EuropeanBeechBasicTree {

	
	PythonEuropeanBeechTree(SpeciesType speciesType,
			AverageBasicDensity species,
			StatusClass statusClass, 
			double dbhCm,
			double dbhCmStandardDeviation,
			double number, 
			double biomassRoots,
			double biomassTrunk, 
			double biomassBranches) {
		super(speciesType, species, statusClass, number, biomassRoots, biomassTrunk, biomassBranches, dbhCm, dbhCmStandardDeviation);

	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getDbhCmStandardDeviation() {return dbhCmStandardDeviation;}


	
}
