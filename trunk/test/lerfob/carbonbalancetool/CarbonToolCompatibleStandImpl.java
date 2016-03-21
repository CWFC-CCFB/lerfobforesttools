package lerfob.carbonbalancetool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

class CarbonToolCompatibleStandImpl implements CarbonToolCompatibleEvenAgedStand {

	
	private final double areaHa;
	private final int dateYr;
	@SuppressWarnings("rawtypes")
	private final Map<StatusClass, Collection> treeMap;
	private final String standID;
	
	@SuppressWarnings("rawtypes")
	protected CarbonToolCompatibleStandImpl(String standID, double areaHa, int dateYr) {
		this.standID = standID;
		this.areaHa = areaHa;
		this.dateYr = dateYr;
		treeMap = new HashMap<StatusClass, Collection>();
	}
	
	@SuppressWarnings("unchecked")
	protected void addTree(CarbonToolCompatibleTree tree) {
		getTrees(tree.getStatusClass()).add(tree);
	}
	
	@Override
	public double getAreaHa() {return areaHa;}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection getTrees(StatusClass statusClass) {
		if (!treeMap.containsKey(statusClass)) {
			treeMap.put(statusClass, new ArrayList());
		}
		return treeMap.get(statusClass);
	}

	@Override
	public String getStandIdentification() {return standID;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public int getAgeYr() {return getDateYr();}

	@SuppressWarnings("rawtypes")
	@Override
	public CarbonToolCompatibleStand getHarvestedStand() {
		CarbonToolCompatibleStandImpl newStand = new CarbonToolCompatibleStandImpl(standID, areaHa, dateYr);
		Collection coll = getTrees(StatusClass.alive);
		for (Object obj : coll) {
			CarbonToolCompatibleTreeImpl tree = (CarbonToolCompatibleTreeImpl) obj;
			CarbonToolCompatibleTree clonedTree = tree.clone();
			clonedTree.setStatusClass(StatusClass.cut);
			newStand.addTree(clonedTree);
		}
		return newStand;
	}

	@Override
	public boolean isInterventionResult() {
		return false;
	}

}
