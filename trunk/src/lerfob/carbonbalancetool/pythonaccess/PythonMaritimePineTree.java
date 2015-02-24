package lerfob.carbonbalancetool.pythonaccess;

import repicea.treelogger.maritimepine.MaritimePineBasicTree;

public class PythonMaritimePineTree extends PythonCarbonToolCompatibleTree implements MaritimePineBasicTree {

	PythonMaritimePineTree(SpeciesType speciesType,
			AverageBasicDensity species,
			TreeStatusPriorToLogging treeStatusPriorToLogging,
			StatusClass statusClass, 
			double dbhCm,
			double number, 
			double biomassRoots,
			double biomassTrunk, 
			double biomassBranches) {
		super(speciesType, species, treeStatusPriorToLogging, statusClass, dbhCm, number,
				biomassRoots, biomassTrunk, biomassBranches);
	}

	@Override
	public double getStumpVolumeM3() {return rootsVolume;}

	@Override
	public double getFineWoodyDebrisVolumeM3() {return this.branchesVolume;}

}
