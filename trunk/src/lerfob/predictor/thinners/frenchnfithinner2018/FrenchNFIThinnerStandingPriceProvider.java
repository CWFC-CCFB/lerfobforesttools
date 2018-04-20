package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class FrenchNFIThinnerStandingPriceProvider {

	public enum Species{
		Oak("Chene"),
		Beech("Hetre"),
		Fir("Sapin"),
		Spruce("Epicea"),
		ScotsPine("Pin sylvestre"),
		MaritimePine("Pin maritime"),
		Coppice("Taillis feuillus");
		
		private static Map<String, Species> MatchMap;
		
		private final String frenchName;
		
		Species(String frenchName) {
			this.frenchName = frenchName;
		}
		
		
		private static Map<String, Species> getMatchMap() {
			if (MatchMap == null) {
				MatchMap = new HashMap<String, Species>();
				for (Species sp : Species.values()) {
					MatchMap.put(sp.frenchName, sp);
				}
			}
			return MatchMap;
		}
		
		static Species getSpeciesFromFrenchName(String frenchName) {
			return getMatchMap().get(frenchName);
		}
	}
	
	
	private final Map<FrenchNFIThinnerStandingPriceProvider.Species, Map<Integer, Double>> priceMap;
	
	private final int minimumYearDate = 2006;
	private final int maximumYearDate = 2016;
	
	FrenchNFIThinnerStandingPriceProvider() {
		priceMap = new HashMap<FrenchNFIThinnerStandingPriceProvider.Species, Map<Integer, Double>>();
		init();
	}
	
	
	
	
	private void init() {
		priceMap.clear();
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "prixBoisOnf.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				if (record[1].toString().equals("total")) {
					Species sp = Species.getSpeciesFromFrenchName(record[0].toString());
					if (!priceMap.containsKey(sp)) {
						priceMap.put(sp, new HashMap<Integer, Double>());
					}
					Map<Integer, Double> innerMap = priceMap.get(sp);
					int year = Integer.parseInt(record[2].toString());
					double value = Double.parseDouble(record[3].toString());
					innerMap.put(year, value);
				}
			}
		} catch (IOException e) {
			System.out.println("Unable to read the price of standing volume in the FrenchNFIThinnerStandingPriceProvider class!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	
	/**
	 * This method returns the array of the prices for standing volume. If the year is smaller than 2006, it is assumed that the
	 * price is that of 2006. If the year is larger than 2016, it is assumed that the price is that of 2016.
	 * @param species a Species enum
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @return an array of double
	 */
	double[] getStandingPrices(Species species, int startingYear, int endingYear) {
		if (endingYear <= startingYear) {
			throw new InvalidParameterException("The ending year must be greater than the starting year!");
		}
		int length = endingYear - startingYear;
		double[] priceArray = new double[length];
		for (int yearIndex = 0; yearIndex < priceArray.length; yearIndex++) {
			int yearDate = startingYear + yearIndex + 1;
			if (yearDate < minimumYearDate) {
				yearDate = minimumYearDate;
			}
			if (yearDate > maximumYearDate) {
				yearDate = maximumYearDate;
			}
			priceArray[yearIndex] = priceMap.get(species).get(yearDate);
		}
		return priceArray;
	}
	
}
