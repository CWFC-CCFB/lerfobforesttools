package lerfob.carbonbalancetool.pythonaccess;

import repicea.treelogger.maritimepine.MaritimePineBasicTree;

class PythonMaritimePineTree extends PythonCarbonToolCompatibleTree implements MaritimePineBasicTree {

	
	PythonMaritimePineTree(SpeciesType speciesType,
			AverageBasicDensity species,
			TreeStatusPriorToLogging treeStatusPriorToLogging,
			StatusClass statusClass, 
			double dbhCm,
			double dbhCmStandardDeviation,
			double number, 
			double biomassRoots,
			double biomassTrunk, 
			double biomassBranches) {
		super(speciesType, species, treeStatusPriorToLogging, statusClass, number, biomassRoots, biomassTrunk, biomassBranches, dbhCm, dbhCmStandardDeviation);

	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getDbhCmStandardDeviation() {return dbhCmStandardDeviation;}


	
}