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
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class PredictedEffects {

	private enum Variable {
		DBH("rangeDBH.csv", "EffectPredDBH.csv"),
		BasalArea("rangeBasalArea.csv", "EffectPredBasalArea.csv"),
		Slope("rangeSlope.csv", "EffectPredSlope.csv"),
		Temperature("rangeTemp.csv", "EffectPredTemp.csv"),
		Precipitation("rangePrec.csv", "EffectPredPrec.csv"),
		Dg("rangeDg.csv", "EffectPredDg.csv");
		
		final String inputFilename;
		final String outputFilename;
		
		Variable(String inputFilename, String outputFilename) {
			this.inputFilename = inputFilename;
			this.outputFilename = outputFilename;
		}
	}
	
	static class Tree implements FrenchHDRelationship2018Tree {
		final FrenchHd2018Species species;
		double dbhCm;
		int reference;
		
		Tree(FrenchHd2018Species species) {
			this.species = species;
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
	
	static class Stand implements FrenchHDRelationship2018ExtPlot {

		private final double plotAreaHa = 15d * 15d * Math.PI * .0001;

		private double basalAreaM2Ha;;
		private double meanTemperatureGrowingSeason;
		private double meanPrecipitationGrowingSeason;
		private double meanQuadraticDiameterCm;
		private double slopeInclination;;

		private final List<FrenchHDRelationship2018Tree> trees;

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
		public double getSlopeInclinationPercent() {return slopeInclination;}

		@Override
		public Collection<FrenchHDRelationship2018Tree> getTreesForFrenchHDRelationship() {return trees;}

		@Override
		public double getMeanTemperatureOfGrowingSeason() {return meanTemperatureGrowingSeason;}

		@Override
		public double getMeanPrecipitationOfGrowingSeason() {return meanPrecipitationGrowingSeason;}

		@Override
		public double getLatitudeDeg() {return 0;}

		@Override
		public double getLongitudeDeg() {return 0;}

		@Override
		public double getElevationM() {return 0;}
		
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
	
	
	final Map<Variable, Map<FrenchHd2018Species, Range>> rangeMap;
	
	PredictedEffects() throws IOException {
		rangeMap = new HashMap<Variable, Map<FrenchHd2018Species, Range>>();
		rangeMap.put(Variable.DBH, readVariableRange(Variable.DBH));
		rangeMap.put(Variable.BasalArea, readVariableRange(Variable.BasalArea));
		rangeMap.put(Variable.Dg, readVariableRange(Variable.Dg));
		rangeMap.put(Variable.Slope, readVariableRange(Variable.Slope));
		rangeMap.put(Variable.Temperature, readVariableRange(Variable.Temperature));
		rangeMap.put(Variable.Precipitation, readVariableRange(Variable.Precipitation));
	}
	

	private Map<FrenchHd2018Species, Range> readVariableRange(Variable var) throws IOException {
		Map<FrenchHd2018Species, Range> rangeMap = new HashMap<FrenchHd2018Species, Range>();
		Object[] record;
		String filenameTempRange = ObjectUtility.getPackagePath(PredictedEffects.class) + var.inputFilename;
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
		return rangeMap;
	}
	
	private void setStandAndTreeMeanVariables(Stand s, Tree t, Variable var) {
		FrenchHd2018Species species = t.getFrenchHDTreeSpecies();
		if (var == Variable.Dg) {
			t.dbhCm = rangeMap.get(Variable.Dg).get(species).mean;
		} else {
			t.dbhCm = rangeMap.get(Variable.DBH).get(species).mean;
		}
		s.basalAreaM2Ha = rangeMap.get(Variable.BasalArea).get(species).mean;
		s.meanQuadraticDiameterCm = rangeMap.get(Variable.Dg).get(species).mean;
		s.slopeInclination = rangeMap.get(Variable.Slope).get(species).mean;
		s.meanTemperatureGrowingSeason = rangeMap.get(Variable.Temperature).get(species).mean;
		s.meanPrecipitationGrowingSeason = rangeMap.get(Variable.Precipitation).get(species).mean;
	}
	
	private void setParticularVariable(Variable var, double value, Stand s, Tree t) {
		switch(var) {
		case DBH:
			t.dbhCm = value;
			s.meanQuadraticDiameterCm = value;
			break;
		case BasalArea:
			s.basalAreaM2Ha = value;
			break;
		case Slope:
			s.slopeInclination = value;
			break;
		case Temperature:
			s.meanTemperatureGrowingSeason = value;
			break;
		case Precipitation:
			s.meanPrecipitationGrowingSeason = value;
			break;
		case Dg:
			s.meanQuadraticDiameterCm = value;
			break;
		}
	}
	
	private void testVariable(Variable var) {
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		
		Map<FrenchHd2018Species, FrenchHDRelationship2018InternalPredictor> internalPredictorMap = predictor.getInternalPredictorMap();
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class) + var.outputFilename;
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = instantiateWriter(filename);

			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				Tree t = new Tree(species);
				Stand s = new Stand();
				setStandAndTreeMeanVariables(s, t, var);  // default value for each species
				Range r = rangeMap.get(var).get(species);
				double range = r.max - r.min;
				double meanVariable = r.mean;

				setParticularVariable(var, meanVariable, s, t);
				GaussianEstimate prediction;
				if (s.getBasalAreaM2HaMinusThisSubject(t) >= 0d) {
					prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
					t.reference = 1;
					writeRecord(s, t, prediction, writer);
				}

				double step = range * .01;
				for (double tmpVar = r.min; tmpVar <= r.max; tmpVar += step) {
					setParticularVariable(var, tmpVar, s, t);
					if (s.getBasalAreaM2HaMinusThisSubject(t) >= 0d) {
						prediction = internalPredictorMap.get(species).predictHeightAndVariance(s, t);
						t.reference = 0;
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
	
	private void writeRecord(Stand stand, Tree tree, GaussianEstimate prediction, CSVWriter writer) throws IOException {
		Object[] record = new Object[10];
		record[0] = tree.getFrenchHDTreeSpecies().toString();
		record[1] = tree.getDbhCm();
		record[2] = stand.basalAreaM2Ha;
		record[3] = stand.getMeanQuadraticDiameterCm();
		record[4] = stand.getSlopeInclinationPercent();
		record[5] = stand.getMeanTemperatureOfGrowingSeason();
		record[6] = stand.getMeanPrecipitationOfGrowingSeason();
		record[7] = tree.reference;
		record[8] = prediction.getMean().m_afData[0][0];
		record[9] = prediction.getVariance().m_afData[0][0];
		writer.addRecord(record);
	}

	private CSVWriter instantiateWriter(String filename) throws IOException {
		CSVWriter writer = new CSVWriter(new File(filename), false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("species"));
		fields.add(new CSVField("dbhCm"));
		fields.add(new CSVField("basalAreaM2Ha"));
		fields.add(new CSVField("mqdCm"));
		fields.add(new CSVField("inclination"));
		fields.add(new CSVField("meanTemp"));
		fields.add(new CSVField("meanPrec"));
		fields.add(new CSVField("reference"));
		fields.add(new CSVField("pred"));
		fields.add(new CSVField("var"));
		writer.setFields(fields);
		return writer;
	}

	public void predictVolumeChangeForTemperatureIncrease() {
		System.out.println("Running climate warming simulation...");
		FrenchHDRelationship2018PredictorTest.readTrees();
		List<FrenchHDRelationship2018ExtPlotImplForTest> Stands = FrenchHDRelationship2018PredictorTest.ExtStands;
		Map<FrenchHd2018Species, List<Double>> obsMap = new HashMap<FrenchHd2018Species, List<Double>>();
		Map<FrenchHd2018Species, List<Double>> predMap = new HashMap<FrenchHd2018Species, List<Double>>();
		for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
			obsMap.put(species, new ArrayList<Double>());
			predMap.put(species, new ArrayList<Double>());
		}
		FrenchHDRelationship2018TreeImplForTest.BlupPrediction = false;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		for (FrenchHDRelationship2018Plot stand : Stands) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImplForTest tree = (FrenchHDRelationship2018TreeImplForTest) obj;
				double cylinder = predictor.predictHeightM(stand, tree) * tree.getDbhCm() * tree.getDbhCm() * tree.weight;
				obsMap.get(tree.getFrenchHDTreeSpecies()).add(cylinder);
			}
			((FrenchHDRelationship2018ExtPlotImplForTest) stand).meanTemp = ((FrenchHDRelationship2018ExtPlotImplForTest) stand).getMeanTemperatureOfGrowingSeason() + 1.5;
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImplForTest tree = (FrenchHDRelationship2018TreeImplForTest) obj;
				double cylinder = predictor.predictHeightM(stand, tree) * tree.getDbhCm() * tree.getDbhCm() * tree.weight;
				predMap.get(tree.getFrenchHDTreeSpecies()).add(cylinder);
			}
		}
		String filename = ObjectUtility.getPackagePath(PredictedEffects.class).replace("bin", "manuscripts") + "simClimateWarming.csv";
		
		CSVWriter writer = null;
		
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("nbObs"));
			fields.add(new CSVField("before"));
			fields.add(new CSVField("after"));
			writer.setFields(fields);
			for (FrenchHd2018Species species : obsMap.keySet()) {
				List<Double> obs = obsMap.get(species);
				List<Double> pred = predMap.get(species);
				if (obs.size() != pred.size()) {
					throw new Exception(" The obsMap size does not match the predMap size for species " + species.name());
				}
				Object[] record = new Object[4];
				record[0] = species.latinName;
				record[1] = obs.size();
				Matrix mat = new Matrix(obs);
				record[2] = mat.getSumOfElements();
				mat = new Matrix(pred);
				record[3] = mat.getSumOfElements();
				writer.addRecord(record);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		System.out.println("Climate warming simulation successfully run!");
		
	}

	
	
	
	
	public static void main(String[] args) throws IOException {
		PredictedEffects p = new PredictedEffects();
		p.testVariable(Variable.DBH);
		p.testVariable(Variable.BasalArea);
		p.testVariable(Variable.Slope);
		p.testVariable(Variable.Temperature);
		p.testVariable(Variable.Precipitation);
		p.testVariable(Variable.Dg);
		p.predictVolumeChangeForTemperatureIncrease();
	}


}
