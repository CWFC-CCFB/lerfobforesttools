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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.filechooser.FileFilter;

import lerfob.carbonbalancetool.AbstractDesigner;
import lerfob.carbonbalancetool.CATFrame;
import lerfob.carbonbalancetool.CATCompartmentManager;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.Processor;
import repicea.util.ExtendedFileFilter;

/**
 * The WoodProductMarketManager class handles one or more instances of the WoodProductMarketModel class. This class
 * has a GUI interface (see WoodProductMarketManagerDialog) which is sent to the Event Dispatch Thread when created.
 * @author Mathieu Fortin - October 2010
 */
@Deprecated
public final class ProductionLineManager extends AbstractDesigner<ProductionLine> {

	private static final long serialVersionUID = 20130127L;

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.WoodProductMarketManager", 	"lerfob.carbonbalancetool.productionlines.ProductionLineManager");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.WoodProductMarketModel", 	"lerfob.carbonbalancetool.productionlines.ProductionLine");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.WoodProductProcessor",		"lerfob.carbonbalancetool.productionlines.ProductionLineProcessor");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.MarketTreeStructure", 		"lerfob.carbonbalancetool.productionlines.ProductionLineStructure");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.LifeCycleAnalysis", 		"lerfob.carbonbalancetool.productionlines.LifeCycleAnalysis");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.LandfillCarbonUnitFeature", "lerfob.carbonbalancetool.productionlines.LandfillCarbonUnitFeature");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.LandfillCarbonUnit", 		"lerfob.carbonbalancetool.productionlines.LandfillCarbonUnit");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.EndUseWoodProductCarbonUnitFeature", "lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.EndUseWoodProductCarbonUnit", "lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnit");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.CarbonUnitList", 			"lerfob.carbonbalancetool.productionlines.CarbonUnitList");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.CarbonUnitFeature", 		"lerfob.carbonbalancetool.productionlines.CarbonUnitFeature");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.CarbonUnit", 				"lerfob.carbonbalancetool.productionlines.CarbonUnit");
		XmlSerializerChangeMonitor.registerClassNameChange("marketmodel.EndUseWoodProductCarbonUnitFeature$UseClass", "lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature$UseClass");
	}

	private static class ProductionLineFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".prl";
		
		@Override
		public boolean accept(File f) {
			if (f.getAbsolutePath().toLowerCase().trim().endsWith(extension)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getDescription() {
			return ProductionProcessorManagerDialog.MessageID.ProductionLineFileExtension.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

//	public static enum CarbonUnitType {EndUseWoodProduct, 
//		LandFillDegradable,
//		LandFillNonDegradable,
//		Recycled, 
//		LeftInForest, 
//		IndustrialLosses,
//		RecycledLosses};
	
	public final static int PROCESSED_PIECE = 0;
	public final static int LEFTINFOREST_PIECE = 1;
	protected static final ProductionLineFileFilter MyFileFilter = new ProductionLineFileFilter();
	
	private transient List<ProductionLineManagerChangeListener> listeners = new CopyOnWriteArrayList<ProductionLineManagerChangeListener>();
	
	private List<ProductionLine> marketModels;
	
	private ProductionLine landfillModel;		
	private ProductionLine leftInTheForestModel;	
	private transient CarbonUnitMap<CarbonUnitStatus> carbonUnitMap;
	

	/**
	 * General constructor.
	 */
	public ProductionLineManager() {
		marketModels = new ArrayList<ProductionLine>();
		reset();
	}
	
	/**
	 * This method adds a ProductionLineManagerChangeListener instance if it is not already contained in the
	 * listeners.
	 * @param listener a ProductionLineManagerChangeListener listener
	 */
	public void addProductionLineManagerChangeListener(ProductionLineManagerChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			listener.productionLineManagerChanged(new ProductionLineEvent(this));  // update the listener
		}
	}
	
	/**
	 * This method removes a ProductionLineManagerChangeListener instance from the listeners.
	 * @param listener a ProductionLineManagerChangeListener instance
	 */
	public void removeProductionLineManagerChangeListener(ProductionLineManagerChangeListener listener) {
		while (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	protected void fireDesignerChangeEvent() {
		ProductionLineEvent evt = new ProductionLineEvent(this);
		for (ProductionLineManagerChangeListener listener : listeners) {
			listener.productionLineManagerChanged(evt);
		}
	}
	
	/**
	 * This method reinitializes the production line manager and fire an event to listeners for
	 * eventual updating.
	 */
	public void setToDefaultValue() {
		reset();
		fireDesignerChangeEvent();
	}
	
	@Override
	public void reset() {
		setName("");
		getContent().clear();
		landfillModel = ProductionLine.createLandfillSite(this);
		addMarketModel(landfillModel);
		leftInTheForestModel = ProductionLine.createLeftInForestModel(this);
		addMarketModel(leftInTheForestModel);
		
		ProductionLine shortLived = new ProductionLine(this);
		shortLived.setMarketName(CATFrame.MessageID.ShortLived.toString());
		ProductionLineProcessor primaryProcessor = new ProductionLineProcessor(shortLived, 1d, 1d);
		primaryProcessor.setName(CATFrame.MessageID.ShortLived.toString());
		shortLived.setPrimaryProcessor(primaryProcessor);
		EndUseWoodProductCarbonUnitFeature carbonUnitFeature = (EndUseWoodProductCarbonUnitFeature) primaryProcessor.getEndProductFeature();
		carbonUnitFeature.setAverageLifetime(2d);						// default value
		carbonUnitFeature.setDisposable(false);
		carbonUnitFeature.setUseClass(UseClass.ENERGY); 
		carbonUnitFeature.setLCA(LifeCycleAnalysis.ReferenceLCA.ECOINVENT_LOGGINGHW.getLCA());
		addMarketModel(shortLived);
		
		ProductionLine longLived = new ProductionLine(this);
		longLived.setMarketName(CATFrame.MessageID.LongLived.toString());
		primaryProcessor = new ProductionLineProcessor(longLived, 1d, 1d);
		primaryProcessor.setName(CATFrame.MessageID.LongLived.toString());
		longLived.setPrimaryProcessor(primaryProcessor);
		carbonUnitFeature = (EndUseWoodProductCarbonUnitFeature) primaryProcessor.getEndProductFeature();
		carbonUnitFeature.setAverageLifetime(30d);							// default value
		carbonUnitFeature.setDisposable(true);
		carbonUnitFeature.setDisposableProportion(1d);
		carbonUnitFeature.setUseClass(UseClass.BUILDING);
		carbonUnitFeature.setLCA(LifeCycleAnalysis.ReferenceLCA.FCBA_OAKHOUSING.getLCA());
		addMarketModel(longLived);
		
		Collection<Processor> subProcessors = leftInTheForestModel.getPrimaryProcessor().getSubProcessors();
		for (Processor processor : subProcessors) {
			ProductionLineProcessor plp = (ProductionLineProcessor) processor;
			if (plp.isLeftInForestProcessor()) {
				((CarbonUnitFeature) plp.getEndProductFeature()).setAverageLifetime(10d);			// default value
			}
		}
		
		subProcessors = landfillModel.getPrimaryProcessor().getSubProcessors();
		for (Processor processor : subProcessors) {
			ProductionLineProcessor plp = (ProductionLineProcessor) processor;
			if (plp.isLandfillProcessor()) {
				LandfillCarbonUnitFeature cuf = (LandfillCarbonUnitFeature) plp.getEndProductFeature();
				cuf.setDegradableOrganicCarbonFraction(.5);
				cuf.setAverageLifetime(25d);			// default value
				plp.setAverageIntake(.1d);
			}
		}
		
		ProductionLineProcessor energyProcessor = new ProductionLineProcessor(landfillModel, landfillModel.getPrimaryProcessor());
		energyProcessor.setAverageIntake(.9);
		energyProcessor.setAverageYield(1d);
		energyProcessor.setName(UseClass.ENERGY.toString());		
		carbonUnitFeature = (EndUseWoodProductCarbonUnitFeature) energyProcessor.getEndProductFeature();
		carbonUnitFeature.setAverageLifetime(2d);
		carbonUnitFeature.setDisposable(false);
		carbonUnitFeature.setUseClass(UseClass.ENERGY); 
		carbonUnitFeature.setLCA(LifeCycleAnalysis.ReferenceLCA.ECOINVENT_LOGGINGHW.getLCA());
		landfillModel.getPrimaryProcessor().addSubProcessor(energyProcessor);
	}

	protected void addMarketModel(ProductionLine marketModel) {
		getContent().add(marketModel);
	}

	protected void removeMarketModel(ProductionLine marketModel) {
		getContent().remove(marketModel);
	}
	
	/**
	 * This method reinitializes the two collections that contains the carbon units left in the forest and those sent to the landfill.
	 */
	public void resetCarbonUnitMap() {
		getCarbonUnitMap().clear();
	}

	


//	@Override
//	public ProductionLineManagerDialog getGuiInterface(Container parent) {
//		if (guiInterface == null) {
//			guiInterface = new ProductionLineManagerDialog((Window) parent, this);
//		}
//		return guiInterface;
//	}
//
//	
//	@Override
//	public void showInterface(Window parent) {
//		if (!getGuiInterface(parent).isVisible()) {
//			getGuiInterface(parent).validateEntries();
//			getGuiInterface(parent).setVisible(true);
//		}
//	}


	
	/**
	 * The main method for this class. The different kinds of produced carbon units are stored in the carbon unit map, which is accessible
	 * through the getCarbonUnits(CarbonUnitType) method.
	 * @param marketName the production line the wood log is sent to
	 * @param creationDate the date at which the wood log is processed
	 * @param amountMap a Map which contains the amounts of the different elements
	 * @throws Exception
	 */
	public void processWoodPiece(String marketName,	int creationDate, AmountMap<Element> amountMap) throws Exception {
		CarbonUnitMap<CarbonUnitStatus> carbonUnits = processWoodPieceIntoThisProductionLine(marketName, creationDate, amountMap);
		getCarbonUnitMap().add(carbonUnits);
	}
	
	/**
	 * This method sends a log into a specific production line.
	 * @param productionLineName the production line
	 * @param creationDate the date at which the wood log is processed
	 * @param amountMap a Map which contains the amounts of the different elements
	 * @return a Collection of carbon unit that comes from this log
	 * @throws Exception
	 */
	protected CarbonUnitMap<CarbonUnitStatus> processWoodPieceIntoThisProductionLine(String productionLineName, int dateIndex, AmountMap<Element> amountMap) throws Exception {
		CarbonUnitMap<CarbonUnitStatus> carbonUnits = new CarbonUnitMap<CarbonUnitStatus>(CarbonUnitStatus.EndUseWoodProduct);
		try {
			int index = getProductionLineNames().indexOf(productionLineName);
			if (index == -1) {
				throw new Exception("This production line does not exist : " + productionLineName);
			} else {
				ProductionLine model = getContent().get(index);
				if (model.isLandfillSite()) {
					sendToTheLandfill(dateIndex, amountMap);
				} else if (model.isLeftInForestModel()) {
					leftThisPieceInTheForest(dateIndex, amountMap);
				} else {
					carbonUnits.add(model.createCarbonUnitFromAWoodPiece(dateIndex, amountMap));
				}
				return carbonUnits;
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method sends a piece of wood to the landfill site. 
	 * @param dateIndex the date at which the product was sent to the landfill
	 * @param amountMap a Map which contains the amounts of the different elements
	 * @throws Exception
	 */
	protected void sendToTheLandfill(int dateIndex, AmountMap<Element> amountMap) throws Exception {
		CarbonUnitMap<CarbonUnitStatus> carbonUnits = landfillModel.createCarbonUnitFromAWoodPiece(dateIndex, amountMap);

		for (CarbonUnitStatus type : carbonUnits.keySet()) {
			if (type != CarbonUnitStatus.EndUseWoodProduct && type != CarbonUnitStatus.IndustrialLosses) {
				if (!carbonUnits.get(type).isEmpty()) {
					throw new Exception();
				}
			}
		}
		getCarbonUnits(CarbonUnitStatus.Recycled).addAll(carbonUnits.get(CarbonUnitStatus.EndUseWoodProduct));
		getCarbonUnits(CarbonUnitStatus.RecycledLosses).addAll(carbonUnits.get(CarbonUnitStatus.IndustrialLosses));
	}

	/**
	 * This method leaves a wood piece in the forest (for instance the roots). 
	 * @param dateIndex the index of the date at which the piece was left in the forest
	 * @param amountMap a Map which contains the amounts of the different elements
	 * @throws Exception
	 */
	public void leftThisPieceInTheForest(int dateIndex, AmountMap<Element> amountMap) throws Exception {
		leftInTheForestModel.createCarbonUnitFromAWoodPiece(dateIndex, amountMap);
	}
	
	@Override
	protected void loadFrom(AbstractDesigner<ProductionLine> designer) {
		super.loadFrom(designer);
		ProductionLineManager plm = (ProductionLineManager) designer;
		setMarketModels(plm.marketModels);			// this method takes in charge the settings of the manager in the model instances
		
		for (ProductionLine model : getContent()) {
			if (model.isLandfillSite()) {
				landfillModel = model;
				break;
			}
			landfillModel = null;		// no landfill model has been found
		}

		if (landfillModel == null) {
			ProductionLine landfillSite = ProductionLine.createLandfillSite(this);
			getContent().add(0, landfillSite);
			landfillModel = landfillSite;
		}

		if (landfillModel != null && landfillModel.getPrimaryProcessor().isLandfillProcessor()) {			// former version : ensures compatibility
			ProductionLineProcessor landFillProcessor = landfillModel.getPrimaryProcessor();
			ProductionLineProcessor replacingProcessor = new ProductionLineProcessor(landfillModel, 1, 1);   // average intake = 1, average yield = 1
			replacingProcessor.addSubProcessor(landFillProcessor);
			landfillModel.setPrimaryProcessor(replacingProcessor);
		}
		

		for (ProductionLine model : getContent()) {
			if (model.isLeftInForestModel()) {
				leftInTheForestModel = model;
				break;
			}
			leftInTheForestModel = null;
		}

		if (leftInTheForestModel == null) {
			ProductionLine leftInForestModel = ProductionLine.createLeftInForestModel(this);
			getContent().add(1, leftInForestModel);
		}

	}
	
	@Override
	public List<ProductionLine> getContent() {return marketModels;}
	
	/**
	 * This method provides the names of the different production lines.
	 * @return a vector of String objects, each string representing a production line
	 */
	public Vector<String> getProductionLineNames() {
		Vector<String> productionLineNames = new Vector<String>();
		for (ProductionLine model : getContent()) {
			productionLineNames.add(model.getProductionLineName());
		}
		return productionLineNames;
	}

	/**
	 * This method actualizes the different carbon units. It proceeds in the following order : </br>
	 * &nbsp	1- the carbon units left in the forest</br>
	 * &nbsp	2- the carbon units in the wood products</br>
	 * &nbsp	3- the carbon units at the landfill site</br>
	 * &nbsp	4- the carbon units recycled from the disposed wood products</br>
	 * @param timeScale is an Array of integer that indicates the years of actualization
	 * @throws Exception
	 */
	public void actualizeCarbonUnits(CATCompartmentManager compartmentManager) throws Exception {
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.DeadWood, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.EndUseWoodProduct, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.LandFillDegradable, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.Recycled, compartmentManager);
	}
	
	private void actualizeCarbonUnitsOfThisType(CarbonUnitStatus type, CATCompartmentManager compartmentManager) throws Exception {
		try {
			for (CarbonUnit carbonUnit : getCarbonUnits(type)) {
				carbonUnit.actualizeCarbon(compartmentManager);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("An exception occurred while actualizing carbon units of type " + type.name() + " : " + e.getMessage());
		}
	}


	private CarbonUnitMap<CarbonUnitStatus> getCarbonUnitMap() {
		if (carbonUnitMap == null) {
			carbonUnitMap = new CarbonUnitMap<CarbonUnitStatus>(CarbonUnitStatus.EndUseWoodProduct);
		}
		return carbonUnitMap;
	}
	
	/**
	 * This method returns the CarbonUnitList instance that match the type of carbon.
	 * @param type a CarbonUnitType enum (EndUseWoodProduct, Landfille, Recycled, LeftInForest)
	 * @return a CarbonUnitList instance
	 */
	public CarbonUnitList getCarbonUnits(CarbonUnitStatus type) {
		return getCarbonUnitMap().get(type);
	}
 	

	private void setMarketModels(List<ProductionLine> marketModels) {
		this.marketModels = marketModels;
		for (ProductionLine model : this.marketModels) {
			model.setManager(this);
		}
	}
	
	

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProductionLineManager)) {
			return false;
		} else {
			ProductionLineManager plm = (ProductionLineManager) obj;
			if (!plm.getName().equals(getName())) {
				return false;
			}
			if (plm.marketModels.size() != marketModels.size()) {
				return false;
			}
			for (int i = 0; i < plm.marketModels.size(); i++) {
				if (!plm.marketModels.get(i).equals(marketModels.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isValid() {
		for (ProductionLine pl : marketModels) {
			if (!pl.isValid()) {
				return false;
			}
		}
		return true;
	}
	

}