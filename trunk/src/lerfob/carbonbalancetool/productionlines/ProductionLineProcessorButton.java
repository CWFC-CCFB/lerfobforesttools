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
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;

import javax.swing.Box;
import javax.swing.JPanel;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager.EnhancedMode;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.simulation.processsystem.DragGestureCreateLinkListener;
import repicea.simulation.processsystem.PreProcessorLinkLine;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.ProcessorInternalDialog;
import repicea.simulation.processsystem.SystemPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class ProductionLineProcessorButton extends ProcessorButton {

	protected class DragGestureCreateEndOfLifeLinkListener extends DragGestureCreateLinkListener {

		protected DragGestureCreateEndOfLifeLinkListener(ProcessorButton button) {
			super(button);
		}
		
		@Override
		protected PreProcessorLinkLine instantiatePreLink(SystemPanel panel) {
			return new PreEndOfLifeLinkLine(panel, button);
		}
	}
	
	protected static class ProductionLineProcessorButtonDialog extends ProcessorInternalDialog {

		protected static enum MessageID implements TextableEnum {
			FunctionalUnitBiomassLabel("Dry biomass per functional unit (kg)", "Biomasse s\u00E8che de l'unit\u00E9 fonctionnelle (kg)"),
			EmissionsLabel("Emissions per functional unit (kg CO2 Eq.)", "Emission par unit\u00E9 fonctionelle (kg CO2 Eq.)");	// TODO FP check the units here

			MessageID(String englishText, String frenchText) {
				setText(englishText, frenchText);
			}
			
			@Override
			public void setText(String englishText, String frenchText) {
				REpiceaTranslator.setString(this, englishText, frenchText);
			}
			
			@Override
			public String toString() {return REpiceaTranslator.getString(this);}
		}
		
		protected JFormattedNumericField functionUnitBiomass;
		protected JFormattedNumericField emissionsByFunctionUnit;
		
		protected ProductionLineProcessorButtonDialog(Window parent, ProcessorButton callerButton) {
			super(parent, callerButton);
		}
		
		
		protected void initializeComponents() {
			super.initializeComponents();
			functionUnitBiomass = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double,
					NumberFormatFieldFactory.Range.Positive,
					false);
			functionUnitBiomass.setColumns(5);
			functionUnitBiomass.setText(((Double) getCaller().functionUnitBiomass).toString());
			emissionsByFunctionUnit = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double,
					NumberFormatFieldFactory.Range.Positive,
					false);
			emissionsByFunctionUnit.setColumns(5);
			emissionsByFunctionUnit.setText(((Double) getCaller().emissionsByFunctionalUnit).toString());
		}
		
		@Override
		protected AbstractProductionLineProcessor getCaller() {
			return (AbstractProductionLineProcessor) super.getCaller();
		}
		
		@Override
		protected JPanel createUpperPartPanel() {
			JPanel upperPartPanel = super.createUpperPartPanel();
	
			JPanel panel = UIControlManager.createSimpleHorizontalPanel(UIControlManager.getLabel(MessageID.FunctionalUnitBiomassLabel),
					functionUnitBiomass, 
					5);
			upperPartPanel.add(panel);
			upperPartPanel.add(Box.createVerticalStrut(5));

			panel = UIControlManager.createSimpleHorizontalPanel(UIControlManager.getLabel(MessageID.EmissionsLabel),
					emissionsByFunctionUnit,
					5);
			upperPartPanel.add(panel);
			upperPartPanel.add(Box.createVerticalStrut(5));

			return upperPartPanel;
		}
		
	}
	
	

	protected final DragGestureRecognizer createEndOfLifeLinkRecognizer;

	/**
	 * Constructor.
	 * @param panel a SystemPanel instance
	 * @param process the Processor instance that owns this button
	 */
	protected ProductionLineProcessorButton(SystemPanel panel, AbstractProductionLineProcessor process) {
		super(panel, process);
		DragSource ds = new DragSource();
		createEndOfLifeLinkRecognizer = ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureCreateEndOfLifeLinkListener(this));
		createEndOfLifeLinkRecognizer.setComponent(null);
	}
	
	@SuppressWarnings("rawtypes")
	protected void setDragMode(Enum mode) {
		super.setDragMode(mode);
		createEndOfLifeLinkRecognizer.setComponent(null);
		boolean isDisposed = false;
		if (getOwner() instanceof ProductionLineProcessor) {
			ProductionLineProcessor processor = (ProductionLineProcessor) getOwner();
			isDisposed = processor.disposedToProcessor != null;
		}
		if (isDisposed) {
			createLinkRecognizer.setComponent(null);	// disable the drag & drop
		} else {
			if (mode == EnhancedMode.CreateEndOfLifeLinkLine) {
				if (!getOwner().hasSubProcessors() && !getOwner().isTerminalProcessor()) {
					createEndOfLifeLinkRecognizer.setComponent(this);
				}
			}
		}
	}

	
	@Override
	public ProcessorInternalDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new ProductionLineProcessorButtonDialog((Window) parent, this);
		}
		return guiInterface;
	}

}
