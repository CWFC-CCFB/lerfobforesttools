package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.security.InvalidParameterException;
import java.util.List;

import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;

public class ValidationOn2013DataStand extends FrenchHDRelationship2018StandImpl {

	
	private final FrenchDepartment department;
	
	
	ValidationOn2013DataStand(int index, 
			int idp, 
			String departmentCode,
			double mqd, 
			double pent2, 
			double hasBeenHarvestedInLast5Years, 
			double meanTemp, 
			double meanPrec, 
			List<FrenchHDRelationship2018StandImpl> standList) {
		super(index, idp, mqd, pent2, hasBeenHarvestedInLast5Years, meanTemp, meanPrec, standList);
		department = FrenchDepartment.getDepartment(departmentCode);
		if (department == null) {
			throw new InvalidParameterException("This department " + departmentCode + " does not exist");
		}
	}

}
