/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolProductWithRecyclingViewer extends CarbonAccountingToolProductViewer {

	protected static enum MessageID implements TextableEnum {
		Title("Harvested Wood Products Distribution with recycling", "R\u00E9partition des produits bois avec recyclage");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}

	protected CarbonAccountingToolProductWithRecyclingViewer(CarbonAssessmentToolSimulationResult summary) {
		super(summary);
	}


	@Override
	protected String getTitle() {return REpiceaTranslator.getString(MessageID.Title);}

	@Override
	protected Map<UseClass, Map<Element, MonteCarloEstimate>> getAppropriateMap() {
		Map<UseClass, Map<Element, MonteCarloEstimate>> outputMap = new TreeMap<UseClass, Map<Element, MonteCarloEstimate>>();
		Map<UseClass, Map<Element, MonteCarloEstimate>> oMapProduct = summary.getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
		Map<UseClass, Map<Element, MonteCarloEstimate>> oMapRecycling = summary.getHWPPerHaByUseClass().get(CarbonUnitStatus.Recycled);
		for (UseClass useClass : oMapProduct.keySet()) {
			Map<Element, MonteCarloEstimate> carrier = oMapProduct.get(useClass);
			Map<Element, MonteCarloEstimate> newCarrier = new HashMap<Element, MonteCarloEstimate>();
			outputMap.put(useClass, newCarrier);
			newCarrier.putAll(carrier);
		}

		for (UseClass useClass : oMapRecycling.keySet()) {
			Map<Element, MonteCarloEstimate> carrier = oMapRecycling.get(useClass);
			Map<Element, MonteCarloEstimate> newCarrier = outputMap.get(useClass);
			if (newCarrier != null) {
				for (Element element : Element.values()) {
					MonteCarloEstimate estimate1 = carrier.get(element);
					MonteCarloEstimate estimate2 = newCarrier.get(element);
					if (estimate1 != null) {
						if (estimate2 == null) {
							newCarrier.put(element, estimate1);
						} else {
							newCarrier.put(element, MonteCarloEstimate.add(estimate1, estimate2));
						}
					}
				}
			} else {
				newCarrier = new HashMap<Element, MonteCarloEstimate>();
				outputMap.put(useClass, newCarrier);
				newCarrier.putAll(carrier);
			}
		}

		return outputMap;
	}

	
}
