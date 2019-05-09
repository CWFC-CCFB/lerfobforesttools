/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2019 Mathieu Fortin for the Canadian Forest Service, 
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
package lerfob.carbonbalancetool;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.math.Matrix;
import repicea.simulation.processsystem.AmountMap;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * This class contains map of different sorts which handle the results of the simulations.
 * @author Mathieu Fortin - May 2019
 */
public class CATUtilityMaps {

	/**
	 * A Map with <br>
	 * 1st key UseClass instances <br>
	 * 2nd key the species names <br>
	 * values AmountMap instances
	 * @author Mathieu Fortin - May 2019
	 */
	@SuppressWarnings("serial")
	public static class CATUseClassSpeciesAmountMap extends HashMap<UseClass, CATSpeciesAmountMap> {

		/**
		 * Combines two CATUseClassSpeciesAmountMap instances into a single one. If the keys exist in 
		 * both instances, then the AmountMap instances are summed up. Otherwise, they are copied into
		 * the resulting CATUseClassSpeciesAmountMap instance. IMPORTANT: all the AmountMap instances are 
		 * copies and as such they are NOT references to the original instances.
		 * @param otherMap a CATUseClassSpeciesAmountMap instance
		 * @return a CATUseClassSpeciesAmountMap instance
		 */
		CATUseClassSpeciesAmountMap mergeWith(CATUseClassSpeciesAmountMap otherMap) {
			CATUseClassSpeciesAmountMap outputMap = new CATUseClassSpeciesAmountMap();
			for (UseClass key : keySet()) {
				CATSpeciesAmountMap innerMap = get(key);
				if (otherMap.containsKey(key)) {
					outputMap.put(key, innerMap.mergeWith(otherMap.get(key)));
				} else {
					outputMap.put(key, innerMap);
				}
			}
			for (UseClass key : otherMap.keySet()) {
				if (!outputMap.containsKey(key)) {
					outputMap.put(key, otherMap.get(key).clone());
				}
			}
			return outputMap;
		}

		/**
		 * Sums the AmountMap instances contained in this CarbonUnitUseClassSpeciesAmountMap instance.
		 * @return an AmountMap instance
		 */
		AmountMap<Element> getSum() {
			AmountMap<Element> amountMap = new AmountMap<Element>();
			for (UseClass key : keySet()) {
				CATSpeciesAmountMap innerMap = get(key);
				amountMap.putAll(innerMap.getSum());
			}
			return amountMap;
		}
		
		void recordAsRealization(UseClassSpeciesMonteCarloEstimateMap map) {
			for (UseClass useClass : keySet()) {
				if (!map.containsKey(useClass)) {
					map.put(useClass, new SpeciesMonteCarloEstimateMap());
				}
				SpeciesMonteCarloEstimateMap innerEstimateMap = map.get(useClass);
				CATSpeciesAmountMap speciesAmountMap = get(useClass);
				speciesAmountMap.recordAsRealization(innerEstimateMap);
			}
		}
		
	}

	
	/**
	 * A Map with <br>
	 * 1st key the species names <br>
	 * values AmountMap instances
	 * @author Mathieu Fortin - May 2019
	 */
	@SuppressWarnings("serial")
	public static class CATSpeciesAmountMap extends HashMap<String, AmountMap<Element>> implements Cloneable {

		/**
		 * Combines two CATSpeciesAmountMap instances into a single one. If the keys exist in 
		 * both instances, then the AmountMap instances are summed up. Otherwise, they are copied into
		 * the resulting CATSpeciesAmountMap instance. IMPORTANT: all the AmountMap instances are 
		 * copies and as such they are NOT references to the original instances.
		 * @param otherMap a CATSpeciesAmountMap instance
		 * @return a CATSpeciesAmountMap instance
		 */
		CATSpeciesAmountMap mergeWith(CATSpeciesAmountMap otherMap) {
			if (otherMap == null) {
				throw new InvalidParameterException("The otherMap parameter cannot be null!");
			}
			CATSpeciesAmountMap outputMap = new CATSpeciesAmountMap();
			AmountMap<Element> newAmountMap;
			for (String speciesName : keySet()) {
				newAmountMap = new AmountMap<Element>();
				newAmountMap.putAll(get(speciesName));
				if (otherMap.containsKey(speciesName)) {
					newAmountMap.putAll(otherMap.get(speciesName));
				} 
				outputMap.put(speciesName, newAmountMap);
			}
			for (String speciesName : otherMap.keySet()) {
				if (!outputMap.containsKey(speciesName)) {
					newAmountMap = new AmountMap<Element>();
					newAmountMap.putAll(otherMap.get(speciesName));
					outputMap.put(speciesName, newAmountMap);
				}
			}
			return outputMap;
		}


		@Override
		public CATSpeciesAmountMap clone() {
			CATSpeciesAmountMap outputMap = new CATSpeciesAmountMap();
			for (String speciesName : keySet()) {
				outputMap.put(speciesName, get(speciesName).clone());
			}
			return outputMap;
		}
		
