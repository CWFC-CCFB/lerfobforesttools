/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
import java.util.ArrayList;
import java.util.List;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;

/**
 * The LeftInForestProcessor class is a specific implementation of Processor for
 * dead organic matter left in the forest.
 * @author Mathieu Fortin - May 2014
 */
@SuppressWarnings("serial")
public class LeftInForestProcessor extends AbstractProductionLineProcessor {

	/**
	 * The LeftInForestProcessorButton class is the button with a particular icon for better
	 * identification in the GUI
	 * @author Mathieu Fortin - May 2014
	 */
	protected static class LeftInForestProcessorButton extends ProductionLineProcessorButton {

		protected LeftInForestProcessorButton(SystemPanel panel, AbstractProductionLineProcessor process) {
			super(panel, process);
		}
		
		@Override
		void setExtractionPopupMenu() {} // to disable the fork operations
	}
	
	
	/**
	 * Constructor.
	 */
	protected LeftInForestProcessor() {
		super();
		setName(ProductionProcessorManagerDialog.MessageID.LeftInForestLabel.toString());
		woodProductFeature = new CarbonUnitFeature(this);
		isTerminal = true;
	}
	
	
	@Override
	public ProcessorButton getUI(Container container) {
		if (guiInterface == null) {
			guiInterface = new LeftInForestProcessorButton((SystemPanel) container, this);
		}
		return guiInterface;
	}

	
	@SuppressWarnings({ "rawtypes"})
	@Override
	protected List<ProcessUnit> createProcessUnitsFromThisProcessor(ProcessUnit unit, int intake) {
		
		List<ProcessUnit> outputUnits = new ArrayList<ProcessUnit>();
		
		CarbonUnit carbonUnit = (CarbonUnit) unit;
		int dateIndex = carbonUnit.getIndexInTimeScale();
		AmountMap<Element> processedAmountMap = carbonUnit.getAmountMap().multiplyByAScalar(intake * .01);

		CarbonUnit woodProduct =  new CarbonUnit(dateIndex, 
				carbonUnit.samplingUnitID, 
				woodProductFeature, 
				processedAmountMap, 
				carbonUnit.getSpeciesName());
		woodProduct.addStatus(CarbonUnitStatus.DeadWood);
		outputUnits.add(woodProduct);
		return outputUnits;
	}

	
}
