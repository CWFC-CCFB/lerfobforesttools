/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.mathilde;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaSlider;
import repicea.simulation.treelogger.LogCategoryPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class MathildeTreeLogCategoryPanel extends LogCategoryPanel<MathildeTreeLogCategory> implements PropertyChangeListener {

	private static enum MessageID implements TextableEnum {
		MinimumTreeDiameter("Minimum tree dbh (cm)", "d130 minimum (cm)"),
		PotentialUse("Proportion of commercial volume (%)", "Proportion du volume bois fort (%)"),
		DownGrading("Proportion downgraded (%)", "Proportion de d\u00E9class\u00E9s (%)");

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
	
	private final REpiceaSlider potentialSlider;
	private final REpiceaSlider downgradingSlider;
	
	protected MathildeTreeLogCategoryPanel(MathildeTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(false);
		potentialSlider = new REpiceaSlider();
		potentialSlider.setValue((int) (logCategory.conversionFactor * 100));
		downgradingSlider = new REpiceaSlider();
		downgradingSlider.setValue((int) (logCategory.downgradingProportion * 100));
		createUI();
	}

	private void createUI() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new BorderLayout(0, 0));

		JPanel logCategoryNamePanel = new JPanel();
		add(logCategoryNamePanel, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) logCategoryNamePanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut);

		JLabel nameLabel = new JLabel(REpiceaTranslator.getString(repicea.simulation.treelogger.LogCategoryPanel.MessageID.LogGradeName));
		nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		logCategoryNamePanel.add(nameLabel);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut_1);

		nameTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		logCategoryNamePanel.add(nameTextField);
		nameTextField.setColumns(15);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel featurePanel = new JPanel();
		featurePanel.add(Box.createHorizontalStrut(5));
		featurePanel.add(UIControlManager.getLabel(MessageID.MinimumTreeDiameter));
		featurePanel.add(Box.createHorizontalStrut(5));
		JTextField textField = new JTextField(5);
		if (!Double.isNaN(getTreeLogCategory().minimumDbhCm)) {
			textField.setText("" + getTreeLogCategory().minimumDbhCm);
		}
		textField.setEditable(false);
		featurePanel.add(textField);
		panel.add(featurePanel);
		panel.add(Box.createHorizontalStrut(5));
		
		JPanel potentialLumberWoodPanel = new JPanel();
		potentialLumberWoodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageID.PotentialUse.toString()));
		panel.add(potentialLumberWoodPanel);
		potentialLumberWoodPanel.add(potentialSlider);

		JPanel downgradingPanel = new JPanel();
		downgradingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageID.DownGrading.toString()));
		panel.add(downgradingPanel);
		downgradingPanel.add(downgradingSlider);
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		downgradingSlider.addPropertyChangeListener(this);
		potentialSlider.addPropertyChangeListener(this);
	}
	
	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		downgradingSlider.removePropertyChangeListener(this);
		potentialSlider.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(REpiceaSlider.SLIDER_CHANGE)) {
			if (arg0.getSource().equals(downgradingSlider)) {
				logCategory.downgradingProportion = downgradingSlider.getValue() * .01;
			} else if (arg0.getSource().equals(potentialSlider)) {
				logCategory.conversionFactor = potentialSlider.getValue() * .01;
			}
		}
	}

}
