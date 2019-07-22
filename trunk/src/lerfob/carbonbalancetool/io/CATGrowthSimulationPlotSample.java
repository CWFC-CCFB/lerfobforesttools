package lerfob.carbonbalancetool.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lerfob.carbonbalancetool.CATCompatibleStand;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

class CATGrowthSimulationPlotSample implements CATCompatibleStand {

	protected final CATGrowthSimulationCompositeStand compositeStand;
	
	private final Map<String, CATGrowthSimulationPlot> plotMap;
	
	
	CATGrowthSimulationPlotSample(CATGrowthSimulationCompositeStand compositeStand) {
		this.compositeStand = compositeStand;
		this.plotMap = new HashMap<String, CATGrowthSimulationPlot>();
	}
	
	@Override
	public double getAreaHa() {
		double areaHa = 0d;
		for (CATGrowthSimulationPlot plot : plotMap.values()) {
			areaHa += plot.getAreaHa();
		}
		return areaHa;
	}

	@Override
	public Collection<CATGrowthSimulationTree> getTrees(StatusClass statusClass) {
		Collection<CATGrowthSimulationTree> coll = new ArrayList<CATGrowthSimulationTree>();
		for (CATGrowthSimulationPlot plot : getPlotMap().values()) {
			coll.addAll(plot.getTrees(statusClass));
		}
		return coll;
	}

	@Override
	public boolean isInterventionResult() {return false;}

	@Override
	public String getStandIdentification() {return compositeStand.getStandIdentification();}

	@Override
	public int getDateYr() {return compositeStand.getDateYr();}

	void createPlot(String plotID, double plotAreaHa) {
		if (!getPlotMap().containsKey(plotID)) {
			getPlotMap().put(plotID, new CATGrowthSimulationPlot(plotID, plotAreaHa, this));
		}
	}
	
	Map<String, CATGrowthSimulationPlot> getPlotMap() {return plotMap;}
	
	CATGrowthSimulationPlot getPlot(String plotID) {return getPlotMap().get(plotID);}

	@Override
	public ManagementType getManagementType() {return compositeStand.getManagementType();}

	@Override
	public ApplicationScale getApplicationScale() {return compositeStand.getApplicationScale();}

	@Override
	public CATCompatibleStand getHarvestedStand() {return null;}

	/*
	 * Useless for this class.
	 */
	@Override
	public int getAgeYr() {return getDateYr();}

}
