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
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager.EnhancedMode;
import repicea.gui.CommonGuiUtility;
import repicea.gui.Refreshable;
import repicea.gui.dnd.LocatedEvent;
import repicea.gui.popup.REpiceaPopupListener;
import repicea.gui.popup.REpiceaPopupMenu;
import repicea.simulation.processsystem.DragGestureCreateLinkListener;
import repicea.simulation.processsystem.PreProcessorLinkLine;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class ProductionLineProcessorButton extends AbstractProcessorButton implements ActionListener {

	protected class DragGestureCreateEndOfLifeLinkListener extends DragGestureCreateLinkListener {

		protected DragGestureCreateEndOfLifeLinkListener(ProcessorButton button) {
			super(button);
		}
		
		@Override
		protected PreProcessorLinkLine instantiatePreLink(SystemPanel panel) {
			return new PreEndOfLifeLinkLine(panel, button);
		}
		
		
	}
	
	
	protected class ForkOperationPopupMenu extends REpiceaPopupMenu implements Refreshable {

		ForkOperationPopupMenu() {
			super(ProductionLineProcessorButton.this, 
					ProductionLineProcessorButton.this.addDebarkerItem,
					ProductionLineProcessorButton.this.removeDebarkerItem);
		}

		@Override
		public void refreshInterface() {
			ProductionLineProcessor owner = (ProductionLineProcessor) ProductionLineProcessorButton.this.getOwner();
			ProductionLineProcessorButton.this.addDebarkerItem.setEnabled(!owner.containsForkProcessorOfThisKind(DebarkingProcessor.class));
			ProductionLineProcessorButton.this.removeDebarkerItem.setEnabled(owner.containsForkProcessorOfThisKind(DebarkingProcessor.class));
		}
		
		@Override
		public void setVisible(boolean bool) {
			refreshInterface();
			super.setVisible(bool);
		}
		
	}
	
	
	
	static enum MessageID implements TextableEnum {
		AddDebarkerMessage("Add debarking", "Ajouter \u00E9cor\u00E7age"),
		RemoveDebarkerMessage("Remove debarking", "Enlever \u00E9cor\u00E7age");

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


	protected final DragGestureRecognizer createEndOfLifeLinkRecognizer;
	
	final JMenuItem addDebarkerItem;
	final JMenuItem removeDebarkerItem;
	
	
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
		addDebarkerItem = new JMenuItem(MessageID.AddDebarkerMessage.toString());
		removeDebarkerItem = new JMenuItem(MessageID.RemoveDebarkerMessage.toString());
		setForkOperationPopupMenu();
	}
	
	void setForkOperationPopupMenu() {
		ForkOperationPopupMenu forkOperationPopupMenu = new ForkOperationPopupMenu();
		addMouseListener(new REpiceaPopupListener(forkOperationPopupMenu));
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


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(addDebarkerItem)) {
			ProductionLineProcessor parentProcessor = (ProductionLineProcessor) getOwner();
			Point upperStreamProcessorLocation = parentProcessor.getOriginalLocation();
			Point newLocation = new Point(upperStreamProcessorLocation.x + 50, upperStreamProcessorLocation.y - 50);
			DebarkingProcessor debarkingProcessor = new DebarkingProcessor();
			LocatedEvent evt = new LocatedEvent(this, newLocation);
			ExtendedSystemPanel panel = (ExtendedSystemPanel) CommonGuiUtility.getParentComponent(this, SystemPanel.class);
			panel.addLinkLine(new ForkOperationLinkLine(panel, parentProcessor, debarkingProcessor));
			panel.acceptThisObject(debarkingProcessor, evt);
		} else if (arg0.getSource().equals(removeDebarkerItem)) {
			ProductionLineProcessor parentProcessor = (ProductionLineProcessor) getOwner();
			AbstractForkOperationProcessor forkProcessor = parentProcessor.getForkProcessorOfThisKind(DebarkingProcessor.class) ;
			ExtendedSystemPanel panel = (ExtendedSystemPanel) CommonGuiUtility.getParentComponent(this, SystemPanel.class);
			panel.deleteFeature(forkProcessor.getUI());
		}
	}

	
}
