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

import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.simulation.processsystem.AmountMap;
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
	protected Map<UseClass, AmountMap<Element>> getAppropriateMap() {
		Map<UseClass, AmountMap<Element>> outputMap = new TreeMap<UseClass, AmountMap<Element>>();
		Map<UseClass, AmountMap<Element>> oMapProduct = summary.getHWPContentByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
		Map<UseClass, AmountMap<Element>> oMapRecycling = summary.getHWPContentByUseClass().get(CarbonUnitStatus.Recycled);
		for (UseClass useClass : oMapProduct.keySet()) {
			AmountMap<Element> carrier = oMapProduct.get(useClass);
			AmountMap<Element> newCarrier = new AmountMap<Element>();
			outputMap.put(useClass, newCarrier);
			newCarrier.putAll(carrier);
		}

		for (UseClass useClass : oMapRecycling.keySet()) {
			AmountMap<Element> carrier = oMapRecycling.get(useClass);
			AmountMap<Element> newCarrier = outputMap.get(useClass);
			if (newCarrier != null) {
				newCarrier.putAll(carrier);
			} else {
				newCarrier = new AmountMap<Element>();
				outputMap.put(useClass, newCarrier);
				newCarrier.putAll(carrier);
			}
		}

		return outputMap;
	}

	
}
