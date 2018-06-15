package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerStandingPriceProvider.Species;
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
			Species targetSpecies, 
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
				0d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
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

	void predictHarvestProbabilityAgainstSlope(int startingYear, 
			Species targetSpecies, 
			FrenchRegion2016 region,
			double basalAreaM2Ha,
			double stemDensityHa,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("slope" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				region,
				basalAreaM2Ha,
				stemDensityHa,	
				0, 
				targetSpecies,
				underManagement,
				0d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Slope"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

//			List<SpeciesComposition> compositions = new ArrayList<SpeciesComposition>();
//			compositions.add(SpeciesComposition.BroadleavedDominated);
//			compositions.add(SpeciesComposition.Mixed);
		
				for (double slope = 0; slope <= 70; slope++) {
					plot.slopeInclination = slope;
					MonteCarloEstimate estimate = new MonteCarloEstimate();
					for (int real = 0; real < 10000; real++) {
						plot.monteCarloRealization = real;
						Matrix realization = new Matrix(1,1);
						realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
						estimate.addRealization(realization);
					}
					Object[] record = new Object[4];
					record[0] = slope;
					record[1] = estimate.getMean().m_afData[0][0];
					ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
					record[2] = ci.getLowerLimit().m_afData[0][0];
					record[3] = ci.getUpperLimit().m_afData[0][0];
					writer.addRecord(record);
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
			Species targetSpecies, 
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
				0d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
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

	void predictHarvestProbabilityAgainstPreviousManagement(int startingYear, 
			Species targetSpecies, 
			FrenchRegion2016 region,
			double basalAreaM2Ha,
			double stemDensityHa,
			double slope) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("previousManagement" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				region,
				basalAreaM2Ha,
				stemDensityHa,	
				slope, 
				targetSpecies,
				true,
				0d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Intervention"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			List<Boolean> bools = new ArrayList<Boolean>();
			bools.add(true);
			bools.add(false);
			for (Boolean bool : bools) {
				plot.interventionInPrevious5Years = bool;;
				MonteCarloEstimate estimate = new MonteCarloEstimate();
				for (int real = 0; real < 10000; real++) {
					plot.monteCarloRealization = real;
					Matrix realization = new Matrix(1,1);
					realization.m_afData[0][0] = thinner.predictEventProbability(plot, null, startingYear, startingYear + 5);
					estimate.addRealization(realization);
				}
				Object[] record = new Object[4];
				record[0] = bool.toString();
				record[1] = estimate.getMean().m_afData[0][0];
				ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(.95);
				record[2] = ci.getLowerLimit().m_afData[0][0];
				record[3] = ci.getUpperLimit().m_afData[0][0];
				writer.addRecord(record);
			}
		} catch (Exception e) {
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstPreviousManagement!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	void predictHarvestProbabilityAgainstLandownership(int startingYear, 
			Species targetSpecies, 
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
				1d); // 100% of being on public land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("Ownership"));
			fields.add(new CSVField("Pred"));
			fields.add(new CSVField("Lower95"));
			fields.add(new CSVField("Upper95"));
			writer.setFields(fields);

			List<Boolean> bools = new ArrayList<Boolean>();
			bools.add(true);
			bools.add(false);
			for (Boolean bool : bools) {
				String label;
				if (bool) {
					plot.probabilityPublicLand = 1d;
					label = "Public";
				} else {
					plot.probabilityPublicLand = 0d;
					label = "Private";
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
			System.out.println("Unable to predict harvest probability for predictHarvestProbabilityAgainstLandownership!");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	void predictHarvestProbabilityAgainstBasalAreaAndStemDensity(int startingYear, 
			Species targetSpecies, 
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
				0d); // 100% of being on private land
		
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true);
		
		
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
					record[0] = plot.stemDensityHa;
					record[1] = plot.basalAreaM2Ha;
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
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.6, 783, 4, false); 
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.6, 783, 4, false);
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, false);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, false);
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 870, 33, false);

		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 751, 14, false);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 751, 14, false);
		
		predictions.predictHarvestProbabilityAgainstSlope(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, false);
		predictions.predictHarvestProbabilityAgainstRegion(2011, Species.Beech, 24, 691, false, 14);

		predictions.predictHarvestProbabilityAgainstPreviousManagement(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14);

		predictions.predictHarvestProbabilityAgainstBasalAreaAndStemDensity(2011, Species.Beech, false, 14);
	
		predictions.predictHarvestProbabilityAgainstLandownership(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14);
	}
}

