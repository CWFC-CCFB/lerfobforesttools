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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager.EnhancedMode;
import repicea.simulation.processsystem.DragGestureCreateLinkListener;
import repicea.simulation.processsystem.PreProcessorLinkLine;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;

@SuppressWarnings("serial")
public class ProductionLineProcessorButton extends AbstractProcessorButton {

	protected class DragGestureCreateEndOfLifeLinkListener extends DragGestureCreateLinkListener {

		protected DragGestureCreateEndOfLifeLinkListener(ProcessorButton button) {
			super(button);
		}
		
		@Override
		protected PreProcessorLinkLine instantiatePreLink(SystemPanel panel) {
			return new PreEndOfLifeLinkLine(panel, button);
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
	public void paint(Graphics g) {
		if (!getOwner().hasSubProcessors() && getOwner() instanceof ProductionLineProcessor) {
			setBorderColor(Color.BLUE);
			if (!isSelected()) {
				setBorderWidth(2);
			}
		} else {
			setBorderColor(Color.BLACK);
		}
		super.paint(g);
	}


}
