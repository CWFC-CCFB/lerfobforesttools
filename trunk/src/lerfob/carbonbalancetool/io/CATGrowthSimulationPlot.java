package lerfob.carbonbalancetool.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.carbonbalancetool.CATCompatibleStand;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

class CATGrowthSimulationPlot implements CATCompatibleStand {

	private final Map<StatusClass, List<CATGrowthSimulationTree>> statusMap;
	private final double areaHa;
	protected final CATGrowthSimulationPlotSample plotSample;
	private final String plotID;
	
	
	CATGrowthSimulationPlot(String plotID, double areaHa, CATGrowthSimulationPlotSample plotSample) {
		this.areaHa = areaHa;
		this.plotSample = plotSample;
		this.plotID = plotID;
		statusMap = new HashMap<StatusClass, List<CATGrowthSimulationTree>>();
		for (StatusClass status : StatusClass.values()) {
			statusMap.put(status, new ArrayList<CATGrowthSimulationTree>());
		}
	}
	
	
	@Override
	public double getAreaHa() {return areaHa;}

	@Override
	public Collection<CATGrowthSimulationTree> getTrees(StatusClass statusClass) {return statusMap.get(statusClass);}

	@Override
	public boolean isInterventionResult() {return !getTrees(StatusClass.cut).isEmpty();}

	@Override
	public String getStandIdentification() {return plotID;}

	@Override
	public int getDateYr() {return plotSample.getDateYr();}

	protected void addTree(CATGrowthSimulationTree tree) {
		getTrees(tree.getStatusClass()).add(tree);
	}

	
}
