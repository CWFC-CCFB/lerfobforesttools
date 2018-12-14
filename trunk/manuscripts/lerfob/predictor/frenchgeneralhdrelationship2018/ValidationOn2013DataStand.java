package lerfob.predictor.frenchgeneralhdrelationship2018;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2018.FrenchHDRelationship2018Tree.FrenchHd2018Species;
import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;
import repicea.stats.StatisticalUtility;

public class ValidationOn2013DataStand extends FrenchHDRelationship2018PlotImplForTest {

	
	private final FrenchDepartment department;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	ValidationOn2013DataStand(int index, 
			int idp, 
			String departmentCode,
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years, 
			double meanTemp, 
			double meanPrec, 
			List<ValidationOn2013DataStand> standList) {
		super(index, idp, mqd, pent2, hasBeenHarvestedInLast5Years, meanTemp, meanPrec, (List) standList);
		department = FrenchDepartment.getDepartment(departmentCode);
		if (department == null) {
			throw new InvalidParameterException("This department " + departmentCode + " does not exist");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setHeightForThisNumberOfTrees(int i) {
		if (i > treeList.size()) {
			i = treeList.size();
		}
		if (i > 0) {
			try {
				List<FrenchHDRelationship2018Tree> sample = (List) StatisticalUtility.getSampleFromPopulation(treeList, i, false);
				for (FrenchHDRelationship2018Tree tree : sample) {
					((ValidationOn2013DataTree) tree).heightM = ((ValidationOn2013DataTree) tree).reference;
					((ValidationOn2013DataTree) tree).knownHeight = true;
				}
			} catch (Exception e) {
				int u = 0;
			}
		}
	}
	

	protected void clear() {
		for (FrenchHDRelationship2018Tree tree : treeList) {
			((ValidationOn2013DataTree) tree).heightM = -1d;
			((ValidationOn2013DataTree) tree).knownHeight = false;
		}
	}

	
	protected Map<FrenchHd2018Species, List<Double>> getDifferences() {
		Map<FrenchHd2018Species, List<Double>> diffMap = new HashMap<FrenchHd2018Species, List<Double>>();
		for (FrenchHd2018Species sp : FrenchHd2018Species.values()) {
			diffMap.put(sp, new ArrayList<Double>());
		}
		for (FrenchHDRelationship2018Tree tree : treeList) {
			ValidationOn2013DataTree t = (ValidationOn2013DataTree) tree;
			if (!t.knownHeight) {
				diffMap.get(tree.getFrenchHDTreeSpecies()).add(t.getDiff());
			}
		}
		return diffMap;
	}

	protected Map<FrenchHd2018Species, List<Double>> getObservations() {
		Map<FrenchHd2018Species, List<Double>> obsMap = new HashMap<FrenchHd2018Species, List<Double>>();
		for (FrenchHd2018Species sp : FrenchHd2018Species.values()) {
			obsMap.put(sp, new ArrayList<Double>());
		}
		for (FrenchHDRelationship2018Tree tree : treeList) {
			ValidationOn2013DataTree t = (ValidationOn2013DataTree) tree;
			if (!t.knownHeight) {
				obsMap.get(tree.getFrenchHDTreeSpecies()).add(t.reference);
			}
		}
		return obsMap;
	}

}
