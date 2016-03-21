/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.carbonbalancetool.productionlines;

import java.io.Serializable;
import java.util.Vector;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import repicea.simulation.processsystem.AmountMap;
import repicea.util.REpiceaTranslator;

/**
 * This WoodProductMarketModel class implements a series of WoodProductProcessor instances. It also has 
 * its own Gui interface.
 * @author Mathieu Fortin - October 2010
 */
@Deprecated
public class ProductionLine implements 	Serializable {
	
	private static final long serialVersionUID = 20101018;
	
	/**
	 * This member refers to the WoodProductMarketManager instance
	 * that handles all the WoodProductMarketModel. It is transient 
	 * because the WoodProductMarketManager object deserializes the
	 * WoodProductMarketModel. Otherwise, there would be a mismatch 
	 * between the current WoodProductMarketManager and the previously
	 * serialized instance.
	 */
	private transient ProductionLineManager manager;		
	private ProductionLineProcessor primaryProcessor;
	private String marketName;
	private boolean landfillSite;
	private boolean leftInForest;
	
//	private transient ProductionLinePanel guiInterface;
	
	
	/**
	 * General constructor
	 */
	protected ProductionLine(ProductionLineManager manager) {
		this.manager = manager;
		String marketNameTmp = "Unnamed0";
		int suffix = 0;
		while (manager.getProductionLineNames().contains(marketNameTmp)) {
			marketNameTmp = marketNameTmp.replace(((Integer) suffix).toString(), ((Integer) (++suffix)).toString());
		}
		marketName = marketNameTmp;
		landfillSite = false;
	}

	
	protected void setPrimaryProcessor(ProductionLineProcessor primaryProcessor) {this.primaryProcessor = primaryProcessor;}
	protected ProductionLineProcessor getPrimaryProcessor() {return primaryProcessor;}
	
	protected ProductionLineManager getManager() {return manager;}
	protected void setManager(ProductionLineManager manager) {this.manager = manager;}
	
	protected boolean isLandfillSite() {return landfillSite;}
	protected boolean isLeftInForestModel() {return leftInForest;}

	protected String getProductionLineName() {return marketName;} 
	protected void setMarketName(String marketName) {
		this.marketName = marketName;
	}
	
//	@Override
//	public ProductionLinePanel getGuiInterface() {
//		if (guiInterface == null) {
//			guiInterface = new ProductionLinePanel(this);
//		}
//		return guiInterface;
//	}

	
	protected CarbonUnitMap<CarbonUnitStatus> createCarbonUnitFromAWoodPiece(int dateIndex, AmountMap<Element> amountMap) throws Exception {
		return primaryProcessor.processWoodPiece(dateIndex, amountMap);
	}
	
	@Override
	public String toString() {return getProductionLineName();}

	
	protected static ProductionLine createLandfillSite(ProductionLineManager manager) {
		String marketName = REpiceaTranslator.getString(ProductionProcessorManagerDialog.MessageID.LandFillMarketLabel);
		ProductionLine market = new ProductionLine(manager); 
		market.landfillSite = true;
		market.marketName = marketName;
		ProductionLineProcessor processor = new ProductionLineProcessor(market, 1, 1);	// average intake = 1, average yield = 1
		market.setPrimaryProcessor(processor);
		processor.addSubProcessor(ProductionLineProcessor.getLandfillProcessor(processor, market));
		return market;
	}
	
	protected static ProductionLine createLeftInForestModel(ProductionLineManager manager) {
		String marketName = REpiceaTranslator.getString(ProductionProcessorManagerDialog.MessageID.LeftInForestLabel);
		ProductionLine market = new ProductionLine(manager); 
		market.leftInForest = true;
		market.marketName = marketName;
		ProductionLineProcessor processor = new ProductionLineProcessor(market, 1, 1);	// average intakes = 1, average yield = 1
		market.setPrimaryProcessor(processor);
		processor.addSubProcessor(ProductionLineProcessor.getLeftInForestProcessor(processor, market));
		return market;
	}

	/**
	 * This method checks if the production line is valid.
	 * @return true if valid or false otherwise
	 */
	protected boolean isValid() {
		if (getPrimaryProcessor() != null) {
			Vector<String> marketNames = getManager().getProductionLineNames();
			boolean isValid = countOccurrence(marketNames, getProductionLineName()) <= 1;
			if (isValid) {
				isValid = getPrimaryProcessor().isValid();
			}
			return isValid;
		} else  {
			return false;
		}

	}
	
	private int countOccurrence(Vector<String> referenceVector, String lookFor) {
		int occurrence = 0;
		for (String str : referenceVector) {
			if (str.equals(lookFor)) {
				occurrence++;
			}
		}
		return occurrence;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProductionLine)) {
			return false;
		} else {
			ProductionLine pl = (ProductionLine) obj;
			if (!pl.marketName.equals(marketName)) {
				return false;
			}
			if (pl.landfillSite != landfillSite) {
				return false;
			}
			if (pl.leftInForest != leftInForest) {
				return false;
			}
			if (!pl.primaryProcessor.equals(primaryProcessor)) {
				return false;
			}
		}
		return true;
	}
	
	
}
