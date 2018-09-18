package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;
import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.standlevel.SpeciesCompositionProvider.SpeciesComposition;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class FrenchNFIThinnerPredictorTests {

	private List<FrenchNFIThinnerPlot> readPlots() {
		List<FrenchNFIThinnerPlot> plots = new ArrayList<FrenchNFIThinnerPlot>();
		
		String filename = ObjectUtility.getPackagePath(getClass()) + "testData.csv";
		
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String idp = record[0].toString();
				String targetSpeciesStr = record[1].toString();
				FrenchNFIThinnerSpecies targetSpecies = FrenchNFIThinnerSpecies.getSpeciesFromFrenchName(targetSpeciesStr);
				String forestType = record[2].toString().trim().toUpperCase();
				SpeciesComposition spComp = null;
				if (forestType.equals("RES")) {
					spComp = SpeciesComposition.ConiferDominated;
				} else if (forestType.equals("MIX")) {
					spComp = SpeciesComposition.Mixed;
				} else if (forestType.equals("FEU")) {
					spComp = SpeciesComposition.BroadleavedDominated;
				}
				if (spComp == null) {
					throw new InvalidParameterException("Impossible to determine the species composition!");
				}
				
				double slopeInclination = Double.parseDouble(record[3].toString());
				double basalAreaM2Ha = Double.parseDouble(record[4].toString());
				double stemDensityHa = Double.parseDouble(record[5].toString());
				String departmentCode = record[6].toString();
				FrenchDepartment department = FrenchDepartment.getDepartment(departmentCode);
				int underManagementInt = Integer.parseInt(record[7].toString());
				boolean underManagement = underManagementInt == 1;
				double probabilityPrivateLand = Double.parseDouble(record[8].toString());
				double pred = Double.parseDouble(record[9].toString());
				int year0 = Integer.parseInt(record[10].toString());
				int year1 = Integer.parseInt(record[11].toString());
				
				FrenchNFIThinnerPlot plot = new FrenchNFIThinnerPlotImpl(idp, 
						department.getFrenchRegion2016(), 
						basalAreaM2Ha, 
						stemDensityHa,	
						slopeInclination, 
						targetSpecies, 
						underManagement,
						probabilityPrivateLand,
						pred,
						year0,
						year1);
				
				plots.add(plot);
			}
			return plots;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
	}
	
	
	
	@Test
	public void testSASPredictions() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int nbPlots = 0;
		for (FrenchNFIThinnerPlot plot : plots) {
			FrenchNFIThinnerPlotImpl p = (FrenchNFIThinnerPlotImpl) plot;
			double actual = thinner.predictEventProbability(plot, null, p.getYear0(), p.getYear1());
			double expected = p.getPredictedProbability();
			Assert.assertEquals(expected, actual, 1E-8);
			nbPlots++;
		}
		
		Assert.assertEquals(40077, nbPlots);
		
		System.out.println("Number of plots successfully tested for FrenchNFIThinnertests: " + nbPlots);
		
	}

	@Test
	public void testBeyond2016PricesPredictionsDeterministic() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		FrenchNFIThinnerPlotImpl plot = (FrenchNFIThinnerPlotImpl) plots.get(0); // we pick the first plot
		FrenchNFIThinnerSpecies species = plot.targetSpecies;

		double[] prices = thinner.priceProvider.getStandingPrices(plot.getTargetSpecies(), 2016, 2017, plot.getMonteCarloRealizationId());

		double expectedPrice = thinner.priceProvider.subModels.get(species).getParameterEstimates().getMean().m_afData[0][0];
		
		Assert.assertEquals("Prices 2017 oak", expectedPrice, prices[0], 1E-8);
		
		System.out.println("Successful deterministic test on oak price in 2017");
	}

	@Test
	public void testBeyond2016PredictionsDeterministic() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}
		double predictedProbability = thinner.predictEventProbability(plot, null, 2016, 2017);
		Assert.assertEquals("Probability of harvesting", 0.027637913261943425, predictedProbability, 1E-8);
		
		System.out.println("Successful deterministic test on an oak plot from 2016 to 2017");
	}
	
	@Test
	public void testBeyond2016PricesPredictionsStochastic() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, true);

		FrenchNFIThinnerPlotImpl plot = (FrenchNFIThinnerPlotImpl) plots.get(0); // we pick the first plot
		FrenchNFIThinnerSpecies species = plot.targetSpecies;
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		for (int mc = 0; mc < 50000; mc++) {
			double[] prices = thinner.priceProvider.getStandingPrices(plot.getTargetSpecies(), 2016, 2017, mc);
			estimate.addRealization(new Matrix(prices));
		}
		double expectedPrice = thinner.priceProvider.subModels.get(species).getParameterEstimates().getMean().m_afData[0][0];
		double estimatedMean = estimate.getMean().m_afData[0][0];
		Assert.assertEquals("Prices 2017 oak", expectedPrice, estimatedMean, 3E-1);

		double estimatedVariance = estimate.getVariance().m_afData[0][0];
		Assert.assertTrue("Testing if variance greater than 0", estimatedVariance > 0);
		
		System.out.println("Successful stochastic test on oak price in 2017");
	}

	
	@Test
	public void testBeyond2016PredictionsStochastic() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(true, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		MonteCarloEstimate estimate = new MonteCarloEstimate();
		for (int mc = 0; mc < 50000; mc++) {
			((FrenchNFIThinnerPlotImpl) plot).monteCarloRealization = mc;
			double predictedProbability = thinner.predictEventProbability(plot, null, 2016, 2017);
			estimate.addRealization(new Matrix(new double[]{predictedProbability}));
		}
		double estimatedProbability = estimate.getMean().m_afData[0][0];
		Assert.assertEquals("Probability of harvesting", 0.027637913261943425, estimatedProbability, 5E-5);
		
		System.out.println("Successful stochastic test on an oak plot from 2016 to 2017");
	}

	@Test
	public void testNoChange2016PricePredictionsWithBasicTrend() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		double priceIn2016 = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2016, plot.getMonteCarloRealizationId());
		
		thinner.setBasicTrendModifier(2016, 2017, .5);

		double priceIn2016WithModifier = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2016, plot.getMonteCarloRealizationId());

		Assert.assertEquals("Price in 2016", priceIn2016, priceIn2016WithModifier, 1E-8);
		
		System.out.println("Successful test on basic trend modifier for year 2016");
	}

	@Test
	public void testChange2017PricePredictionsWithBasicTrend() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		double priceIn2017 = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2017, plot.getMonteCarloRealizationId());
		
		thinner.setBasicTrendModifier(2016, 2017, .5);

		double priceIn2017WithModifier = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2017, plot.getMonteCarloRealizationId());

		Assert.assertEquals("Price in 2017", priceIn2017 * 1.5, priceIn2017WithModifier, 1E-8);
		
		System.out.println("Successful test on basic trend modifier for year 2017");
	}
	
	@Test
	public void testChange2018PricePredictionsWithBasicTrend() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		double priceIn2018 = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2018, plot.getMonteCarloRealizationId());
		
		thinner.setBasicTrendModifier(2016, 2020, .5);

		double priceIn2018WithModifier = thinner.priceProvider.getStandingPriceForThisYear(((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies(), 2018, plot.getMonteCarloRealizationId());

		Assert.assertEquals("Price in 2018", priceIn2018 * 1.25, priceIn2018WithModifier, 1E-8);
		
		System.out.println("Successful test on basic trend modifier for year 2018");
	}

	@Test
	public void testChangeInPredictionsWithConstantTrendDouble() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		double unModifiedPrediction = thinner.predictEventProbability(plot, null, 2015, 2020);
		double modifiedPrediction = thinner.predictEventProbability(plot, null, 2015, 2020, .5);
		double actualDifference = modifiedPrediction - unModifiedPrediction;
		Assert.assertEquals("Probability difference with 50% increase in stumpage price",
				0.17374675683611285, 
				actualDifference,
				1E-8);
		
		System.out.println("Successful test on constant trend modifier for 2015-2020 predictions!");
	}

	
	@Test
	public void testChangeInPredictionsWithConstantTrendMatrix() {
		List<FrenchNFIThinnerPlot> plots = readPlots();
		FrenchNFIThinnerPredictor thinner = new FrenchNFIThinnerPredictor(false, false);

		int i = 0;
		FrenchNFIThinnerPlot plot = plots.get(i);
		while (!plot.getSubjectId().equals("310480")) {
			i++;
			plot = plots.get(i);
		}

		double predictionWithDouble = thinner.predictEventProbability(plot, null, 2015, 2020, .5);
		Matrix constantModifier = new Matrix(FrenchNFIThinnerSpecies.values().length, 1);
		constantModifier.m_afData[((FrenchNFIThinnerPlotImpl) plot).getTargetSpecies().ordinal()][0] = .5;
		double predictionWithMatrix = thinner.predictEventProbability(plot, null, 2015, 2020, constantModifier);
		Assert.assertEquals("Comparison modified predictions with double and matrix",
				predictionWithDouble, 
				predictionWithMatrix,
				1E-8);
		
		System.out.println("Successful test on constant trend modifier with double and matrix!");
	}

	
	
}
