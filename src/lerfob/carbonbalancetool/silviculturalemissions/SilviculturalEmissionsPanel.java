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

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.gui.dnd.DnDPanel;

@SuppressWarnings("serial")
public class SilviculturalEmissionsPanel extends REpiceaPanel implements NumberFieldListener, ActionListener {

	private final SilviculturalEmissions caller;
	private final JFormattedNumericField dateField;
	private final JButton deleteButton;
	private JLabel lciLabel;
	
	protected SilviculturalEmissionsPanel(SilviculturalEmissions caller) {
		this.caller = caller;
		lciLabel = new JLabel();
		dateField = NumberFormatFieldFactory.createNumberFormatField(10, Type.Integer, Range.Positive, true);
		deleteButton = UIControlManager.createCommonButton(CommonControlID.Cancel);
		deleteButton.setText("");
		Insets insets = new Insets(2,2,2,2);
		deleteButton.setMargin(insets);
		createUI();
	}
	
	private void createUI() {
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(Box.createHorizontalStrut(5));
		add(deleteButton);
		add(Box.createHorizontalStrut(5));
		add(new JLabel("Date"));
		add(Box.createHorizontalStrut(5));
		add(dateField);
		add(Box.createHorizontalStrut(5));
		add(new JLabel("Intervention"));
		add(Box.createHorizontalStrut(5));
		add(lciLabel);
		add(Box.createHorizontalStrut(5));
	}

	@Override
	public void refreshInterface() {
		lciLabel.setText(caller.lci.toString());
		if (caller.date == -1) {
			dateField.setText("");
		} else {
			dateField.setText(((Integer) caller.date).toString());
		}
	}

	@Override
	public void listenTo() {
		dateField.addNumberFieldListener(this);
		deleteButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		dateField.removeNumberFieldListener(this);
		deleteButton.removeActionListener(this);
	}

	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(dateField)) {
			if (dateField.getText().isEmpty()) {
				caller.date = -1;
			} else {
				caller.date = Integer.parseInt(dateField.getText());
			}
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(deleteButton)) {
			DnDPanel panel = (DnDPanel) CommonGuiUtility.getParentComponent(this, DnDPanel.class);
			panel.removeSubpanel(caller);
		}
	}
	


}
