package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class ValidationOn2014Data {

	static List<String> SpeciesList = new ArrayList<String>();

	
	private static Map<Integer, Map<Integer, FrenchHDRelationship2018StandImpl>> readTrees() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2018PredictorTest.class) + "dataHD.csv";
		List<FrenchHDRelationship2018StandImpl> standList = new ArrayList<FrenchHDRelationship2018StandImpl>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			int idp;
			double harvestInLastFiveYears;
			int year;
			String species;
			double pent2;
			double gOther;
			double d130;
//			double pred;
			double mqd;
			double htot;
			double meanTemp_3, meanTemp_4, meanTemp_5, meanTemp_6, meanTemp_7, meanTemp_8, meanTemp_9;
			double meanPrec_3, meanPrec_4, meanPrec_5, meanPrec_6, meanPrec_7, meanPrec_8, meanPrec_9;
			Map<Integer, Map<Integer, FrenchHDRelationship2018StandImpl>> standMap = new HashMap<Integer, Map<Integer, FrenchHDRelationship2018StandImpl>>();
			int counter = 0;
			while ((record = reader.nextRecord()) != null) {
				idp = Integer.parseInt(record[0].toString());
				if (record[1].toString().trim().equals("NA") || record[1].toString().trim().equals("0")) {
					harvestInLastFiveYears = 0;
				} else {
					harvestInLastFiveYears = 1;
				}
				year = Integer.parseInt(record[4].toString());
				species = record[5].toString();
				pent2 = Double.parseDouble(record[6].toString());
				htot = Double.parseDouble(record[8].toString()) + 1.3;
				gOther = Double.parseDouble(record[9].toString());
				d130 = Double.parseDouble(record[10].toString());
				mqd = d130 - Double.parseDouble(record[12].toString());
				meanTemp_3 = Double.parseDouble(record[27].toString()); 
				meanTemp_4 = Double.parseDouble(record[28].toString()); 
				meanTemp_5 = Double.parseDouble(record[29].toString()); 
				meanTemp_6 = Double.parseDouble(record[30].toString()); 
				meanTemp_7 = Double.parseDouble(record[31].toString()); 
				meanTemp_8 = Double.parseDouble(record[32].toString()); 
				meanTemp_9 = Double.parseDouble(record[33].toString()); 
				meanPrec_3 = Double.parseDouble(record[39].toString()); 
				meanPrec_4 = Double.parseDouble(record[40].toString()); 
				meanPrec_5 = Double.parseDouble(record[41].toString()); 
				meanPrec_6 = Double.parseDouble(record[42].toString()); 
				meanPrec_7 = Double.parseDouble(record[43].toString()); 
				meanPrec_8 = Double.parseDouble(record[44].toString()); 
				meanPrec_9 = Double.parseDouble(record[45].toString()); 
				double meanPrec = meanPrec_3 + meanPrec_4 + meanPrec_5 + meanPrec_6 + meanPrec_7 + meanPrec_8 + meanPrec_9; 
				double meanTemp = (meanTemp_3 + meanTemp_4 + meanTemp_5 + meanTemp_6 + meanTemp_7 + meanTemp_8 + meanTemp_9) / 7d;
				if (!standMap.containsKey(year)) {
					standMap.put(year, new HashMap<Integer, FrenchHDRelationship2018StandImpl>());
				}
				Map<Integer, FrenchHDRelationship2018StandImpl> innerMap = standMap.get(year);
				if (!innerMap.containsKey(idp)) {
					FrenchHDRelationship2018StandImpl stand = new FrenchHDRelationship2018StandImpl(counter++, idp, mqd, pent2, harvestInLastFiveYears, meanTemp, meanPrec, standList);
					innerMap.put(idp, stand);
				}
				new FrenchHDRelationship2018TreeImpl(htot, d130, gOther, species, -1, innerMap.get(idp));
				if (!SpeciesList.contains(species)) {
					SpeciesList.add(species);
				}
			}
			Collections.sort(SpeciesList);
			return standMap;
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the stands");
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	
	
	
	
	public static void main(String[] args) {
		readTrees();
		// TODO FP find a way to return the height only if the tree has been selected randomly
	}
}
