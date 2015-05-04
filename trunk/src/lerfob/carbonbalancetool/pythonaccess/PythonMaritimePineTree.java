package lerfob.carbonbalancetool.pythonaccess;

import repicea.treelogger.maritimepine.MaritimePineBasicTree;

public class PythonMaritimePineTree extends PythonCarbonToolCompatibleTree implements MaritimePineBasicTree {

	final double dbhCm;
	final double dbhCmStandardDeviation;

	
	PythonMaritimePineTree(SpeciesType speciesType,
			AverageBasicDensity species,
			TreeStatusPriorToLogging treeStatusPriorToLogging,
			StatusClass statusClass, 
			double dbhCm,
			double dbhStandardDeviation,
			double number, 
			double biomassRoots,
			double biomassTrunk, 
			double biomassBranches) {
		super(speciesType, species, treeStatusPriorToLogging, statusClass, number, biomassRoots, biomassTrunk, biomassBranches);
		this.dbhCm = dbhCm;
		this.dbhCmStandardDeviation = dbhStandardDeviation;

	}

	@Override
	public double getStumpVolumeM3() {return rootsVolume;}

	@Override
	public double getFineWoodyDebrisVolumeM3() {return this.branchesVolume;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getDbhCmStandardDeviation() {return dbhCmStandardDeviation;}

	
}
