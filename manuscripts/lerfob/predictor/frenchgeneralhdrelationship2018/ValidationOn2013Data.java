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
import repicea.stats.estimates.ConfidenceInterval;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class ValidationOn2013Data {

	static class Realization {
		final int realization;
		final FrenchHd2018Species species;
		final int nbObs;
		final double bias;
		final double mse;
		final double meanObs;

		Realization(int realization, FrenchHd2018Species species, int nbObs, double bias, double mse, double meanObs) {
			this.realization = realization;
			this.species = species;
			this.nbObs = nbObs;
			this.bias = bias;
			this.mse = mse;
			this.meanObs = meanObs;
		}
	}

	static class Record {
		final FrenchHd2018Species species;
		final double meanNbObs;
		final double lowNbObs;
		final double uppNbObs;

		final double meanBias;
		final double lowBias;
		final double uppBias;

		final double meanMse;
		final double lowMse;
		final double uppMse;
		
		final double meanMeanObs;
		final double lowMeanObs;
		final double uppMeanObs;

		Record(FrenchHd2018Species species, double meanNbObs, double lowNbObs, double uppNbObs,
				double meanBias, double lowBias, double uppBias, 
				double meanMse, double lowMse, double uppMse,
				double meanMeanObs, double lowMeanObs, double uppMeanObs) {
			this.species = species;
			
			this.meanNbObs = meanNbObs;
			this.lowNbObs = lowNbObs;
			this.uppNbObs = uppNbObs;

			this.meanBias = meanBias;
			this.lowBias = lowBias;
			this.uppBias = uppBias;

			this.meanMse = meanMse;
			this.lowMse = lowMse;
			this.uppMse = uppMse;
			
			this.meanMeanObs = meanMeanObs;
			this.lowMeanObs = lowMeanObs;
			this.uppMeanObs= uppMeanObs;
			
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

		List<Realization> mainOutput = new ArrayList<Realization>();
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

			List<Realization> output = new ArrayList<Realization>();
			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				Matrix diff = new Matrix(biasMap.get(species));
				Matrix obs = new Matrix(obsMap.get(species));
				if (diff.m_iRows != obs.m_iRows) {
					throw new InvalidParameterException("The number of observations is different from the number of differences!");
				}
				int nbObs = diff.m_iRows;
				double bias = diff.scalarMultiply(1d/nbObs).getSumOfElements();
				double mse = diff.transpose().multiply(diff).m_afData[0][0] / nbObs;
				double meanObs = obs.scalarMultiply(1d/nbObs).getSumOfElements();
				output.add(new Realization(real, species, nbObs, bias, mse, meanObs));
			}
			mainOutput.addAll(output);
		}

		Map<FrenchHd2018Species, MonteCarloEstimate> nbObs = new HashMap<FrenchHd2018Species, MonteCarloEstimate>();  
		Map<FrenchHd2018Species, MonteCarloEstimate> bias = new HashMap<FrenchHd2018Species, MonteCarloEstimate>();  
		Map<FrenchHd2018Species, MonteCarloEstimate> mse = new HashMap<FrenchHd2018Species, MonteCarloEstimate>();  
		Map<FrenchHd2018Species, MonteCarloEstimate> meanObs = new HashMap<FrenchHd2018Species, MonteCarloEstimate>();  
		for (Realization rec : mainOutput) {
			recordValueInThisMap(nbObs, rec.species, (double) rec.nbObs);
			recordValueInThisMap(bias, rec.species, rec.bias);
			recordValueInThisMap(mse, rec.species, rec.mse);
			recordValueInThisMap(meanObs, rec.species, rec.meanObs);
		}

		String filename = ObjectUtility.getPackagePath(getClass()) + "knownHeight_" + i + ".csv";
		filename = filename.replace("bin", "manuscripts");
		File file = new File(filename);
		CSVWriter writer = null;
		
		try {
	        writer = new CSVWriter(file, false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			
			fields.add(new CSVField("meanNbObs"));
			fields.add(new CSVField("lowNbObs"));
			fields.add(new CSVField("uppNbObs"));
			
			fields.add(new CSVField("meanBias"));
			fields.add(new CSVField("lowBias"));
			fields.add(new CSVField("uppBias"));
			
			fields.add(new CSVField("meanMse"));
			fields.add(new CSVField("lowMse"));
			fields.add(new CSVField("uppMse"));

			fields.add(new CSVField("meanMeanObs"));
			fields.add(new CSVField("lowMeanObs"));
			fields.add(new CSVField("uppMeanObs"));
			
			writer.setFields(fields);
			
			Object[] record;
			ConfidenceInterval ci;
			MonteCarloEstimate estimate;
			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				record = new Object[13];
				record[0] = species;

				estimate = nbObs.get(species);
				if (estimate.getNumberOfRealizations() != nbRealizations) {
					throw new InvalidParameterException("The resulting number of realizations is inconsistent!");
				}
				record[1] = estimate.getMean().m_afData[0][0];
				ci = estimate.getConfidenceIntervalBounds(0.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				
				estimate = bias.get(species);
				if (estimate.getNumberOfRealizations() != nbRealizations) {
					throw new InvalidParameterException("The resulting number of realizations is inconsistent!");
				}
				record[4] = estimate.getMean().m_afData[0][0];
				ci = estimate.getConfidenceIntervalBounds(0.95);
				record[5] = ci.getLowerLimit().m_afData[0][0];
				record[6] = ci.getUpperLimit().m_afData[0][0];

				estimate = mse.get(species);
				if (estimate.getNumberOfRealizations() != nbRealizations) {
					throw new InvalidParameterException("The resulting number of realizations is inconsistent!");
				}
				record[7] = estimate.getMean().m_afData[0][0];
				ci = estimate.getConfidenceIntervalBounds(0.95);
				record[8] = ci.getLowerLimit().m_afData[0][0];
				record[9] = ci.getUpperLimit().m_afData[0][0];

				estimate = meanObs.get(species);
				if (estimate.getNumberOfRealizations() != nbRealizations) {
					throw new InvalidParameterException("The resulting number of realizations is inconsistent!");
				}
				record[10] = estimate.getMean().m_afData[0][0];
				ci = estimate.getConfidenceIntervalBounds(0.95);
				record[11] = ci.getLowerLimit().m_afData[0][0];
				record[12] = ci.getUpperLimit().m_afData[0][0];
				
				writer.addRecord(record);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}


	private void recordValueInThisMap(Map<FrenchHd2018Species, MonteCarloEstimate> map, FrenchHd2018Species species, double value) {
		if (!map.containsKey(species)) {
			map.put(species, new MonteCarloEstimate());
		}
		Matrix newFormat = new Matrix(1,1);
		newFormat.m_afData[0][0] = value;
		map.get(species).addRealization(newFormat);
	}
	
	public static void main(String[] args) throws IOException {
		int nbMaxReal = 1000;
		ValidationOn2013Data validator = new ValidationOn2013Data();
		FrenchHDRelationship2018TreeImplForTest.BlupPrediction = true;
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
