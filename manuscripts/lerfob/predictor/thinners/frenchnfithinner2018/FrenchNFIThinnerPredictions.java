package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;
import lerfob.simulation.covariateproviders.standlevel.FrenchRegion2016Provider.FrenchRegion2016;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.estimates.ConfidenceInterval;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

class FrenchNFIThinnerPredictions {

	void predictHarvestProbabilityAgainstStandingPrice(int startingYear, 
			FrenchNFIThinnerSpecies targetSpecies, 
			FrenchRegion2016 region,
			double basalAreaM2Ha,
			double stemDensityHa,
			double slopeInclination,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("standingPrice" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				region,
				basalAreaM2Ha,
				stemDensityHa,	
				slopeInclination, 
				targetSpecies,
				underManagement,
				1d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false);	// no price variability
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Year"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			int year0 = startingYear;
			for (int year = startingYear + 1; year <= startingYear + 5; year++) {
				MonteCarloEstimate estimate = new MonteCarloEstimate();
				for (int real = 0; real < 10000; real++) {
					plot.monteCarloRealization = real;
					Matrix realization = new Matrix(1,1);
					realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, year0, year);
					estimate.addRealization(realization);
				}
				Object[] record = new Object[4];
				record[0] = year;
				record[1] = estimate.getMean().m_afData[0][0];
				ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				writer.addRecord(record);
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstStandingPrice!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	void predictHarvestProbabilityAgainstIncreasingStandingPriceForAllSpecies(int startingYear, 
			double basalAreaM2Ha,
			double stemDensityHa,
			double slopeInclination,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("standingPriceAllSpecies.csv");
		List<FrenchNFIThinnerPlotImpl> plots = new ArrayList<FrenchNFIThinnerPlotImpl>();
		for (FrenchNFIThinnerSpecies sp : FrenchNFIThinnerSpecies.values()) {
			FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
					FrenchRegion2016.NOUVELLE_AQUITAINE,
					basalAreaM2Ha,
					stemDensityHa,	
					slopeInclination, 
					sp,
					underManagement,
					1d); // 100% of being on private land
			plots.add(plot);
		}
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false);	// no price variability
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Species"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			int year0 = startingYear;
			int year1 = startingYear + 5;
			for (FrenchNFIThinnerPlotImpl plot : plots) {
				MonteCarloEstimate estimate = new MonteCarloEstimate();
				for (int real = 0; real < 10000; real++) {
					plot.monteCarloRealization = real;
					Matrix realization = new Matrix(1,1);
					double regPred = thinner.predictEventProbability(plot, null, year0, year1);
					double incPred = thinner.predictEventProbability(plot, null, year0, year1, .15);
					realization.m_afData[0][0] = incPred / regPred - 1d;
					estimate.addRealization(realization);
				}
				Object[] record = new Object[4];
				record[0] = plot.targetSpecies.name();
				record[1] = estimate.getMean().m_afData[0][0];
				ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				writer.addRecord(record);
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstIncreasingStandingPriceForAllSpecies!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
 
	void predictHarvestProbabilityAgainstSlope(int startingYear, 
			FrenchNFIThinnerSpecies targetSpecies, 
			FrenchRegion2016 region,
			double basalAreaM2Ha,
			double stemDensityHa,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("slope" + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				region,
				basalAreaM2Ha,
				stemDensityHa,	
				0, 
				targetSpecies,
				underManagement,
				1d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false); // no price variability
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Slope"));
			fields.add(new CSVField("Species"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			List<FrenchNFIThinnerSpecies> tSp = new ArrayList<FrenchNFIThinnerSpecies>();
			tSp.add(FrenchNFIThinnerSpecies.Beech);
			tSp.add(FrenchNFIThinnerSpecies.Fir);
			for (FrenchNFIThinnerSpecies sp : tSp) {
				plot.targetSpecies = sp;
				for (double slope = 0; slope <= 70; slope++) {
					plot.slopeInclination = slope;
					MonteCarloEstimate estimate = new MonteCarloEstimate();
					for (int real = 0; real < 10000; real++) {
						plot.monteCarloRealization = real;
						Matrix realization = new Matrix(1,1);
						realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
						estimate.addRealization(realization);
					}
					Object[] record = new Object[5];
					record[0] = slope;
					record[1] = plot.targetSpecies;
					record[2] = estimate.getMean().m_afData[0][0];
					ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
					record[3] = ci.getLowerLimit().m_afData[0][0];
					record[4] = ci.getUpperLimit().m_afData[0][0];
					writer.addRecord(record);
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstSlope!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	void predictHarvestProbabilityAgainstRegion(int startingYear, 
			FrenchNFIThinnerSpecies targetSpecies, 
			double basalAreaM2Ha,
			double stemDensityHa,
			boolean underManagement,
			double slope) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("region" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				FrenchRegion2016.AUVERGNE_RHONE_ALPES,
				basalAreaM2Ha,
				stemDensityHa,	
				slope, 
				targetSpecies,
				underManagement,
				1d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false); // no price variability
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Region"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			
			for (FrenchRegion2016 region : FrenchRegion2016.values()) {
				plot.region2016 = region;;
				MonteCarloEstimate estimate = new MonteCarloEstimate();
				for (int real = 0; real < 10000; real++) {
					plot.monteCarloRealization = real;
					Matrix realization = new Matrix(1,1);
					realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
					estimate.addRealization(realization);
				}
				Object[] record = new Object[4];
				record[0] = region.name();
				record[1] = estimate.getMean().m_afData[0][0];
				ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				writer.addRecord(record);
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstRegion!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	void predictHarvestProbabilityAgainstLandownershipAndPreviousManagement(int startingYear, 
			FrenchNFIThinnerSpecies targetSpecies, 
			FrenchRegion2016 region,
			double basalAreaM2Ha,
			double stemDensityHa,
			double slope) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("landownership" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				region,
				basalAreaM2Ha,
				stemDensityHa,	
				slope, 
				targetSpecies,
				false,
				1d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false); // no price variability
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Ownership"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			List<Integer> bools = new ArrayList<Integer>();
			bools.add(1);
			bools.add(2);
			bools.add(3);
			for (Integer bool : bools) {
				String label;
				if (bool == 1) {
					plot.probabilityPrivateLand = 0d;
					plot.interventionInPrevious5Years = true;
					label = "Public";
				} else if (bool == 2) {
					plot.probabilityPrivateLand = 1d;
					plot.interventionInPrevious5Years = false;
					label = "Private - No intervention";
				} else {
					plot.probabilityPrivateLand = 1d;
					plot.interventionInPrevious5Years = true;
					label = "Private - With intervention";
				}
				MonteCarloEstimate estimate = new MonteCarloEstimate();
				for (int real = 0; real < 10000; real++) {
					plot.monteCarloRealization = real;
					Matrix realization = new Matrix(1,1);
					realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
					estimate.addRealization(realization);
				}
				Object[] record = new Object[4];
				record[0] = label;
				record[1] = estimate.getMean().m_afData[0][0];
				ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				writer.addRecord(record);
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstLandownershipAndPreviousManagement!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	void predictHarvestProbabilityAgainstBasalAreaAndStemDensity(int startingYear, 
			FrenchNFIThinnerSpecies targetSpecies, 
			boolean underManagement,
			double slope) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("BAStemDensity" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				FrenchRegion2016.GRAND_EST,
				24,
				700,	
				slope, 
				targetSpecies,
				underManagement,
				1d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false); // no price variability
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("BasalAreaM2Ha"));
			fields.add(new CSVField("StemDensityHa"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			List<Double> densities = new ArrayList<Double>();
			densities.add(200d);
			densities.add(1200d);
			for (Double densityHa : densities) {
				plot.stemDensityHa = densityHa;
				for (double basalAreaM2Ha = 10d; basalAreaM2Ha < 40; basalAreaM2Ha++) {
					plot.basalAreaM2Ha = basalAreaM2Ha;
					MonteCarloEstimate estimate = new MonteCarloEstimate();
					for (int real = 0; real < 10000; real++) {
						plot.monteCarloRealization = real;
						Matrix realization = new Matrix(1,1);
						realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
						estimate.addRealization(realization);
					}
					Object[] record = new Object[5];
					record[0] = plot.basalAreaM2Ha;
					record[1] = plot.stemDensityHa;
					record[2] = estimate.getMean().m_afData[0][0];
					ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
					record[3] = ci.getLowerLimit().m_afData[0][0];
					record[4] = ci.getUpperLimit().m_afData[0][0];
					writer.addRecord(record);
					
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstBasalAreaAndStemDensity!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}


	public static void main(String[] args) {
		FrenchNFIThinnerPredictions predictions = new FrenchNFIThinnerPredictions();
		
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, FrenchNFIThinnerSpecies.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.6, 783, 4, false); 
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, FrenchNFIThinnerSpecies.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.6, 783, 4, false);
		
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, FrenchNFIThinnerSpecies.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, false);
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, FrenchNFIThinnerSpecies.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, false);
		
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, FrenchNFIThinnerSpecies.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, FrenchNFIThinnerSpecies.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, FrenchNFIThinnerSpecies.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, FrenchNFIThinnerSpecies.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);

//		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, FrenchNFIThinnerSpecies.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 751, 14, false);
//		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, FrenchNFIThinnerSpecies.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 751, 14, false);

		predictions.predictHarvestProbabilityAgainstIncreasingStandingPriceForAllSpecies(2011, 23, 751, 14, false);
		
//		predictions.predictHarvestProbabilityAgainstLandownershipAndPreviousManagement(2011, FrenchNFIThinnerSpecies.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14);

//		predictions.predictHarvestProbabilityAgainstRegion(2011, FrenchNFIThinnerSpecies.Beech, 24, 691, false, 14);
		
//		predictions.predictHarvestProbabilityAgainstBasalAreaAndStemDensity(2011, FrenchNFIThinnerSpecies.Beech, false, 14);

//		predictions.predictHarvestProbabilityAgainstSlope(2011, FrenchNFIThinnerSpecies.Beech, FrenchRegion2016.GRAND_EST, 24, 691, false);
		
	}
	
}

