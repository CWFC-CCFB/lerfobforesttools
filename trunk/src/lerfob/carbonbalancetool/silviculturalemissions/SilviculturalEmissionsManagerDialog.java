/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB
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
package lerfob.carbonbalancetool.silviculturalemissions;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JSplitPane;

import lerfob.carbonbalancetool.detailedlci.DetailedLCI;
import lerfob.carbonbalancetool.detailedlci.DetailedLCIManager;
import repicea.gui.REpiceaDialog;
import repicea.gui.dnd.DnDPanel;
import repicea.gui.dnd.DragGestureImpl;

@SuppressWarnings("serial")
public class SilviculturalEmissionsManagerDialog extends REpiceaDialog {

	protected static class InternalDragGestureImpl extends DragGestureImpl<SilviculturalEmissions> {
		@Override
		protected SilviculturalEmissions adaptSourceToTransferable(DragGestureEvent event) {
			SilviculturalEmissions obj;
			DetailedLCI lci = (DetailedLCI) ((JList) event.getComponent()).getSelectedValue();
			obj = new SilviculturalEmissions(lci);
			return obj;
		}
	}
	
	@SuppressWarnings("unused")
	private final SilviculturalEmissionsManager caller;

	private JList list;
	protected DnDPanel<SilviculturalEmissions> mainPanel;
	
	/**
	 * Constructor.
	 * @param caller a SilviculturalEmissionsManager instance
	 * @param parent a Window that is the parent of this dialog
	 */
	protected SilviculturalEmissionsManagerDialog(SilviculturalEmissionsManager caller, Window parent) {
		super(parent);
		this.caller = caller;
		list = new JList();
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY, new InternalDragGestureImpl());
		mainPanel = new DnDPanel<SilviculturalEmissions>(caller, SilviculturalEmissions.class);
		initUI();
		setMinimumSize(new Dimension(400,200));
		refreshInterface();
	}
	
	@Override
	protected void initUI() {
		JSplitPane pane = new JSplitPane();
		pane.setRightComponent(list);
		pane.setLeftComponent(mainPanel);
		pane.setDividerLocation(0.25);
		add(pane);
	}

	@Override
	public void listenTo() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doNotListenToAnymore() {
		// TODO Auto-generated method stub
	}


	@Override
	public void refreshInterface() {
		DefaultListModel model = new DefaultListModel();
		for (DetailedLCI lci : DetailedLCIManager.getInstance().values()) {
			model.addElement(lci);
		}
		list.setModel(model);
		mainPanel.refreshInterface();
		repaint();
	}


}
