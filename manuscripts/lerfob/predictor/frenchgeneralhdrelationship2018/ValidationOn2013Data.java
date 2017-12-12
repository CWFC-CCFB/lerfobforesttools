package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import lerfob.predictor.frenchcommercialvolume2014.FrenchCommercialVolume2014Predictor;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.util.ObjectUtility;

public class ValidationOn2013Data {

	static enum Department {
		
		d54("Lorraine"),
		d55("Lorraine"),
		d57("Lorraine"),
		d88("Lorraine"),
		d18("Centre"),
		d28("Centre"),
		d36("Centre"),
		d37("Centre"),
		d41("Centre"),
		d45("Centre"),
		d03("Auvergne"),
		d15("Auvergne"),
		d43("Auvergne"),
		d63("Auvergne"),
;
		final String region;
		
		Department(String region) {
			this.region = region;
		}
		
	}
	
	
	static Map<Integer, FrenchHDRelationship2018StandImpl> StandMap;
	
	private static Map<Integer, FrenchHDRelationship2018StandImpl> readTrees() {
		if (StandMap == null) {
			String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2018PredictorTest.class) + "dataHDComplete.csv";
			List<FrenchHDRelationship2018StandImpl> standList = new ArrayList<FrenchHDRelationship2018StandImpl>();
			CSVReader reader = null;
			try {
				// TODO FP update the reader with the new fields and update the vectors of parameters
				reader = new CSVReader(filename);
				Object[] record;
				int idp;
				double harvestInLastFiveYears;
				int year;
				String species;
				double pent2;
				double gOther;
				double d130;
				String dep;
				double w;
//				double pred;
				double mqd;
				double htot;
				double meanTemp_3, meanTemp_4, meanTemp_5, meanTemp_6, meanTemp_7, meanTemp_8, meanTemp_9;
				double meanPrec_3, meanPrec_4, meanPrec_5, meanPrec_6, meanPrec_7, meanPrec_8, meanPrec_9;
				Map<Integer, FrenchHDRelationship2018StandImpl> standMap = new HashMap<Integer, FrenchHDRelationship2018StandImpl>();
				int counter = 0;
				while ((record = reader.nextRecord()) != null) {
					idp = Integer.parseInt(record[0].toString());
					dep = record[1].toString().trim();

					if (record[2].toString().trim().equals("NA") || record[1].toString().trim().equals("0")) {
						harvestInLastFiveYears = 0;
					} else {
						harvestInLastFiveYears = 1;
					}
					
					year = Integer.parseInt(record[5].toString());

					if (year == 2013) {
						
						species = record[6].toString();
						pent2 = Double.parseDouble(record[7].toString());
						double htot_mod = Double.parseDouble(record[8].toString());
						if (Double.isNaN(htot_mod)) {
							int u = 0;
						}
						htot = htot_mod + 1.3;
						gOther = Double.parseDouble(record[9].toString());
						w = Double.parseDouble(record[10].toString());
						d130 = Double.parseDouble(record[11].toString());
						mqd = d130 - Double.parseDouble(record[13].toString());
						meanTemp_3 = Double.parseDouble(record[28].toString()); 
						meanTemp_4 = Double.parseDouble(record[29].toString()); 
						meanTemp_5 = Double.parseDouble(record[30].toString()); 
						meanTemp_6 = Double.parseDouble(record[31].toString()); 
						meanTemp_7 = Double.parseDouble(record[32].toString()); 
						meanTemp_8 = Double.parseDouble(record[33].toString()); 
						meanTemp_9 = Double.parseDouble(record[34].toString()); 
						meanPrec_3 = Double.parseDouble(record[40].toString()); 
						meanPrec_4 = Double.parseDouble(record[41].toString()); 
						meanPrec_5 = Double.parseDouble(record[42].toString()); 
						meanPrec_6 = Double.parseDouble(record[43].toString()); 
						meanPrec_7 = Double.parseDouble(record[44].toString()); 
						meanPrec_8 = Double.parseDouble(record[45].toString()); 
						meanPrec_9 = Double.parseDouble(record[46].toString()); 
						double meanPrec = meanPrec_3 + meanPrec_4 + meanPrec_5 + meanPrec_6 + meanPrec_7 + meanPrec_8 + meanPrec_9; 
						double meanTemp = (meanTemp_3 + meanTemp_4 + meanTemp_5 + meanTemp_6 + meanTemp_7 + meanTemp_8 + meanTemp_9) / 7d;
						if (!standMap.containsKey(idp)) {
							ValidationOn2013DataStand stand = new ValidationOn2013DataStand(counter++, 
									idp, 
									dep,
									mqd, 
									pent2, 
									harvestInLastFiveYears, 
									meanTemp, 
									meanPrec, 
									standList);
							standMap.put(idp, stand);
						}
						new ValidationOn2013DataTree(-1, d130, gOther, species, htot, w, standMap.get(idp));
					}
				}
				standList.addAll(standMap.values());
				StandMap = standMap;
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
		return StandMap;
	}

	public void validateWithTheNumberOfKnownHeightsPerPlot(int i, int realization) throws IOException {
		readTrees();
		FrenchHDRelationship2018Predictor pred = new FrenchHDRelationship2018Predictor();		// deterministic 
		
		for (FrenchHDRelationship2018StandImpl stand : StandMap.values()) {
			stand.clear();
			stand.setHeightForThisNumberOfTrees(i);
			for (FrenchHDRelationship2018Tree t : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) t;
				if (!tree.knownHeight) {
					tree.heightM = pred.predictHeightM(stand, tree);
				}
			}
		}
		String filename = ObjectUtility.getPackagePath(getClass()) + "knownHeight_" + i + "_" + realization + ".csv";
		filename = filename.replace("bin", "manuscripts");
		File file = new File(filename);
		CSVWriter writer = new CSVWriter(file, false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("realization"));
		fields.add(new CSVField("idp"));
		fields.add(new CSVField("species"));
		fields.add(new CSVField("speciesType"));
		fields.add(new CSVField("d130Cm"));
		fields.add(new CSVField("heightM"));
		fields.add(new CSVField("trueHeightM"));
		fields.add(new CSVField("knownHeight"));
		writer.setFields(fields);
		Object[] record;
		for (FrenchHDRelationship2018StandImpl stand : StandMap.values()) {
			for (FrenchHDRelationship2018Tree t : stand.getTreesForFrenchHDRelationship()) {
				record = new Object[8];
				record[0] = realization;
				record[1] = stand.getSubjectId();
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) t;
				record[2] = tree.species;
				record[3] = tree.species.type;
				record[4] = tree.getDbhCm();
				record[5] = tree.heightM;
				record[6] = tree.reference;
				record[7] = tree.knownHeight;
				writer.addRecord(record);
			}
		}
		writer.close();
	}
	
	
	
	public static void main(String[] args) throws IOException {
		int nbMaxReal = 100;
		ValidationOn2013Data validator = new ValidationOn2013Data();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		System.out.println("Running height simulation without known heights...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(0,0);
		for (int realization = 0; realization < nbMaxReal; realization++) {
			System.out.println("Running realization " + realization);
//			System.out.println("Running height simulation with 1 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(1,realization);
//			System.out.println("Running height simulation with 2 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(2,realization);
//			System.out.println("Running height simulation with 3 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(3,realization);
//			System.out.println("Running height simulation with 4 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(4,realization);
//			System.out.println("Running height simulation with 5 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(5,realization);
//			System.out.println("Running height simulation with 6 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(6,realization);
//			System.out.println("Running height simulation with 7 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(7,realization);
//			System.out.println("Running height simulation with 8 known height per plot...");
			validator.validateWithTheNumberOfKnownHeightsPerPlot(8,realization);
		}
		System.out.println("Simulations done");
	}
}