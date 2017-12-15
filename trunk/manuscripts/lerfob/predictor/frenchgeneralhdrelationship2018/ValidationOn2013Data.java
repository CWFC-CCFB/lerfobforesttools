package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import lerfob.predictor.frenchgeneralhdrelationship2018.FrenchHDRelationship2018Tree.FrenchHd2018Species;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.util.ObjectUtility;

public class ValidationOn2013Data {

	static class FutureRecord {
		final int realization;
		final FrenchHd2018Species species;
		final int nbObs;
		final double bias;
		final double rmse;
		final double meanObs;

		FutureRecord(int realization, FrenchHd2018Species species, int nbObs, double bias, double rmse, double meanObs) {
			this.realization = realization;
			this.species = species;
			this.nbObs = nbObs;
			this.bias = bias;
			this.rmse = rmse;
			this.meanObs = meanObs;
		}
	}



	static Map<Integer, ValidationOn2013DataStand> StandMap;

	private static Map<Integer, ValidationOn2013DataStand> readTrees() {
		if (StandMap == null) {
			String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2018PredictorTest.class) + "dataHDComplete.csv";
			List<ValidationOn2013DataStand> standList = new ArrayList<ValidationOn2013DataStand>();
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
				String dep;
				double w;
				//				double pred;
				double mqd;
				double htot;
				double meanTemp_3, meanTemp_4, meanTemp_5, meanTemp_6, meanTemp_7, meanTemp_8, meanTemp_9;
				double meanPrec_3, meanPrec_4, meanPrec_5, meanPrec_6, meanPrec_7, meanPrec_8, meanPrec_9;
				Map<Integer, ValidationOn2013DataStand> standMap = new HashMap<Integer, ValidationOn2013DataStand>();
				int counterStand = 0;
				int counterTree = 0;
				int line = 0;
				while ((record = reader.nextRecord()) != null) {
					line++;
					try {
						idp = Integer.parseInt(record[0].toString());
						dep = record[1].toString().trim();
						if (dep.length() == 1) {
							dep = "0" + dep;
						}

						if (record[2].toString().trim().equals("NA") || record[1].toString().trim().equals("0")) {
							harvestInLastFiveYears = 0;
						} else {
							harvestInLastFiveYears = 1;
						}

						year = Integer.parseInt(record[5].toString());

						if (year == 2013) {
							species = record[6].toString();
							if (!record[7].toString().equals("NA")) {
								pent2 = Double.parseDouble(record[7].toString());
								if (!record[8].toString().equals("NA")) {
									htot = Double.parseDouble(record[8].toString()) + 1.3;
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
										ValidationOn2013DataStand stand = new ValidationOn2013DataStand(counterStand++, 
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
									
									new ValidationOn2013DataTree(counterTree++, d130, gOther, species, htot, w, standMap.get(idp));
								}
							}
						}

					} catch (Exception e) {
						System.out.println("Error while reading file at line " + line);
					}
				}
				System.out.println("Nb of trees = " + counterTree);
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

	public void validateWithTheNumberOfKnownHeightsPerPlot(int i, int nbRealizations) throws IOException {
		readTrees();

		List<FutureRecord> mainOutput = new ArrayList<FutureRecord>();
		for (int real = 0; real < nbRealizations; real++) {
			FrenchHDRelationship2018Predictor pred = new FrenchHDRelationship2018Predictor();		// deterministic 

			for (ValidationOn2013DataStand stand : StandMap.values()) {
				stand.clear();
				stand.setHeightForThisNumberOfTrees(i);
			}
			
			for (ValidationOn2013DataStand stand : StandMap.values()) {
				for (FrenchHDRelationship2018Tree t : stand.getTreesForFrenchHDRelationship()) {
					ValidationOn2013DataTree tree = (ValidationOn2013DataTree) t;
					if (!tree.knownHeight) {
						double predicted = pred.predictHeightM(stand, tree);
						if (predicted > 100) {
							int u = 0;
						}
						tree.heightM = predicted;
					}
				}
			}


			Map<FrenchHd2018Species, List<Double>> biasMap = null;
			Map<FrenchHd2018Species, List<Double>> obsMap = null;
			for (ValidationOn2013DataStand stand : StandMap.values()) {
				Map<FrenchHd2018Species, List<Double>> incomingMap = stand.getDifferences();
				Map<FrenchHd2018Species, List<Double>> incomingObsMap = stand.getObservations();
				if (biasMap == null) {
					biasMap = incomingMap;
					obsMap = incomingObsMap;
				} else {
					for (FrenchHd2018Species species : incomingMap.keySet()) {
						biasMap.get(species).addAll(incomingMap.get(species));
						obsMap.get(species).addAll(incomingObsMap.get(species));
					}
				}
			}		

			List<FutureRecord> output = new ArrayList<FutureRecord>();
			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				Matrix diff = new Matrix(biasMap.get(species));
				Matrix obs = new Matrix(obsMap.get(species));
				if (diff.m_iRows != obs.m_iRows) {
					throw new InvalidParameterException("The number of observations is different from the number of differences!");
				}
				int nbObs = diff.m_iRows;
				double bias = diff.scalarMultiply(1d/nbObs).getSumOfElements();
				double rmse = Math.sqrt(diff.transpose().multiply(diff).m_afData[0][0] / nbObs);
				double meanObs = obs.scalarMultiply(1d/nbObs).getSumOfElements();
				output.add(new FutureRecord(real, species, nbObs, bias, rmse, meanObs));
			}

			mainOutput.addAll(output);
		}



		String filename = ObjectUtility.getPackagePath(getClass()) + "knownHeight_" + i + ".csv";
		filename = filename.replace("bin", "manuscripts");
		File file = new File(filename);
		CSVWriter writer = new CSVWriter(file, false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("realization"));
		fields.add(new CSVField("species"));
		fields.add(new CSVField("nbObs"));
		fields.add(new CSVField("bias"));
		fields.add(new CSVField("rmse"));
		fields.add(new CSVField("meanObs"));
		writer.setFields(fields);
		Object[] record;
		for (FutureRecord rec : mainOutput) {
			record = new Object[6];
			record[0] = rec.realization;
			record[1] = rec.species.name();
			record[2] = rec.nbObs;
			record[3] = rec.bias;
			record[4] = rec.rmse;
			record[5] = rec.meanObs;
			writer.addRecord(record);
		}
		writer.close();
	}



	public static void main(String[] args) throws IOException {
		int nbMaxReal = 1000;
		ValidationOn2013Data validator = new ValidationOn2013Data();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		System.out.println("Running height simulation without known heights...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(0,1);
		System.out.println("Running height simulation with 1 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(1,nbMaxReal);
		System.out.println("Running height simulation with 2 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(2,nbMaxReal);
		System.out.println("Running height simulation with 3 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(3,nbMaxReal);
		System.out.println("Running height simulation with 4 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(4,nbMaxReal);
		System.out.println("Running height simulation with 5 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(5,nbMaxReal);
		System.out.println("Running height simulation with 6 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(6,nbMaxReal);
		System.out.println("Running height simulation with 7 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(7,nbMaxReal);
		System.out.println("Running height simulation with 8 known height per plot...");
		validator.validateWithTheNumberOfKnownHeightsPerPlot(8,nbMaxReal);
		System.out.println("Simulations done");
	}
}
