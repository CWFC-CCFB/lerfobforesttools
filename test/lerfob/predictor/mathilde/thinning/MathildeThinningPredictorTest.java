package lerfob.predictor.mathilde.thinning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class MathildeThinningPredictorTest {

	private static List<MathildeThinningTreeImpl> Trees;
	
	
	
	private static void ReadTrees() {
		Trees = new ArrayList<MathildeThinningTreeImpl>();
		
		String filenamePath = ObjectUtility.getRelativePackagePath(MathildeThinningPredictorTest.class) + "dataBaseThinningGlobalPredictions.csv";
		
		try {
			CSVReader reader = new CSVReader(filenamePath);
			Object[] record;
			
			while((record = reader.nextRecord()) != null) {
				int u = 0;
			}
			
		} catch (IOException e) {
			System.out.println("Unable to read trees in MathildeThinningPredictorTest class!");
		}
		
		
		
		
	}
}
