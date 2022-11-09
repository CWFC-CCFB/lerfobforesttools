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
package lerfob.treelogger.diameterbasedtreelogger;

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
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.REpiceaSlider;
import repicea.simulation.treelogger.LogCategoryPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class DiameterBasedTreeLogCategoryPanel extends LogCategoryPanel<DiameterBasedTreeLogCategory> implements NumberFieldListener, PropertyChangeListener {
	
	protected static enum MessageID implements TextableEnum {
		MinimumDBH("Minimum DBH (cm)", "DHP minimum (cm)"),
		PotentialUse("Proportion of commercial volume (%)", "Proportion du volume commercial (%)"),
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
	
	private final JFormattedNumericField minimumDiameterField;
	private final REpiceaSlider downgradingProportionSlider;
	private final REpiceaSlider potentialSlider;

	
	protected DiameterBasedTreeLogCategoryPanel(DiameterBasedTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(logCategory.isChangeAllowed);
		minimumDiameterField = NumberFormatFieldFactory.createNumberFormatField(10,
				NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.Positive,
				false);
		minimumDiameterField.setText(((Double) getTreeLogCategory().minimumDbhCm).toString());
		minimumDiameterField.setEditable(logCategory.isChangeAllowed);
		
		potentialSlider = new REpiceaSlider();
		potentialSlider.setValue((int) (logCategory.conversionFactor * 100));
		potentialSlider.setEnabled(logCategory.isChangeAllowed);
		
		downgradingProportionSlider = new REpiceaSlider();
		downgradingProportionSlider.setValue((int) (logCategory.downgradingProportion * 100));
		downgradingProportionSlider.setEnabled(logCategory.isChangeAllowed);
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

		JLabel nameLabel = new JLabel(LogCategoryPanel.MessageID.LogGradeName.toString());
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
		featurePanel.add(UIControlManager.getLabel(MessageID.MinimumDBH));
		featurePanel.add(Box.createHorizontalStrut(5));
		featurePanel.add(minimumDiameterField);
		featurePanel.add(Box.createHorizontalGlue());
		panel.add(featurePanel);

		if (getTreeLogCategory().isConversionEnabled) {
			JPanel potentialLumberWoodPanel = getPotentialPanel();
			if (potentialLumberWoodPanel != null) {
				panel.add(potentialLumberWoodPanel);
			}
		}

		if (getTreeLogCategory().isDowngradingEnabled) {
			JPanel downgradingPanel = new JPanel();
			downgradingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageID.DownGrading.toString()));
			panel.add(downgradingPanel);
			downgradingPanel.add(downgradingProportionSlider);
		}
	}
	
	
	protected JPanel getPotentialPanel() {
		JPanel potentialLumberWoodPanel = new JPanel();
		potentialLumberWoodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageID.PotentialUse.toString()));
		potentialLumberWoodPanel.add(potentialSlider);
		return potentialLumberWoodPanel;
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		minimumDiameterField.addNumberFieldListener(this);
		potentialSlider.addPropertyChangeListener(this);
		downgradingProportionSlider.addPropertyChangeListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		minimumDiameterField.removeNumberFieldListener(this);
		potentialSlider.removePropertyChangeListener(this);
		downgradingProportionSlider.removePropertyChangeListener(this);
	}

	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(minimumDiameterField)) {
			getTreeLogCategory().minimumDbhCm = minimumDiameterField.getValue().doubleValue();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(downgradingProportionSlider)) {
			getTreeLogCategory().downgradingProportion = downgradingProportionSlider.getValue() * .01;
		} else if (evt.getSource().equals(potentialSlider)) {
			getTreeLogCategory().conversionFactor = potentialSlider.getValue() * .01;
		}
	}
	
}
