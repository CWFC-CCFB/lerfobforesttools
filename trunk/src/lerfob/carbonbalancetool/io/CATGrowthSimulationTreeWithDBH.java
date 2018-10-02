package lerfob.carbonbalancetool.io;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedLoggableTree;

class CATGrowthSimulationTreeWithDBH extends CATGrowthSimulationTree implements DiameterBasedLoggableTree {

	private final double dbhCm;
	
	CATGrowthSimulationTreeWithDBH(CATGrowthSimulationPlot plot, 
			StatusClass statusClass, 
			double treeVolumeDm3,
			double numberOfTrees, 
			String originalSpeciesName,
			double dbhCm) {
		super(plot, statusClass, treeVolumeDm3, numberOfTrees, originalSpeciesName);
		this.dbhCm = dbhCm;
	}

	@Override
	public double getDbhCm() {return dbhCm;}

}
