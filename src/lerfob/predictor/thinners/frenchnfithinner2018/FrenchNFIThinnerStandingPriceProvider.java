/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.Species;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * The FrenchNFIThinnerStandingPriceProvider class provides the prices of the 
 * target species which are used by the FrenchNFIThinnerPredictor class to estimate 
 * the probability of harvesting.
 * @author Mathieu Fortin - June 2018
 *
 */
@SuppressWarnings("serial")
class FrenchNFIThinnerStandingPriceProvider extends REpiceaPredictor {

	class TargetSpeciesSelection implements Serializable {
		
		final Species targetSpecies;
		final int yearDate;
		
		TargetSpeciesSelection(Species targetSpecies, int yearDate) {
			this.targetSpecies = targetSpecies;
			this.yearDate = yearDate;
		}
	}
	
	
	
	final Map<Species, FrenchNFIThinnerStandingPriceProviderSubModel> subModels;
	
	final int minimumYearDate = 2006;
	final int maximumYearDate = 2016;
	
	final Map<String, Map<Integer, List<TargetSpeciesSelection>>> targetSpeciesSelectionMap;

	
	FrenchNFIThinnerStandingPriceProvider(boolean isVariabilityEnabled) {
		super(false, isVariabilityEnabled, false); // although it was a residual error, it is handled through the random effects for convenience
		subModels = new HashMap<Species, FrenchNFIThinnerStandingPriceProviderSubModel>();
		for (Species sp : Species.values()) {
			subModels.put(sp, new FrenchNFIThinnerStandingPriceProviderSubModel(isVariabilityEnabled, this));
		}
		targetSpeciesSelectionMap = new HashMap<String, Map<Integer, List<TargetSpeciesSelection>>>();
		init();
	}
			
	@Override
	protected void init() {
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "prixBoisOnf.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				if (record[1].toString().equals("total")) {
					Species sp = Species.getSpeciesFromFrenchName(record[0].toString());
					if (sp != null) {
						FrenchNFIThinnerStandingPriceProviderSubModel subModel = subModels.get(sp);
						int year = Integer.parseInt(record[2].toString());
						double value = Double.parseDouble(record[3].toString());
						subModel.observedPriceMap.put(year, value);
					}
				}
			}
			for (Species sp : Species.values()) {
				subModels.get(sp).init();	// initialize the mean and the residual variance
			}
		} catch (Exception e) {
			System.out.println("Unable to read the price of standing volume in the FrenchNFIThinnerStandingPriceProvider class!");
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	synchronized Species getTargetSpecies(FrenchNFIThinnerPlot plot, int yearDate) {
		TargetSpeciesSelection tSp = getLastTargetSpeciesSelection(plot);
		if (tSp != null & (yearDate - tSp.yearDate) <= 20) {
			return tSp.targetSpecies;
		} else {
			Map<Species, Double> volumeBySpecies = plot.getVolumeBySpecies();
			Map<Species, Double> valueBySpecies = new HashMap<Species, Double>();
			for (Species sp : volumeBySpecies.keySet()) {
				double priceByM3 = getStandingPriceForThisYear(sp, yearDate, plot.getMonteCarloRealizationId());
				valueBySpecies.put(sp, volumeBySpecies.get(sp) * priceByM3);
			}
			double priceMax = 0;
			Species speciesWithMaxValue = null; 
			for (Species sp : valueBySpecies.keySet()) {
				if (valueBySpecies.get(sp) > priceMax) {
					priceMax = valueBySpecies.get(sp);
					speciesWithMaxValue = sp;
				}
			}
			recordTargetSpeciesSelection(plot, speciesWithMaxValue, yearDate);
			return speciesWithMaxValue;
		}
	}
	
	
	private void recordTargetSpeciesSelection(FrenchNFIThinnerPlot plot, Species speciesWithMaxValue, int yearDate) {
		if (!targetSpeciesSelectionMap.containsKey(plot.getSubjectId())) {
			targetSpeciesSelectionMap.put(plot.getSubjectId(), new HashMap<Integer, List<TargetSpeciesSelection>>());
		}
		Map<Integer, List<TargetSpeciesSelection>> innerMap = targetSpeciesSelectionMap.get(plot.getSubjectId());
		if (!innerMap.containsKey(plot.getMonteCarloRealizationId())) {
			innerMap.put(plot.getMonteCarloRealizationId(), new ArrayList<TargetSpeciesSelection>());
		}
		List<TargetSpeciesSelection> tSpList = innerMap.get(plot.getMonteCarloRealizationId());
		tSpList.add(new TargetSpeciesSelection(speciesWithMaxValue, yearDate));
	}
	
	
	double getStandingPriceForThisYear(Species species, int yearDate, int monteCarloID) {
		return subModels.get(species).getStandingPriceForThisYear(yearDate, monteCarloID);
	}
	
	private TargetSpeciesSelection getLastTargetSpeciesSelection(FrenchNFIThinnerPlot plot) {
		if (targetSpeciesSelectionMap.containsKey(plot.getSubjectId())) {
			Map<Integer, List<TargetSpeciesSelection>> innerMap = targetSpeciesSelectionMap.get(plot.getSubjectId());
			if (innerMap.containsKey(plot.getMonteCarloRealizationId())) {
				List<TargetSpeciesSelection> targetSpeciesSelections = innerMap.get(plot.getMonteCarloRealizationId());
				return targetSpeciesSelections.get(targetSpeciesSelections.size() - 1);
			}
		}
		return null;
	}
	
	/**
	 * This method returns the array of the prices for standing volume. If the year is smaller than 2006, it is assumed that the
	 * price is that of 2006. If the year is larger than 2016, it is assumed that the price is that of 2016.
	 * @param species a Species enum
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @param monteCarloId the id of the Monte Carlo realization
	 * @return an array of double
	 */
	double[] getStandingPrices(Species species, int startingYear, int endingYear, int monteCarloId) {
		return subModels.get(species).getStandingPrices(startingYear, endingYear, monteCarloId);
	}

	
}
