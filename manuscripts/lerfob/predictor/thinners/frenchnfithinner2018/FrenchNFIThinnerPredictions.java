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
//				spComp,
				0, 
				2010,
				2015);
		
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
//				SpeciesComposition.BroadleavedDominated,
				0, 
				2010,
				2015);
		
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
			double slope,
			double basalAreaM2Ha,
			double stemDensityHa,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("region" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				FrenchRegion2016.AUVERGNE_RHONE_ALPES,
				basalAreaM2Ha,
				stemDensityHa,	
				slope, 
				targetSpecies,
				underManagement,
				0, 
				2010,
				2015);
		
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

	public static void main(String[] args) {
		FrenchNFIThinnerPredictions predictions = new FrenchNFIThinnerPredictions();
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.7, 778, 4, true); 
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Oak, FrenchRegion2016.PAYS_DE_LA_LOIRE, 23.7, 778, 4, true);
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, 14, true);
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 872, 33, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Fir, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 872, 33, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 872, 33, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Spruce, FrenchRegion2016.AUVERGNE_RHONE_ALPES, 28.5, 872, 33, true);

		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 749, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.MaritimePine, FrenchRegion2016.NOUVELLE_AQUITAINE, 23, 749, 14, true);
		
		predictions.predictHarvestProbabilityAgainstSlope(2011, Species.Beech, FrenchRegion2016.GRAND_EST, 24, 691, true);
		predictions.predictHarvestProbabilityAgainstRegion(2011, Species.Beech, 15, 24, 691, true);
	}
}

