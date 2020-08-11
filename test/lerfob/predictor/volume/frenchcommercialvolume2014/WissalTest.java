package lerfob.predictor.volume.frenchcommercialvolume2014;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.util.ObjectUtility;

public class WissalTest {

	private class Tree extends FrenchCommercialVolume2014TreeImpl {
		private final int plotID;
		private final String originalSpeciesName;

		Tree(int plotID, int treeId, String originalSpeciesName, double dbhCm, double heightM, String speciesName) {
			super(treeId, dbhCm, heightM, speciesName, 0d);
			this.plotID = plotID;
			this.originalSpeciesName = originalSpeciesName;
		}
	}
	
	private void processWissalDataSet() {
		int recordIndex = 0;
		List<Tree> trees = new ArrayList<Tree>();
		String path = ObjectUtility.getRelativePackagePath(getClass());
		Object[] record;
		CSVReader reader = null;
		try {
			reader = new CSVReader(path + "dataSet.csv");

			int plotID;
			int treeID;
			double dbhCm;
			String species;
			double htotM;
			String imputedSpecies;

			while ((record = reader.nextRecord()) != null) {
				recordIndex++;
				plotID = Integer.parseInt(record[0].toString());
				treeID = Integer.parseInt(record[1].toString());
				dbhCm = Double.parseDouble(record[2].toString());
				species = record[3].toString();
				htotM = Double.parseDouble(record[4].toString());
				imputedSpecies = record[5].toString();
				trees.add(new Tree(plotID, treeID, species, dbhCm, htotM, imputedSpecies));
			}

		} catch (Exception e) {
			System.out.println("Error while reading record " + recordIndex);
			return;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		try {
			recordIndex = 0;
			FrenchCommercialVolume2014Predictor predictor = new FrenchCommercialVolume2014Predictor(false);
			for (Tree t : trees) {
				t.pred = predictor.predictTreeCommercialVolumeDm3(t) * .001;
			}
			
			File outputFile = new File(ObjectUtility.getPackagePath(getClass()) + "outputVolume.csv");
			CSVWriter writer = new CSVWriter(outputFile, false);
			List<FormatField> fields = new ArrayList<FormatField>();
			fields.add(new CSVField("PlotID"));
			fields.add(new CSVField("TreeID"));
			fields.add(new CSVField("Species"));
			fields.add(new CSVField("dbhCm"));
			fields.add(new CSVField("htot"));
			fields.add(new CSVField("predVolM3"));
			writer.setFields(fields);
			
			for (Tree t : trees) {
				recordIndex++;
				record = new Object[6];
				record[0] = t.plotID;
				record[1] = t.getSubjectId();
				record[2] = t.originalSpeciesName;
				record[3] = t.getDbhCm();
				record[4] = t.getHeightM();
				record[5] = t.pred;
				writer.addRecord(record);
			}
			
			writer.close();
		} catch (Exception e) {
			System.out.println("Error while writing record " + recordIndex);
			return;
		}
		System.out.println("Wissal test properly ended!");
	}
	
	public static void main(String[] args) {
		WissalTest wissalTest = new  WissalTest();
		wissalTest.processWissalDataSet();
	}

}
