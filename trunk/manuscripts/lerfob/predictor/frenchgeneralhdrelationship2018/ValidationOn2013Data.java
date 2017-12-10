package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

	static Map<Integer, FrenchHDRelationship2018StandImpl> StandMap;
	
	private static Map<Integer, FrenchHDRelationship2018StandImpl> readTrees() {
		if (StandMap == null) {
			FrenchHDRelationship2018PredictorTest.SpeciesList.clear();
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
//				double pred;
				double mqd;
				double htot;
				double meanTemp_3, meanTemp_4, meanTemp_5, meanTemp_6, meanTemp_7, meanTemp_8, meanTemp_9;
				double meanPrec_3, meanPrec_4, meanPrec_5, meanPrec_6, meanPrec_7, meanPrec_8, meanPrec_9;
				Map<Integer, FrenchHDRelationship2018StandImpl> standMap = new HashMap<Integer, FrenchHDRelationship2018StandImpl>();
				int counter = 0;
				while ((record = reader.nextRecord()) != null) {
					idp = Integer.parseInt(record[0].toString());
					if (record[1].toString().trim().equals("NA") || record[1].toString().trim().equals("0")) {
						harvestInLastFiveYears = 0;
					} else {
						harvestInLastFiveYears = 1;
					}
					year = Integer.parseInt(record[4].toString());
					if (year == 2013) {
						
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
						if (!standMap.containsKey(idp)) {
							FrenchHDRelationship2018StandImpl stand = new FrenchHDRelationship2018StandImpl(counter++, idp, mqd, pent2, harvestInLastFiveYears, meanTemp, meanPrec, standList);
							standMap.put(idp, stand);
						}
						new FrenchHDRelationship2018TreeImpl(-1, d130, gOther, species, htot, standMap.get(idp));
						if (!FrenchHDRelationship2018PredictorTest.SpeciesList.contains(species)) {
							FrenchHDRelationship2018PredictorTest.SpeciesList.add(species);
						}
					}
				}
				Collections.sort(FrenchHDRelationship2018PredictorTest.SpeciesList);
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

	public void validateWithTheNumberOfKnownHeightsPerPlot(int i) throws IOException {
		readTrees();
		FrenchHDRelationship2018Predictor pred = new FrenchHDRelationship2018Predictor();		// deterministic 
		FrenchCommercialVolume2014Predictor volPred = new FrenchCommercialVolume2014Predictor(false);
		
		for (FrenchHDRelationship2018StandImpl stand : StandMap.values()) {
			stand.clear();
			stand.setHeightForThisNumberOfTrees(i);
			for (FrenchHDRelationship2018Tree t : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) t;
				if (!tree.knownHeight) {
					tree.heightM = pred.predictHeightM(stand, tree);
				}
				FrenchHDRelationship2018TreeImpl.TrueHeight = true;
				tree.obsVolDm3 = volPred.predictTreeCommercialVolumeDm3(tree);
				FrenchHDRelationship2018TreeImpl.TrueHeight = false;
				tree.predictedVolDm3 = volPred.predictTreeCommercialVolumeDm3(tree);
			}
		}
		String filename = ObjectUtility.getPackagePath(getClass()) + "knownHeight_" + i + ".csv";
		filename = filename.replace("bin", "manuscripts");
		File file = new File(filename);
		CSVWriter writer = new CSVWriter(file, false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("idp"));
		fields.add(new CSVField("species"));
		fields.add(new CSVField("speciesType"));
		fields.add(new CSVField("d130Cm"));
		fields.add(new CSVField("heightM"));
		fields.add(new CSVField("trueHeightM"));
		fields.add(new CSVField("knownHeight"));
		fields.add(new CSVField("obsVolDm3"));
		fields.add(new CSVField("predictedVolDm3"));
		writer.setFields(fields);
		Object[] record;
		for (FrenchHDRelationship2018StandImpl stand : StandMap.values()) {
			for (FrenchHDRelationship2018Tree t : stand.getTreesForFrenchHDRelationship()) {
				record = new Object[9];
				record[0] = stand.getSubjectId();
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) t;
				record[1] = tree.species;
				record[2] = tree.species.type;
				record[3] = tree.getDbhCm();
				record[4] = tree.heightM;
				record[5] = tree.reference;
				record[6] = tree.knownHeight;
				record[7] = tree.obsVolDm3;
				record[8] = tree.predictedVolDm3;
				writer.addRecord(record);
			}
		}
		writer.close();
	}
	
	
	
	public static void main(String[] args) throws IOException {
		ValidationOn2013Data validator = new ValidationOn2013Data();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		System.out.println("Running height simulation without known heights...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(0);
		System.out.println("Running height simulation with 1 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(1);
		System.out.println("Running height simulation with 2 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(2);
		System.out.println("Running height simulation with 3 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(3);
		System.out.println("Running height simulation with 4 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(4);
		System.out.println("Running height simulation with 5 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(5);
		System.out.println("Running height simulation with 6 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(6);
		System.out.println("Running height simulation with 7 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(7);
		System.out.println("Running height simulation with 8 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(8);
		System.out.println("Simulations done");
	}
}
