package lerfob.carbonbalancetool.soil;

import java.util.List;

import repicea.io.tools.ImportFieldElement;
import repicea.io.tools.REpiceaRecordReader;
import repicea.io.tools.REpiceaRecordReader.VariableValueException;

/**
 * This record reader makes it possible to create the reference data base for carbon sequestration
 * or emission from the soil after management operation such as thinning, clearcutting, etc.
 * @author Mathieu Fortin - January 2014
 */
@SuppressWarnings("serial")
public class SoilCarbonDataBaseRecordReader extends REpiceaRecordReader {

	@Override
	protected List<ImportFieldElement> defineFieldsToImport() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Enum<?> defineGroupFieldEnum() {return null;}

	@Override
	protected void readLineRecord(Object[] oArray, int lineCounter)
			throws VariableValueException, Exception {
		// TODO Auto-generated method stub
		
	}

}
