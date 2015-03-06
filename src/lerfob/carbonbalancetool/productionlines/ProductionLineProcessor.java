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

import java.awt.Container;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.event.CaretEvent;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.gui.UserInterfaceableObject;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.util.REpiceaTranslator;


/**
 * The WoodProductProcessor class handles all the processing from the wood piece to a particular end product.
 * Basically, it includes an intake factor (from 0 to 1), a yield factor (from 0 to 1) and an indicator that specifies
 * whether or not the residual from this processor can be used for energy.
 * @author M. Fortin - September 2010
 */
@SuppressWarnings("deprecation")
public class ProductionLineProcessor extends AbstractProductionLineProcessor implements Serializable, 
											UserInterfaceableObject {
	
	private static final long serialVersionUID = 20101018L;

	private static ProductionLineProcessor LossProductionLineProcessor;
	
	@Deprecated
	private double averageYield;
	@Deprecated
	private boolean sentToAnotherMarket;
	
	@Deprecated
	private int selectedMarketToBeSentTo;
	@Deprecated
	private String selectedMarketToBeSentToStr;
	
	private ProductionLineProcessor fatherProcessor;

	protected Processor disposedToProcessor;
	
	@Deprecated
	private ProductionLine market;
	
	
	/**
	 * For XmlDeserialization only.
	 */
	protected ProductionLineProcessor() {
		super();
		averageYield = 1d;
		woodProductFeature = new EndUseWoodProductCarbonUnitFeature(this);
	}
	
	/**
	 * Constructor for primary processor. The processor knows the market it belongs to through the 
	 * parameter market.
	 * @param market a WoodProductMarketModel instance
	 */
	@Deprecated
	protected ProductionLineProcessor(ProductionLine market) {
		super();
		this.market = market;
		woodProductFeature = new EndUseWoodProductCarbonUnitFeature(this);
	}

	/**
	 * Constructor for which the average intake and yield would be known.
	 * @param market a WoodProductMarketModel instance
	 * @param averageIntake the average intake as a proportion
	 * @param averageYield the average yield as a proportion
	 */
	@Deprecated
	protected ProductionLineProcessor(ProductionLine market, double averageIntake, double averageYield) {
		this(market);
		setAverageIntake(averageIntake);
		setAverageYield(averageYield);
	}
	
	/**
	 * Constructor for subProcessor. This processor is the child of the fatherProcessor.
	 * @param market a WoodProductMarketModel instance
	 * @param fatherProcessor a WoodProductProcessor instance
	 */
	@Deprecated
	protected ProductionLineProcessor(ProductionLine market, ProductionLineProcessor fatherProcessor) {
		this(market);
		this.fatherProcessor = fatherProcessor;
	}

	protected void patchXmlSerializerBug() {
		if (!hasSubProcessors()) {
			this.subProcessors = new ArrayList<Processor>();
		}
	}
	
	/**
	 * This method returns the average intake (from 0 to 1) taken from the father processor.
	 * @return a double
	 */
	@Deprecated
	public double getAverageIntake() {return averageIntake;}

	@Deprecated
	protected void setAverageIntake(double intake) {
		averageIntake = intake;
		if (intake != 1d) {
			if (fatherProcessor == null) {
				throw new InvalidParameterException("There is a misunderstanding about the father processor!");
			} else {
				fatherProcessor.getSubProcessorIntakes().put(this, (int) (intake * 100));
			}
		}
	}
	

	@Deprecated
	protected void setAverageYield(double averageYield) {this.averageYield = averageYield;}
	
	/**
	 * This method identifies the landfill site processor.
	 * @return a boolean
	 */
	@Deprecated
	protected boolean isLandfillProcessor() {return (woodProductFeature instanceof LandfillCarbonUnitFeature);}

	/**
	 * This method identifies the left in forest processor.
	 * @return a boolean
	 */
	@Deprecated
	protected boolean isLeftInForestProcessor() {
		return !(woodProductFeature instanceof LandfillCarbonUnitFeature) && !(woodProductFeature instanceof EndUseWoodProductCarbonUnitFeature);
	}
	
	@Override
	public ProcessorButton getGuiInterface(Container container) {
		if (guiInterface == null) {
			guiInterface = new ProductionLineProcessorButton((SystemPanel) container, this);
		}
		return guiInterface;
	}
	

	@Deprecated
	protected double getAverageYield() {return averageYield;}		// from now on yield should always be 100%
	
	protected boolean isPrimaryProcessor() {return fatherProcessor == null;}
	
	protected boolean isFinalProcessor() {return (!hasSubProcessors() && !sentToAnotherMarket);}
	
	@Deprecated
	protected String getProductionLineToBeSentTo() {
		if (selectedMarketToBeSentToStr == null) {
			List<String> productionLineNames = getProductionLine().getManager().getProductionLineNames();
			if (selectedMarketToBeSentTo < productionLineNames.size()) {
				selectedMarketToBeSentToStr = productionLineNames.get(selectedMarketToBeSentTo);
			} else {
				selectedMarketToBeSentToStr = productionLineNames.get(0);
			}
		}
		return selectedMarketToBeSentToStr;
	}

	
	
	/**
	 * This method is recursive. It goes up until it reaches the primary processor and calculates what the initial volume
	 * was before the first transformation.
	 * @param processedVolume the volume after processing (m3)
	 * @return the initial volume before any processing (m3)
	 */
	@Deprecated
	protected double getInitialVolumeBeforeFirstTransformation(double processedVolume) {
		double yieldFactor = (double) 1 / averageYield;
		double initialVolume = processedVolume * yieldFactor;
		if (!isPrimaryProcessor()) {
			initialVolume = fatherProcessor.getInitialVolumeBeforeFirstTransformation(initialVolume);
		}
		return initialVolume;
	}

	@Deprecated
	protected boolean isSentToAnotherMarket() {return sentToAnotherMarket;}

	@SuppressWarnings({ "rawtypes"})
	@Override
	protected List<ProcessUnit> createProcessUnitsFromThisProcessor(ProcessUnit unit, int intake) {
		
		List<ProcessUnit> outputUnits = new ArrayList<ProcessUnit>();
		
		CarbonUnit carbonUnit = (CarbonUnit) unit;
		int creationDate = carbonUnit.getCreationDate();
		AmountMap<Element> processedAmountMap = carbonUnit.getAmountMap().multiplyByAScalar(intake * .01);

		CarbonUnit woodProduct;
		
		if (!isFinalProcessor()) {
			woodProduct = new CarbonUnit(creationDate, null, processedAmountMap);
			outputUnits.add(woodProduct);
			return outputUnits;
		} else {
			woodProduct = new EndUseWoodProductCarbonUnit(creationDate, 
					(EndUseWoodProductCarbonUnitFeature) woodProductFeature,
					processedAmountMap,
					getInitialVolumeBeforeFirstTransformation(processedAmountMap.get(Element.Volume)));
			woodProduct.addStatus(CarbonUnitStatus.EndUseWoodProduct);
			outputUnits.add(woodProduct);
			return outputUnits;
		}
	}
	

	/**
	 * This method returns a collection of end products that can be produced from this piece of wood. 
	 * This collection is defined by the end product features associated in the tree log category of this 
	 * piece.
	 * @return a collection of EndProduct instances (Collection) 
	 */
	@Deprecated
	protected CarbonUnitMap<CarbonUnitStatus> processWoodPiece(int creationDate, AmountMap<Element> amountMap) throws Exception {

//		int creationDate = carbonUnit.getCreationDate();
//		AmountMap<Element> amountMap = carbonUnit.getAmountMap();
		
		CarbonUnitMap<CarbonUnitStatus> outputMap = new CarbonUnitMap<CarbonUnitStatus>(CarbonUnitStatus.EndUseWoodProduct);

		CarbonUnit woodProduct;
		
		boolean somethingIsLoss = (averageYield != 1d);
		
		AmountMap<Element> processedAmountMap = amountMap.multiplyByAScalar(averageYield);
			
		if (processedAmountMap.get(Element.C) > ProductionProcessorManager.VERY_SMALL) {		// to avoid looping indefinitely
			if (isFinalProcessor()) {
//				Collection<ProcessUnit> processedUnits = this.createProcessUnitFromThisUnit(unit, intake)
				if (!isLandfillProcessor() && !isLeftInForestProcessor()) {
					woodProduct = new EndUseWoodProductCarbonUnit(
							creationDate, 
							(EndUseWoodProductCarbonUnitFeature) woodProductFeature,
							processedAmountMap,
							getInitialVolumeBeforeFirstTransformation(processedAmountMap.get(Element.Volume)));
					outputMap.get(CarbonUnitStatus.EndUseWoodProduct).add(woodProduct);

				} else if (isLandfillProcessor()) {
					LandfillCarbonUnitFeature lfcuf = (LandfillCarbonUnitFeature) woodProductFeature;
					double docf = lfcuf.getDegradableOrganicCarbonFraction();
					
					AmountMap<Element> landFillMapTmp = processedAmountMap.multiplyByAScalar(docf);
					woodProduct = new LandfillCarbonUnit(creationDate, lfcuf, landFillMapTmp);
					getProductionLine().getManager().getCarbonUnits(CarbonUnitStatus.LandFillDegradable).add((LandfillCarbonUnit) woodProduct); 
					
					landFillMapTmp = processedAmountMap.multiplyByAScalar(1 - docf);
					woodProduct = new LandfillCarbonUnit(creationDate, lfcuf, landFillMapTmp); 
					getProductionLine().getManager().getCarbonUnits(CarbonUnitStatus.LandFillNonDegradable).add((LandfillCarbonUnit) woodProduct); 
					
				} else {				// is left in the forest
					woodProduct = new CarbonUnit(creationDate, woodProductFeature, processedAmountMap);
					getProductionLine().getManager().getCarbonUnits(CarbonUnitStatus.LeftInForest).add(woodProduct);
				}

			} else if (hasSubProcessors()) {
				for (Processor subProcessor : getSubProcessors()) {
					AmountMap<Element> subProcessedMap = processedAmountMap.multiplyByAScalar(((ProductionLineProcessor) subProcessor).getAverageIntake());
					outputMap.add(((ProductionLineProcessor) subProcessor).processWoodPiece(creationDate, subProcessedMap));
				}
			} else if (isSentToAnotherMarket()) {
				outputMap.add(getProductionLine().getManager().processWoodPieceIntoThisProductionLine(getProductionLineToBeSentTo(), 
						creationDate,
						processedAmountMap));
			}
		}
		
		if (somethingIsLoss) {
			AmountMap<Element> lossAmountMap = amountMap.multiplyByAScalar(1 - averageYield);
			CarbonUnitMap<CarbonUnitStatus> tmpMap = ProductionLineProcessor.getLossProcessor().processWoodPiece(creationDate, lossAmountMap);
			for (Collection<CarbonUnit> carbonUnits : tmpMap.values()) {
				outputMap.get(CarbonUnitStatus.IndustrialLosses).addAll(carbonUnits);
			}
		}
		
		return outputMap;
	}


//	/**
//	 * This class listens to the slider in the Gui interface.
//	 */
//	@Override
//	public void stateChanged(ChangeEvent evt) {
//		if (evt.getSource().equals(formerGuiInterface.yieldSlider)) {
//			double factor = (double) 1 / formerGuiInterface.yieldSlider.getMaximum();
//			this.averageYield = formerGuiInterface.yieldSlider.getValue() * factor;
//			
//		} else if (evt.getSource().equals(formerGuiInterface.intakeSlider)) {
//			double factor = (double) 1 / formerGuiInterface.intakeSlider.getMaximum();
//			setAverageIntake(formerGuiInterface.intakeSlider.getValue() * factor);
//			
//		}
//	}
	
	
//	public ProductionLineProcessorPanel getFormerGuiInterface() {
//		if (formerGuiInterface == null) {
//			formerGuiInterface = new ProductionLineProcessorPanel(this);
//		}
//		return formerGuiInterface;
//	}
	

	/**
	 * This class listens to the text field of its Gui interface.
	 * @param evt a CaretEvent
	 */
	@Override
	public void caretUpdate(CaretEvent evt) {
//		if (evt.getSource().equals(getFormerGuiInterface().processorTextField)) {
//			setName(((JTextField) evt.getSource()).getText());
//		} else {
		super.caretUpdate(evt);
//		}
	}

	
//	@Override
//	public void itemStateChanged(ItemEvent evt) {
//		if (evt.getSource().equals(formerGuiInterface.sendToAnotherMarketCheckBox)) {
//			sentToAnotherMarket = formerGuiInterface.sendToAnotherMarketCheckBox.isSelected();
//			formerGuiInterface.checkWhatFeatureShouldBeEnabled();
//		} else if (evt.getSource().equals(formerGuiInterface.availableMarkets)) {
//			setProductionLineToBeSentTo((String) formerGuiInterface.availableMarkets.getSelectedItem());
//		}
//
//	}

	@Deprecated
	protected ProductionLine getProductionLine() {return market;}
	
	@Deprecated
	protected void setProductionLineToBeSentTo(String productionLine) {
		this.selectedMarketToBeSentToStr = productionLine;
	}
	
	@Deprecated
	protected static ProductionLineProcessor getLandfillProcessor(ProductionLineProcessor fatherProcessor, ProductionLine market) {
		ProductionLineProcessor landfillProcessor = new ProductionLineProcessor(market, fatherProcessor);
		landfillProcessor.setAverageYield(1d);
		landfillProcessor.setAverageIntake(1d);
		landfillProcessor.woodProductFeature = new LandfillCarbonUnitFeature(landfillProcessor);
		landfillProcessor.setName(REpiceaTranslator.getString(ProductionProcessorManagerDialog.MessageID.LandFillMarketLabel));
		return landfillProcessor;
	}

	@Deprecated
	protected static ProductionLineProcessor getLeftInForestProcessor(ProductionLineProcessor fatherProcessor, ProductionLine market) {
		ProductionLineProcessor leftInForestProcessor = new ProductionLineProcessor(market, fatherProcessor);
		leftInForestProcessor.setAverageYield(1d);
		leftInForestProcessor.setAverageIntake(1d);
		leftInForestProcessor.woodProductFeature = new CarbonUnitFeature(leftInForestProcessor);
		leftInForestProcessor.woodProductFeature.setAverageLifetime(10d);
		leftInForestProcessor.setName(REpiceaTranslator.getString(ProductionProcessorManagerDialog.MessageID.LeftInForestLabel));
		return leftInForestProcessor;
	}

	
	private static ProductionLineProcessor getLossProcessor() {
		if (LossProductionLineProcessor == null) {
			LossProductionLineProcessor = new ProductionLineProcessor(null, 1, 1);	// with no market
			EndUseWoodProductCarbonUnitFeature feature = (EndUseWoodProductCarbonUnitFeature) LossProductionLineProcessor.getEndProductFeature();
			feature.setAverageLifetime(0);
			feature.setAverageSubstitution(0);
			feature.setDisposable(false);
			feature.setDisposableProportion(0);
			feature.setLCA(null);
			feature.setUseClass(UseClass.NONE);
		}
		return LossProductionLineProcessor; 
	}
	
	
}
