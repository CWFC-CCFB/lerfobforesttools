package lerfob.windstormdamagemodels.awsmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;


public class AWSTestFileReader {
	
	/**
	 * This method reads the yield tables
	 * @param filename = the path of the file that contains the yield tables
	 * @throws Exception
	 */
	protected static Collection<AWSTestStand> instantiateTestStands (String filename) throws Exception {
		if (!new File(filename).exists()) {
			Exception e = new FileNotFoundException("File " + filename + "does not exist");
			System.out.println("Error while reading the test stands in AWS Test module : " + e.getMessage());
			throw e;
		}
		
		Collection<AWSTestStand> stands = new ArrayList<AWSTestStand>();
		
		String token;
		
		int plotId = 0;
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String str = in.readLine();
		while (str != null) {
			// comment / blank line : goes to next line
			if (!str.startsWith("#") && str.trim().length() != 0) {
				
				plotId ++;
				
				AWSTreeSpecies dominantSpecies = null;
				double d100 = -1d;
				double h100 = -1d;
				int age = -1;
				double v = -1d;
				double g = -1d;
				boolean stagnantMoisture = false;
				double topex = -1d;
				double wind50 = -1d;
				double wind99 = -1d;
				boolean carbonateInUpperSoil = false;
				int year = -1;
				
				double cumulatedRemovals = -1d;
				double relativeRemovedVolume = -1d;
				double thinningQuotient = -1d;
				double relativeRemovedVolumeOfPreviousIntervention = -1d;
				int nbYrsSincePreviousIntervention = -1;
				double relativeRemovedVolumeInPast10Yrs = -1d;
				double thinningQuotientOfPast10Yrs = -1d;

				double[] predictedProbabilities = new double[3];
				
				StringTokenizer tkz = new StringTokenizer(str, ";");
				int id = 0;
				int predId = 0;
				while (tkz.hasMoreTokens()) {
					token = tkz.nextToken();
					switch (id) {
					case 2:
						String date = token.substring(token.lastIndexOf("/") + 1);
						year = Integer.parseInt(date);
						if (year < 100) {
							year += 2000;
						}
						break;
					case 3:
						age = Integer.parseInt(token);
						break;
					case 4:
						d100 = Double.parseDouble(token);
						break;
					case 5:
						thinningQuotient = Double.parseDouble(token);
						break;
					case 6:
						h100 = Double.parseDouble(token);
						break;
					case 7:
						v = Double.parseDouble(token);
						break;
					case 8:
						g = Double.parseDouble(token);
						break;
					case 9:
						relativeRemovedVolume = Double.parseDouble(token);
						break;
					case 10:
						cumulatedRemovals = Double.parseDouble(token);
						break;
					case 11:
						relativeRemovedVolumeOfPreviousIntervention = Double.parseDouble(token);
						break;
					case 12:
						nbYrsSincePreviousIntervention = (int) Double.parseDouble(token);
						break;
					case 14:
						int caCo3 = (int) Double.parseDouble(token);
						if (caCo3 == 1) {
							carbonateInUpperSoil = true;
						} else {
							carbonateInUpperSoil = false;
						}
						break;
					case 15:
						int waterLogged = (int) Double.parseDouble(token);
						if (waterLogged == 1) {
							stagnantMoisture = true;
						} else {
							stagnantMoisture = false;
						}
						break;
					case 16:
						topex = Double.parseDouble(token);
						break;
					case 17:
						wind50 = Double.parseDouble(token);
						break;
					case 18:
						wind99 = Double.parseDouble(token);
						break;
					case 19:
						relativeRemovedVolumeInPast10Yrs = Double.parseDouble(token);
						break;
					case 20:
						thinningQuotientOfPast10Yrs = Double.parseDouble(token);
						break;
					default:
						if (id > 21) {
							try {
								predictedProbabilities[predId] = Double.parseDouble(token);
								predId++;
							} catch (Exception e) {}
						}
						String speciesName = token.trim().toLowerCase();
						if (speciesName.equals("beech")) {
							dominantSpecies = AWSTreeSpecies.Beech;
						} else if (speciesName.equals("douglasfir")) {
							dominantSpecies = AWSTreeSpecies.DouglasFir;
						} else if (speciesName.equals("oak")) {
							dominantSpecies = AWSTreeSpecies.Oak;
						} else if (speciesName.equals("spruce")) {
							dominantSpecies = AWSTreeSpecies.Spruce;
						} else if (speciesName.equals("pine+larch")) {
							dominantSpecies = AWSTreeSpecies.ScotsPine;
						} else if (speciesName.equals("silverfir")) {
							dominantSpecies = AWSTreeSpecies.SilverFir;
						} 
						break;
					}
					id++;
				}
				
				if (predId == 2) {
					predictedProbabilities[2] = predictedProbabilities[1];
					predictedProbabilities[1] = 0d;
				}
				
				AWSTestStand newStand = new AWSTestStand(((Integer) plotId).toString(),
						dominantSpecies,
						d100,
						h100,
						age,
						v,
						g,
						stagnantMoisture,
						topex,
						wind50,
						wind99,
						carbonateInUpperSoil,
						year,
						cumulatedRemovals,
						relativeRemovedVolume,
						thinningQuotient,
						relativeRemovedVolumeOfPreviousIntervention,
						nbYrsSincePreviousIntervention,
						relativeRemovedVolumeInPast10Yrs,
						thinningQuotientOfPast10Yrs,
						null,
						predictedProbabilities);
				stands.add(newStand);
			}
			str = in.readLine();
		}
		in.close();
		return stands;
	}
	
	
	protected static Collection<AWSTestStand> instantiateTestTrees(String filename) throws Exception {
		if (!new File(filename).exists()) {
			Exception e = new FileNotFoundException("File " + filename + "does not exist");
			System.out.println("Error while reading the test trees in AWS Test module : " + e.getMessage());
			throw e;
		}
		
		Collection<AWSTestStand> stands = new ArrayList<AWSTestStand>();
		
		String token;
		
		int plotId = 0;
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String str = in.readLine();
		while (str != null) {
			// comment / blank line : goes to next line
			if (!str.startsWith("#") && str.trim().length() != 0) {
				
				plotId ++;
				
				AWSTreeSpecies dominantSpecies = null;
				double d100 = -1d;
				double h100 = -1d;
				int age = -1;
				double v = -1d;
				double g = -1d;
				boolean stagnantMoisture = false;
				double topex = -1d;
				double wind50 = -1d;
				double wind99 = -1d;
				boolean carbonateInUpperSoil = false;
				int year = -1;
				
				double cumulatedRemovals = -1d;
				double relativeRemovedVolume = -1d;
				double thinningQuotient = -1d;
				double relativeRemovedVolumeOfPreviousIntervention = -1d;
				int nbYrsSincePreviousIntervention = -1;
				double relativeRemovedVolumeInPast10Yrs = -1d;
				double thinningQuotientOfPast10Yrs = -1d;
				
				double relativeTreeDiameter = -1d;
				double relativeTreeHD = -1d;
				double treePrediction = -1d;
				double regOffset = -1d;

				double[] predictedProbabilities = new double[3];
				
				StringTokenizer tkz = new StringTokenizer(str, ";");
				int id = 0;
				int predId = 0;
				while (tkz.hasMoreTokens()) {
					token = tkz.nextToken();
					switch (id) {
					case 1:
						String date = token.substring(token.lastIndexOf("/") + 1);
						year = Integer.parseInt(date);
						if (year < 100) {
							year += 2000;
						}
						break;
					case 2:
						relativeTreeDiameter = Double.parseDouble(token);
						break;
					case 3:
						age = Integer.parseInt(token);
						break;
					case 4:
						d100 = Double.parseDouble(token);
						break;
					case 5:
						thinningQuotient = Double.parseDouble(token);
						break;
					case 6:
						g = Double.parseDouble(token);
					case 7:
						h100 = Double.parseDouble(token);
						break;
					case 8:
						nbYrsSincePreviousIntervention = (int) Double.parseDouble(token);
						break;
					case 9:
						relativeRemovedVolumeOfPreviousIntervention = Double.parseDouble(token);
						break;
					case 10:
						cumulatedRemovals = Double.parseDouble(token);
						break;
					case 11:
						relativeRemovedVolume = Double.parseDouble(token);
						break;
					case 12:
						v = Double.parseDouble(token);
						break;
					case 13:
						topex = Double.parseDouble(token);
						break;
					case 14:
						wind50 = Double.parseDouble(token);
						break;
					case 15:
						wind99 = Double.parseDouble(token);
						break;
					case 16:
						int caCo3 = (int) Double.parseDouble(token);
						if (caCo3 == 1) {
							carbonateInUpperSoil = true;
						} else {
							carbonateInUpperSoil = false;
						}
						break;
					case 17:
						int waterLogged = (int) Double.parseDouble(token);
						if (waterLogged == 1) {
							stagnantMoisture = true;
						} else {
							stagnantMoisture = false;
						}
						break;
					case 19:
						String speciesName = token.trim().toLowerCase().substring(0, 2);
						if (speciesName.equals("bu")) {
							dominantSpecies = AWSTreeSpecies.Beech;
						} else if (speciesName.equals("dg")) {
							dominantSpecies = AWSTreeSpecies.DouglasFir;
						} else if (speciesName.equals("ei")) {
							dominantSpecies = AWSTreeSpecies.Oak;
						} else if (speciesName.equals("fi")) {
							dominantSpecies = AWSTreeSpecies.Spruce;
						} else if (speciesName.equals("ki")) {
							dominantSpecies = AWSTreeSpecies.ScotsPine;
						} else if (speciesName.equals("la")){
							dominantSpecies = AWSTreeSpecies.EuropeanLarch;
						} else if (speciesName.equals("ta")) {
							dominantSpecies = AWSTreeSpecies.SilverFir;
						} 
						break;
					case 20:
						relativeTreeHD = Double.parseDouble(token);
						break;
					case 22:
						relativeRemovedVolumeInPast10Yrs = Double.parseDouble(token);
						break;
					case 25:
						regOffset = Double.parseDouble(token);
						break;
					case 26:
						thinningQuotientOfPast10Yrs = Double.parseDouble(token);
						break;
					case 27:
						treePrediction = Double.parseDouble(token);
						break;
					}
					id++;
				}
				
				if (predId == 2) {
					predictedProbabilities[2] = predictedProbabilities[1];
					predictedProbabilities[1] = 0d;
				}
				
				AWSTestStand newStand = new AWSTestStand(((Integer) plotId).toString(),
						dominantSpecies,
						d100,
						h100,
						age,
						v,
						g,
						stagnantMoisture,
						topex,
						wind50,
						wind99,
						carbonateInUpperSoil,
						year,
						cumulatedRemovals,
						relativeRemovedVolume,
						thinningQuotient,
						relativeRemovedVolumeOfPreviousIntervention,
						nbYrsSincePreviousIntervention,
						relativeRemovedVolumeInPast10Yrs,
						thinningQuotientOfPast10Yrs,
						null,
						predictedProbabilities);
				newStand.addTree(new AWSTestTree(dominantSpecies, relativeTreeDiameter, relativeTreeHD, regOffset, treePrediction));
				stands.add(newStand);
			}
			str = in.readLine();
		}
		in.close();
		return stands;
		
	}
	

}
