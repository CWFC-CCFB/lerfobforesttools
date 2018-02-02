package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2018.FrenchHDRelationship2018Tree.FrenchHd2018Species;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class PredictedTemperatureEffect {

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
	
	
	

	public static void main(String[] args) throws IOException {
		Map<FrenchHd2018Species, Range> rangeMap = new HashMap<FrenchHd2018Species, Range>();
		Object[] record;
		String filenameTempRange = ObjectUtility.getPackagePath(PredictedTemperatureEffect.class) + "tempRange.csv";
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
		String filename = ObjectUtility.getPackagePath(PredictedTemperatureEffect.class) + "tempEffectPred.csv";
		filename = filename.replace("bin", "manuscripts");
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new File(filename), false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("meanTemp"));
			fields.add(new CSVField("temp"));
			fields.add(new CSVField("pred"));
			fields.add(new CSVField("var"));
			writer.setFields(fields);

			for (FrenchHd2018Species species : FrenchHd2018Species.values()) {
				GaussianEstimate estimate = internalPredictorMap.get(species).getGaussianEstimateFromTemperatureEffect();

				if (estimate != null) {
					Range r = rangeMap.get(species);
					double meanTemp = r.mean;
					double range = r.max - r.min;
					double step = range * .01;
					Matrix matT;
					for (double temp = r.min; temp <= r.max; temp += step) {
						Matrix beta = estimate.getMean();
						Matrix omega = estimate.getVariance();
						double deltaT = temp - meanTemp;
						matT = new Matrix(1, beta.m_iRows);
						matT.m_afData[0][0] = deltaT;
						if (matT.m_iCols > 1) {
							matT.m_afData[0][1] = deltaT * (temp + meanTemp); 
						}

						double logDiam = Math.log(dbhCm + 1);

						double pred = matT.multiply(beta).m_afData[0][0] * logDiam; 
						double var = matT.multiply(omega).multiply(matT.transpose()).m_afData[0][0] * logDiam * logDiam;
						record = new Object[6];
						record[0] = species.toString();
						record[1] = dbhCm;
						record[2] = meanTemp;
						record[3] = temp;
						record[4] = pred;
						record[5] = var;
						writer.addRecord(record);
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







}
