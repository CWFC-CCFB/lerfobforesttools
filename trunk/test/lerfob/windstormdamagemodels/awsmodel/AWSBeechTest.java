package lerfob.windstormdamagemodels.awsmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;

import org.junit.Test;

import repicea.io.FormatField;
import repicea.io.FormatWriter;
import repicea.io.javacsv.CSVField;

public class AWSBeechTest {

	private static String pathName;
	
	/**
	 * This is a hacked implementation of the AWSModel. It disables the second and the third step because the test is performed on the first step only.
	 * @author Mathieu Fortin - September 2011
	 */
	private static class AWSModelLocalImpl extends AlbrechtWindStormModel {
		
		@Override
		protected double computeProbabilityOfTotalDamage(AWSTreeSpecies species) throws Exception {return 0d;}		// disable the second step
		
		@Override
		protected double computeProportionOfDamageIfNotTotallyDamaged(AWSTreeSpecies species) throws Exception {return 0d;}		// disable the third step
		
		
	}
	
	
	
	
	@Test
	public void beechTestFirstStep() throws Exception {
		Collection<AWSTestStand> stands = readStands();
		
		AWSModelLocalImpl model = new AWSModelLocalImpl();
		for (AWSTestStand stand : stands) {
			model.getPredictionForThisStand(stand, stand);
		}

	
		Vector<FormatField> fields = new Vector<FormatField>();
		fields.add(new CSVField("relHD100_5yr"));
		fields.add(new CSVField("d100"));
		fields.add(new CSVField("relRemovedVol"));
		fields.add(new CSVField("cumulRemovals"));
		fields.add(new CSVField("predictedSAS"));
		fields.add(new CSVField("predictedJava"));
		
		String filename = pathName + File.separator + "BeechTestResult.csv";
		FormatWriter<?> writer = FormatWriter.createFormatWriter(false, filename);
		writer.setFields(fields);
		
		Object[] record = new Object[fields.size()];
		
		for (AWSTestStand stand : stands) {
			record[0] = stand.getHDRatioRel();
			record[1] = stand.getDominantDiameterCm();
			record[2] = stand.getRelRemovedVol();
			record[3] = stand.getCumulRemoval();
			record[4] = stand.getPredictedSAS();
			record[5] = stand.getPredictedJava();
			writer.addRecord(record);
		}
		writer.close();
	}
	
	
	
	
	private static Collection<AWSTestStand> readStands() throws URISyntaxException, IOException {
		Collection<AWSTestStand> stands = new ArrayList<AWSTestStand>();
		
		URL url = AWSBeechTest.class.getResource(AWSBeechTest.class.getSimpleName() + ".class");
		File file = new File(url.toURI());
		pathName = file.getParent();
		String testStands = pathName + File.separator + "testBeech.txt";
		
		InputStream is = new FileInputStream(testStands);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		reader.readLine();		// skip the first line
		String lineRead = reader.readLine();
		int i = 1;
		while (lineRead != null) {
			String[] splittedString = lineRead.split(",");
			double relHD = Double.parseDouble(splittedString[0]);
			double d100 = Double.parseDouble(splittedString[1]);
			double relRemovedVol = Double.parseDouble(splittedString[2]);
			double cumulRemoval = Double.parseDouble(splittedString[3]);
			double prediction = Double.parseDouble(splittedString[5]);
			double[] predictedProbabilities = new double[3];
			predictedProbabilities[0] = prediction;
			AWSTestStand stand = new AWSTestStand("stand" + i, 
					AWSTreeSpecies.Beech,
					d100, 
					-1d, 			// h100
					-1, 			// age
					-1d,			// v
					-1d,			// g,
					false,			// stagnantMoisture,
					0d,				// topex,
					0d,				// wind50,
					0d,				// wind99,
					false, 			// carbonateInUpperSoil,
					-1,				// year,
					cumulRemoval,
					relRemovedVol,
					-1d,			// thinningQuotient,
					-1d,			// relativeRemovedVolumeOfPreviousIntervention,
					-1,				// nbYrsSincePreviousIntervention,
					-1d,				// relativeRemovedVolumeInPast10Yrs,
					-1d,				// thinningQuotientOfPast10Yrs,
					relHD,
					predictedProbabilities);
			stands.add(stand);
			lineRead = reader.readLine();
		}
		reader.close();
		
		return stands;
	}
	
	
}
