package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018.FrenchHDRelationship2018ClimateGenerator.FrenchHDClimateVariableMap;
import lerfob.predictor.mathilde.climate.MathildeClimatePlot;
import lerfob.predictor.mathilde.climate.MathildeClimatePredictor;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.simulation.climate.REpiceaClimateVariableMap;
import repicea.simulation.climate.REpiceaClimateVariableMap.ClimateVariable;
import repicea.util.ObjectUtility;

public class ClimateGeneratorAlteration {

	@SuppressWarnings("serial")
	static class ClimatePlotList extends ArrayList<ClimatePlotImpl> {
		
		void setDateYr(int dateYr) {
			for (ClimatePlotImpl plot : this) {
				plot.setDateYr(dateYr);
			}
		}
		
	}
	
	
	static class ClimatePlotImpl implements MathildeClimatePlot {

		final String id;
		final FrenchHDClimateVariableMap originalPoint;
		final ClimatePlotList plots;
		final List<Double> predictedSeasonalTemperature;
		
		int monteCarloId;
		int dateYr;

		ClimatePlotImpl(String id, 
				FrenchHDClimateVariableMap point, 
				ClimatePlotList plots) {
			this.id = id;
			this.originalPoint = point;
			this.plots = plots;
			plots.add(this);
			predictedSeasonalTemperature = new ArrayList<Double>();
		}
		
		void setDateYr(int dateYr) {
			this.dateYr = dateYr;
		}
		
		@Override
		public String getSubjectId() {return id;}

		@Override
		public int getMonteCarloRealizationId() {return monteCarloId;}

		@Override
		public int getDateYr() {return dateYr;}

		@Override
		public double getLatitudeDeg() {return originalPoint.yCoord;}

		@Override
		public double getLongitudeDeg() {return originalPoint.xCoord;}

		@Override
		public double getElevationM() {return 0d;}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public List<MathildeClimatePlot> getAllMathildeClimatePlots() {return (List) plots;}
		
		void recordTemperature(REpiceaClimateVariableMap temp) {
			predictedSeasonalTemperature.add(temp.get(ClimateVariable.MeanSeasonalTempC));
		}
		
		List<Double> getRecordedTemperature() {return predictedSeasonalTemperature;}
		
		Object[] getRecord() {
			Object[] record = new Object[6];
			record[0] = getSubjectId();
			Matrix mat = new Matrix(predictedSeasonalTemperature);
			record[1] = originalPoint.yCoord;
			record[2] = originalPoint.xCoord;
			record[3] = originalPoint.ser;
			record[4] = mat.getSumOfElements() / mat.m_iRows;
			record[5] = originalPoint.get(ClimateVariable.MeanSeasonalTempC);
			return record;
		}
		
	}
	
	
	ClimateGeneratorAlteration() throws Exception {
		FrenchHDRelationship2018ClimateGenerator hdClimateGen = new FrenchHDRelationship2018ClimateGenerator();
		List<FrenchHDClimateVariableMap> points = hdClimateGen.getClimatePoints();
		int i = 0;
		ClimatePlotList mcp = new ClimatePlotList();
		for (FrenchHDClimateVariableMap point : points) {
			if (i%85 == 0) {
				new ClimatePlotImpl(((Integer) i).toString(), point, mcp);
			}
			i++;
		}
		
		MathildeClimatePredictor climPred = new MathildeClimatePredictor(true);
		
		for (i = 1961; i < 1990; i = i + 5) {
			mcp.setDateYr(i);
			for (ClimatePlotImpl plot : mcp) {
				plot.recordTemperature(climPred.getClimateVariables(plot));
			}
		}

		String filename = ObjectUtility.getPackagePath(getClass()).replace("bin", "manuscripts") + "predVsObsClimate.csv";
		File outputFile = new File(filename);
		CSVWriter writer = new CSVWriter(outputFile, false);
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("Id"));
		fields.add(new CSVField("latDeg"));
		fields.add(new CSVField("longDeg"));
		fields.add(new CSVField("ser"));
		fields.add(new CSVField("pred"));
		fields.add(new CSVField("obs"));
		writer.setFields(fields);
		
		for (ClimatePlotImpl plot : mcp) {
			writer.addRecord(plot.getRecord());
		}
		writer.close();
	}
	
	public static void main(String[] args) throws Exception {
		new ClimateGeneratorAlteration();
	}
}
