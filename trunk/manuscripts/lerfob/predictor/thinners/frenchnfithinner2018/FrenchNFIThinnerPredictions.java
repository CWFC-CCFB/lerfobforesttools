package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerStandingPriceProvider.Species;
import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;
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
			FrenchDepartment dep,
			double basalAreaM2Ha,
			double stemDensityHa,
			double slopeInclination,
			boolean underManagement) {
		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts").concat("standingPrice" + targetSpecies.name() + startingYear + ".csv");
		FrenchNFIThinnerPlotImpl plot = new FrenchNFIThinnerPlotImpl("plotTest", 
				dep,
				basalAreaM2Ha,
				stemDensityHa,	
				slopeInclination, 
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

	
	public static void main(String[] args) {
		FrenchNFIThinnerPredictions predictions = new FrenchNFIThinnerPredictions();
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Oak, FrenchDepartment.MEURTHE_ET_MOSELLE, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Oak, FrenchDepartment.MEURTHE_ET_MOSELLE, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Beech, FrenchDepartment.MEURTHE_ET_MOSELLE, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Beech, FrenchDepartment.MEURTHE_ET_MOSELLE, 24, 691, 14, true);
		
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Fir, FrenchDepartment.VOSGES, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Fir, FrenchDepartment.VOSGES, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.Spruce, FrenchDepartment.VOSGES, 24, 691, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.Spruce, FrenchDepartment.VOSGES, 24, 691, 14, true);

		predictions.predictHarvestProbabilityAgainstStandingPrice(2005, Species.MaritimePine, FrenchDepartment.GIRONDE, 23, 749, 14, true);
		predictions.predictHarvestProbabilityAgainstStandingPrice(2011, Species.MaritimePine, FrenchDepartment.GIRONDE, 23, 749, 14, true);

		
		
	}
}

