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
 * The LandfillProcessor class a specific implementation of Processor for landfill sites.
 * @author Mathieu Fortin - May 2014
 */
@SuppressWarnings("serial")
public class LandfillProcessor extends AbstractProductionLineProcessor {

	/**
	 * The LandfillProcessorButton has a specific icon for GUI.
	 * @author Mathieu Fortin - May 2014
	 */
	protected static class LandfillProcessorButton extends ProductionLineProcessorButton {

		protected LandfillProcessorButton(SystemPanel panel, AbstractProductionLineProcessor process) {
			super(panel, process);
		}
		
		@Override
		void setExtractionPopupMenu() {} // to disable fork options

	}
	
	/**
	 * Constructor.
	 */
	protected LandfillProcessor() {
		super();
		setName(ProductionProcessorManagerDialog.MessageID.LandFillMarketLabel.toString());
		woodProductFeature = new LandfillCarbonUnitFeature(this);
		isTerminal = true;
	}
	
	
	@Override
	public ProcessorButton getUI(Container container) {
		if (guiInterface == null) {
			guiInterface = new LandfillProcessorButton((SystemPanel) container, this);
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

		CarbonUnit woodProduct;

		LandfillCarbonUnitFeature lfcuf = (LandfillCarbonUnitFeature) woodProductFeature;
		double docf = lfcuf.getDegradableOrganicCarbonFraction();

		AmountMap<Element> landFillMapTmp = processedAmountMap.multiplyByAScalar(docf);
		woodProduct = new LandfillCarbonUnit(dateIndex, 
				carbonUnit.samplingUnitID, 
				lfcuf, 
				landFillMapTmp, 
				carbonUnit.getSpeciesName(), 
				carbonUnit.getBiomassType(),
				CarbonUnitStatus.LandFillDegradable);
		outputUnits.add(woodProduct);

		landFillMapTmp = processedAmountMap.multiplyByAScalar(1 - docf);
		woodProduct = new LandfillCarbonUnit(dateIndex, 
				carbonUnit.samplingUnitID, 
				lfcuf, 
				landFillMapTmp, 
				carbonUnit.getSpeciesName(),
				carbonUnit.getBiomassType(),
				CarbonUnitStatus.LandFillNonDegradable); 
		outputUnits.add(woodProduct);
		return outputUnits;
	}

	
	
}