		/**
		 * Sums the AmountMap instances contained in this CarbonUnitUseClassSpeciesAmountMap instance.
		 * @return an AmountMap instance
		 */
		AmountMap<Element> getSum() {
			AmountMap<Element> amountMap = new AmountMap<Element>();
			for (String speciesName : keySet()) {
				AmountMap<Element> am = get(speciesName);
				amountMap.putAll(am);
			}
			return amountMap;
		}

		
		void recordAsRealization(SpeciesMonteCarloEstimateMap map) {
			for (String speciesName : keySet()) {
				if (!map.containsKey(speciesName)) {
					map.put(speciesName, new MonteCarloEstimateMap());
				}
				Map<Element, MonteCarloEstimate> innerEstimateMap = map.get(speciesName);
				AmountMap<Element> amountMap = get(speciesName);
				for (Element e : amountMap.keySet()) {
					if (!innerEstimateMap.containsKey(e)) {
						innerEstimateMap.put(e, new MonteCarloEstimate());
					}
					Matrix realization = new Matrix(1,1);
					realization.m_afData[0][0] = amountMap.get(e);
					innerEstimateMap.get(e).addRealization(realization);
				}
			}
		}
	}

	
	/**
	 * This method returns a Map with the species name as keys and the total amount map as values.
	 * An additional key is also set in the map. That is the all-species amount map.
	 * @return a Map instance
	 */
	public static CATSpeciesAmountMap convertToSpeciesMap(CarbonUnitList list) {
		CATSpeciesAmountMap outputMap = new CATSpeciesAmountMap();
		for (CarbonUnit carbonUnit : list) {
			String speciesName = carbonUnit.getSpeciesName();
//			if (!outputMap.containsKey(CarbonUnit.AllSpecies)) {
//				outputMap.put(CarbonUnit.AllSpecies, new AmountMap<Element>());
//			}
			if (!outputMap.containsKey(speciesName)) {
				outputMap.put(speciesName, new AmountMap<Element>());
			}
			AmountMap<Element> carrier = outputMap.get(speciesName);
			carrier.putAll(carbonUnit.getAmountMap());
//			AmountMap<Element> allSpeciesCarrier = outputMap.get(CarbonUnit.AllSpecies);
//			allSpeciesCarrier.putAll(carbonUnit.getAmountMap());
		}
		return outputMap;
	}

	/**
	 * A Map with <br>
	 * 1st key Element instances <br>
	 * values MonteCarloEstimate instances
	 * @author Mathieu Fortin - May 2019
	 */
	@SuppressWarnings("serial")
	public static class MonteCarloEstimateMap extends TreeMap<Element, MonteCarloEstimate> {
		
		MonteCarloEstimateMap mergeWith(MonteCarloEstimateMap otherMap) {
			if (otherMap == null) {
				throw new InvalidParameterException("The otherMap parameter cannot be null!");
			}
			MonteCarloEstimateMap outputMap = new MonteCarloEstimateMap();
			for (Element e : keySet()) {
				if (otherMap.containsKey(e)) {
					outputMap.put(e, (MonteCarloEstimate) get(e).getSumEstimate(otherMap.get(e))); 
				} else {
					outputMap.put(e, get(e));
				}
			}
			for (Element e : otherMap.keySet()) {
				if (!outputMap.containsKey(e)) {
					outputMap.put(e, otherMap.get(e));
				}
			}
			return outputMap;
		}
		
	}
	
	/**
	 * A Map with <br>
	 * 1st key the species name <br>
	 * 2nd key Element instances <br>
	 * values MonteCarloEstimate instances
	 * @author Mathieu Fortin - May 2019
	 */
	@SuppressWarnings("serial")
	public static class SpeciesMonteCarloEstimateMap extends TreeMap<String, MonteCarloEstimateMap> { 
		
		SpeciesMonteCarloEstimateMap mergeWith(SpeciesMonteCarloEstimateMap otherMap) {
			if (otherMap == null) {
				throw new InvalidParameterException("The otherMap parameter cannot be null!");
			}
			SpeciesMonteCarloEstimateMap outputMap = new SpeciesMonteCarloEstimateMap();
			for (String speciesName : keySet()) {
				if (otherMap.containsKey(speciesName)) {
					outputMap.put(speciesName, get(speciesName).mergeWith(otherMap.get(speciesName)));
				} else {
					outputMap.put(speciesName, get(speciesName));
				}
			}
			for (String speciesName : otherMap.keySet()) {
				if (!outputMap.containsKey(speciesName)) {
					outputMap.put(speciesName, otherMap.get(speciesName));
				}
			}
			return outputMap;
		}
		
		/**
		 * Returns the MonteCarloEstimateMap instance of the sum across the species.
		 * @return a MonteCarloEstimateMap instance
		 */
		public MonteCarloEstimateMap getSumAcrossSpecies() {
			MonteCarloEstimateMap mcem = new MonteCarloEstimateMap();
			for (String key : keySet()) {
				mcem = mcem.mergeWith(get(key));
			}
			return mcem;
		}
	}
	
	
	/**
	 * A Map with <br>
	 * 1st key UseClass instances <br>
	 * 2nd key the species name <br>
	 * 3rd key the Element instances <br>
	 * values MonteCarloEstimate instances
	 * @author Mathieu Fortin - May 2019
	 */
	@SuppressWarnings("serial")
	public static class UseClassSpeciesMonteCarloEstimateMap extends TreeMap<UseClass, SpeciesMonteCarloEstimateMap> { 
		
		UseClassSpeciesMonteCarloEstimateMap mergeWith(UseClassSpeciesMonteCarloEstimateMap otherMap) {
			if (otherMap == null) {
				throw new InvalidParameterException("The otherMap parameter cannot be null!");
			}
			
			UseClassSpeciesMonteCarloEstimateMap outputMap = new UseClassSpeciesMonteCarloEstimateMap();
			for (UseClass useClass : keySet()) {
				if (otherMap.containsKey(useClass)) {
					outputMap.put(useClass, get(useClass).mergeWith(otherMap.get(useClass)));
				} else {
					outputMap.put(useClass, get(useClass));
				}
			}
			for (UseClass useClass : otherMap.keySet()) {
				if (!outputMap.containsKey(useClass)) {
					outputMap.put(useClass, otherMap.get(useClass));
				}
			}
			return outputMap;
		}

		
	}
	
	
}
