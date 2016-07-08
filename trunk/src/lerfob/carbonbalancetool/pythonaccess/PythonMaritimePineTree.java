package lerfob.carbonbalancetool.pythonaccess;

import repicea.treelogger.maritimepine.MaritimePineBasicLoggableTree;

class PythonMaritimePineTree extends PythonCarbonToolCompatibleTree implements MaritimePineBasicLoggableTree {

	
	PythonMaritimePineTree(SpeciesType speciesType,
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

	@Override
	public double getHarvestedStumpVolumeM3() {return rootsVolume;}

	@Override
	public double getHarvestedCrownVolumeM3() {return branchesVolume;}

	
}
