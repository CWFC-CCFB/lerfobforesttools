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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import lerfob.carbonbalancetool.productionlines.LifeCycleAnalysis.ReferenceLCA;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The EndProductFeaturePanel class is the GUI interface of the EndProductFeature class.
 * @author Mathieu Fortin - October 2010
 */
public class EndUseWoodProductCarbonUnitFeaturePanel extends CarbonUnitFeaturePanel implements ChangeListener, ItemListener {
	
	private static final long serialVersionUID = 20101020L;
	
	public static enum MessageID implements TextableEnum {
		LandfillSiteCheckBoxLabel("Send to landfill after useful life", "Envoyer \u00E0 la d\u00E9charge apr\u00E8s vie utile"),
		PercentageSentToTheLandfill("Percentage sent to landfill", "Pourcentage envoy\u00E9 \u00E0 la d\u00E9charge"),
		AverageSubstitution("Average substitution (Mg C eq / m3)", "Subtitution moyenne (Mg C eq / m3)"),
		UseClassCategory("Use class", "Cat\u00E9gorie d'usage"),
		LifeCycleLibrary("Life cycle inventory", "Inventaire de cycle de vie");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	private JComboBox useClassList;
	@SuppressWarnings("rawtypes")
	private JComboBox lifeCycleAnalysisList;
	protected JFormattedNumericField averageSubstitutionTextField;

	protected JCheckBox isDisposableCheckBox;

	private JLabel disposableProportionLabel;
	protected JSlider disposableProportionSlider;

	/**
	 * The constructor is linked to its representing class.
	 * @param caller a EndProductFeature instance
	 */
	protected EndUseWoodProductCarbonUnitFeaturePanel(EndUseWoodProductCarbonUnitFeature caller) {
		super(caller);
	}

	@Override
	protected EndUseWoodProductCarbonUnitFeature getCaller() {return (EndUseWoodProductCarbonUnitFeature) super.getCaller();}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void initializeFields() {
		super.initializeFields();
		
		disposableProportionLabel = new JLabel();
		Dimension dim = new Dimension(getFontMetrics(this.getFont()).getHeight() * 3 , getFontMetrics(this.getFont()).getHeight() * 2);
		disposableProportionLabel.setPreferredSize(dim);

		isDisposableCheckBox = new JCheckBox();
		isDisposableCheckBox.setSelected(getCaller().isDisposed());

		disposableProportionSlider = new JSlider(0,100);
		disposableProportionSlider.setMajorTickSpacing(10);
		disposableProportionSlider.setPaintTicks(true);
		disposableProportionSlider.setPaintLabels(true);

		disposableProportionSlider.setEnabled(isDisposableCheckBox.isSelected());
		disposableProportionSlider.setValue((int) (getCaller()).getDisposableProportion() * 100);
		disposableProportionLabel.setText(((Integer) disposableProportionSlider.getValue()).toString() + "%");

		averageSubstitutionTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, false);
		averageSubstitutionTextField.setText(((Double) getCaller().getAverageSubstitution(null)).toString());
		averageSubstitutionTextField.setPreferredSize(new Dimension(100, averageSubstitutionTextField.getFontMetrics(averageSubstitutionTextField.getFont()).getHeight() + 2));
		
		useClassList = new JComboBox(UseClass.values());
		UseClass useClass = (getCaller()).getUseClass();
		for (int i = 0; i < UseClass.values().length; i++) {
			if (useClass == UseClass.values()[i]) {
				useClassList.setSelectedIndex(i);
				break;
			}
		}
		
		lifeCycleAnalysisList = new JComboBox(LifeCycleAnalysis.ReferenceLCA.values());

		LifeCycleAnalysis lca = getCaller().getLCA();
		if (lca != null) {
			for (int i = 0; i < ReferenceLCA.values().length; i++) {
				if (lca.compare(ReferenceLCA.values()[i].getLCA())) {
					lifeCycleAnalysisList.setSelectedIndex(i);
					break;
				}
			}
		} else {
			lifeCycleAnalysisList.setSelectedIndex(0);
		}
	}
	
	
	@Override
	protected void createUI() {
		super.createUI();

		
		JPanel averageSubstitutionPanel = UIControlManager.createSimpleHorizontalPanel(UIControlManager.getLabel(MessageID.AverageSubstitution),
				averageSubstitutionTextField, 
				5,
				true);

		JPanel useClassPanel = UIControlManager.createSimpleHorizontalPanel(UIControlManager.getLabel(MessageID.UseClassCategory),
				useClassList, 
				5,
				true);
		
		JPanel lifeCyclePanel = UIControlManager.createSimpleHorizontalPanel(UIControlManager.getLabel(MessageID.LifeCycleLibrary),
				lifeCycleAnalysisList, 
				5,
				true);
		
		mainPanel.add(averageSubstitutionPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(useClassPanel);
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(lifeCyclePanel);
		mainPanel.add(Box.createVerticalStrut(5));

	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		useClassList.setEnabled(b);
		lifeCycleAnalysisList.setEnabled(b);
		averageSubstitutionTextField.setEnabled(b);
		isDisposableCheckBox.setEnabled(b);
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(disposableProportionSlider)) {
			disposableProportionLabel.setText(((Integer) disposableProportionSlider.getValue()).toString() + "%");
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource().equals(isDisposableCheckBox)) {
			if (isEnabled()) {
				disposableProportionSlider.setEnabled(isDisposableCheckBox.isSelected());
			}
		}
	}
	
	@Override
	public void listenTo() {
		super.listenTo();
		isDisposableCheckBox.addItemListener(getCaller());
		isDisposableCheckBox.addItemListener(this);
		disposableProportionSlider.addChangeListener(this);
		disposableProportionSlider.addChangeListener(getCaller());
		lifeCycleAnalysisList.addItemListener(getCaller());
		averageSubstitutionTextField.addNumberFieldListener(getCaller());
		useClassList.addItemListener(getCaller());
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		isDisposableCheckBox.removeItemListener(getCaller());
		isDisposableCheckBox.removeItemListener(this);
		disposableProportionSlider.removeChangeListener(this);
		disposableProportionSlider.removeChangeListener(getCaller());
		lifeCycleAnalysisList.removeItemListener(getCaller());
		averageSubstitutionTextField.removeNumberFieldListener(getCaller());
		useClassList.removeItemListener(getCaller());
	}


}
