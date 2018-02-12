package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2018.FrenchHDRelationship2018Tree.FrenchHd2018Species;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.simulation.HierarchicalLevel;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class PredictedEffects {

	static class Tree implements FrenchHDRelationship2018Tree {
		final FrenchHd2018Species species;
		double dbhCm;
		int reference;
		
		Tree(FrenchHd2018Species species, double dbhCm) {
			this.species = species;
			this.dbhCm = dbhCm;
		}
		
		@Override
		public Enum<?> getHDRelationshipTreeErrorGroup() {return null;}

		@Override
		public String getSubjectId() {return null;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public int getErrorTermIndex() {return 0;}

		@Override
		public double getHeightM() {return 0;}

		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

		@Override
		public double getSquaredLnDbhCmPlus1() {return getLnDbhCmPlus1() * getLnDbhCmPlus1();}

		@Override
		public FrenchHd2018Species getFrenchHDTreeSpecies() {
			return species;
		}
		
		
	}
	
	static class Stand implements FrenchHDRelationship2018Stand {

		private final double plotAreaHa = 15d * 15d * Math.PI * .0001;
		private double basalAreaM2Ha = 25d;
		
		private double meanTemperatureGrowingSeason = 13;
		private double meanPrecipitationGrowingSeason = 600;
		private final List<FrenchHDRelationship2018Tree> trees;
		private double meanQuadraticDiameterCm = 20d;
		private double slopeInclination = 0;
		
		Stand() {
			trees = new ArrayList<FrenchHDRelationship2018Tree>();
		}
		
		
		@Override
		public String getSubjectId() {return null;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public double getMeanQuadraticDiameterCm() {return meanQuadraticDiameterCm;}

		@Override
		public boolean isInterventionResult() {return false;}

		@Override
		public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2018Tree tree) {
			return basalAreaM2Ha - tree.getDbhCm() * tree.getDbhCm() * Math.PI * .000025 / getPlotAreaHa();
		}

		private double getPlotAreaHa() {return plotAreaHa;}

		@Override
		public double getSlopePercent() {return slopeInclination;}

		@Override
		public Collection<FrenchHDRelationship2018Tree> getTreesForFrenchHDRelationship() {return trees;}

		@Override
		public double getMeanTemperatureOfGrowingSeason() {return meanTemperatureGrowingSeason;}

		@Override
		public double getMeanPrecipitationOfGrowingSeason() {return meanPrecipitationGrowingSeason;}
		
	}
	
	
	static class Range {
		final double mean;
		final double min;
		final double max;
		Range(double mean, double min, double max) {
			this.mean = mean;
			this.min = min;
			this.max = max;
		}
	}
	
	
	

	private void testTemperatureRange() throws IOException {
		Map<FrenchHd2018Species, Range> rangeMap = new HashMap<FrenchHd2018Species, Range>();
		Object[] record;
		String filenameTempRange = ObjectUtility.getPackagePath(PredictedEffects.class) + "tempRange.csv";
		CSVReader reader = new CSVReader(filenameTempRange);
		while ((record = reader.nextRecord()) != null) {
			String speciesName = record[0].toString();
			speciesName = speciesName.replaceAll("-", " ");
			speciesName = speciesName.replaceAll("'", " ");
			FrenchHd2018Species species = FrenchHd2018Species.valueOf(speciesName.toUpperCase().replace(" ", "_"));
			double mean = Double.parseDouble(record[1].toString());
			double min = Double.parseDouble(record[2].toString());
			double max = Double.parseDouble(record[3].toString());
			Range r = new Range(mean, min, max);
			rangeMap.put(species, r);
		}
		reader.close();
		
		
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		double dbhCm = 20;
		
		Map<FrenchHd2018Species, FrenchHDRelationship2018InternalPredictor> internalPredictorMap = predictor.getInternalPredictorMap();
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class) + "EffectPredTemp.csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("basalAreaM2Ha"));
			fields.add(new CSVField("inclination"));
			fields.add(new CSVField("meanTemp"));
			fields.add(new CSVField("meanPrec"));
			fields.add(new CSVField("reference"));
			fields.add(new CSVField("pred"));
			fields.add(new CSVField("var"));
			writer.setFields(fields);

			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				if (internalPredictorMap.get(species).hasTemperatureEffect()) {
					Tree t = new Tree(species, dbhCm);
					Stand s = new Stand();
					Range r = rangeMap.get(species);
					double range = r.max - r.min;
					double meanTemperature = r.mean;
					
					s.meanTemperatureGrowingSeason = meanTemperature;
					GaussianEstimate prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
					t.reference = 1;
					writeRecord(s, t, prediction, writer);
					
					double step = range * .01;
					for (double temp = r.min; temp <= r.max; temp += step) {
						s.meanTemperatureGrowingSeason = temp;
						t.reference = 0;
						prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
						writeRecord(s, t, prediction, writer);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	
	private void testPrecipitationRange() throws IOException {
		Map<FrenchHd2018Species, Range> rangeMap = new HashMap<FrenchHd2018Species, Range>();
		Object[] record;
		String filenameTempRange = ObjectUtility.getPackagePath(PredictedEffects.class) + "precRange.csv";
		CSVReader reader = new CSVReader(filenameTempRange);
		while ((record = reader.nextRecord()) != null) {
			String speciesName = record[0].toString();
			speciesName = speciesName.replaceAll("-", " ");
			speciesName = speciesName.replaceAll("'", " ");
			FrenchHd2018Species species = FrenchHd2018Species.valueOf(speciesName.toUpperCase().replace(" ", "_"));
			double mean = Double.parseDouble(record[1].toString());
			double min = Double.parseDouble(record[2].toString());
			double max = Double.parseDouble(record[3].toString());
			Range r = new Range(mean, min, max);
			rangeMap.put(species, r);
		}
		reader.close();
		
		
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		double dbhCm = 20;
		
		Map<FrenchHd2018Species, FrenchHDRelationship2018InternalPredictor> internalPredictorMap = predictor.getInternalPredictorMap();
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class) + "EffectPredPrec.csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("basalAreaM2Ha"));
			fields.add(new CSVField("inclination"));
			fields.add(new CSVField("meanTemp"));
			fields.add(new CSVField("meanPrec"));
			fields.add(new CSVField("reference"));
			fields.add(new CSVField("pred"));
			fields.add(new CSVField("var"));
			writer.setFields(fields);

			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				if (internalPredictorMap.get(species).hasPrecipitationEffect()) {
					Tree t = new Tree(species, dbhCm);
					Stand s = new Stand();
					Range r = rangeMap.get(species);
					double range = r.max - r.min;
					double meanPrecipitation = r.mean;
					
					s.meanPrecipitationGrowingSeason = meanPrecipitation;
					GaussianEstimate prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
					t.reference = 1;
					writeRecord(s, t, prediction, writer);
					
					double step = range * .01;
					for (double prec = r.min; prec <= r.max; prec += step) {
						s.meanPrecipitationGrowingSeason = prec;
						t.reference = 0;
						prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
						writeRecord(s, t, prediction, writer);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	private void testBasalAreaRange() throws IOException {
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		double dbhCm = 20;
		
		Map<FrenchHd2018Species, FrenchHDRelationship2018InternalPredictor> internalPredictorMap = predictor.getInternalPredictorMap();
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class) + "EffectPredBasalArea.csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("basalAreaM2Ha"));
			fields.add(new CSVField("inclination"));
			fields.add(new CSVField("meanTemp"));
			fields.add(new CSVField("meanPrec"));
			fields.add(new CSVField("reference"));
			fields.add(new CSVField("pred"));
			fields.add(new CSVField("var"));
			writer.setFields(fields);

			GaussianEstimate prediction;
			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				Tree t = new Tree(species, dbhCm);
				Stand s = new Stand();
				for (double basalArea = 10; basalArea <= 40; basalArea += .5) {
					s.basalAreaM2Ha = basalArea;
					prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
					writeRecord(s, t, prediction, writer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void testSlopeInclinationRange() throws IOException {
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		double dbhCm = 20;
		
		Map<FrenchHd2018Species, FrenchHDRelationship2018InternalPredictor> internalPredictorMap = predictor.getInternalPredictorMap();
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class) + "EffectPredSlope.csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("basalAreaM2Ha"));
			fields.add(new CSVField("inclination"));
			fields.add(new CSVField("meanTemp"));
			fields.add(new CSVField("meanPrec"));
			fields.add(new CSVField("reference"));
			fields.add(new CSVField("pred"));
			fields.add(new CSVField("var"));
			writer.setFields(fields);

			GaussianEstimate prediction;
			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				Tree t = new Tree(species, dbhCm);
				Stand s = new Stand();
				for (double slope = 0; slope <= 70; slope += 1) {
					s.slopeInclination = slope;
					prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
					writeRecord(s, t, prediction, writer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	

	private static void writeRecord(Stand stand, Tree tree, GaussianEstimate prediction, CSVWriter writer) throws IOException {
		Object[] record = new Object[9];
		record[0] = tree.getFrenchHDTreeSpecies().toString();
		record[1] = tree.getDbhCm();
		record[2] = stand.basalAreaM2Ha;
		record[3] = stand.getSlopePercent();
		record[4] = stand.getMeanTemperatureOfGrowingSeason();
		record[5] = stand.getMeanPrecipitationOfGrowingSeason();
		record[6] = tree.reference;
		record[7] = prediction.getMean().m_afData[0][0];
		record[8] = prediction.getVariance().m_afData[0][0];
		// TODO add mean quadratic diameter here
		writer.addRecord(record);

	}


	public static void main(String[] args) throws IOException {
		PredictedEffects p = new PredictedEffects();
		p.testTemperatureRange();
		p.testPrecipitationRange();
		p.testBasalAreaRange();
		p.testSlopeInclinationRange();
	}


}
