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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class LandfillCarbonUnitFeaturePanel extends CarbonUnitFeaturePanel implements ChangeListener {

	private static final long serialVersionUID = 20101118L;
	
	public static enum MessageID implements TextableEnum {
		DegradableOrganicCarbonLabel ("Proportion of degradable carbone", "Proportion de carbone d\u00E9omposable");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
	}
	
	private JLabel degradableOrganicCarbonFractionLabel;
	protected JSlider degradableOrganicCarbonFractionSlider;

	
	protected LandfillCarbonUnitFeaturePanel(LandfillCarbonUnitFeature caller) {
		super(caller);
	}

	
	protected LandfillCarbonUnitFeature getCaller() {return (LandfillCarbonUnitFeature) super.getCaller();}
	
	@Override
	protected void initializeFields() {
		super.initializeFields();
		
		degradableOrganicCarbonFractionLabel = new JLabel();
		Dimension dim = new Dimension(getFontMetrics(this.getFont()).getHeight() * 3 , getFontMetrics(this.getFont()).getHeight() * 2);
		degradableOrganicCarbonFractionLabel.setPreferredSize(dim);

		degradableOrganicCarbonFractionSlider = new JSlider(0,100);
		degradableOrganicCarbonFractionSlider.setMajorTickSpacing(10);
		degradableOrganicCarbonFractionSlider.setPaintTicks(true);
		degradableOrganicCarbonFractionSlider.setPaintLabels(true);
		degradableOrganicCarbonFractionSlider.setValue((int) (getCaller().getDegradableOrganicCarbonFraction() * 100));
		degradableOrganicCarbonFractionLabel.setText(((Integer) degradableOrganicCarbonFractionSlider.getValue()).toString() + "%");
	}	
	
	@Override
	protected void createUI() {
		super.createUI();

		JPanel degradableOrganicCarbonFractionSliderLabel = new JPanel(new BorderLayout());
		JPanel degradableOrganicCarbonFractionSliderSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		degradableOrganicCarbonFractionSliderSubPanel.add(Box.createHorizontalStrut(10));
		degradableOrganicCarbonFractionSliderSubPanel.add(new JLabel(REpiceaTranslator.getString(MessageID.DegradableOrganicCarbonLabel)));
		degradableOrganicCarbonFractionSliderSubPanel.add(degradableOrganicCarbonFractionLabel);
		degradableOrganicCarbonFractionSliderLabel.add(degradableOrganicCarbonFractionSliderSubPanel, BorderLayout.WEST);
		degradableOrganicCarbonFractionSliderLabel.add(degradableOrganicCarbonFractionSlider, BorderLayout.CENTER);
		degradableOrganicCarbonFractionSliderLabel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

		mainPanel.add(degradableOrganicCarbonFractionSliderLabel);
		mainPanel.add(Box.createVerticalStrut(5));
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		degradableOrganicCarbonFractionSlider.setEnabled(b);
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(degradableOrganicCarbonFractionSlider)) {
			degradableOrganicCarbonFractionLabel.setText(((Integer) degradableOrganicCarbonFractionSlider.getValue()).toString() + "%");
		}
	}

	@Override
	public void listenTo() {
		super.listenTo();
		degradableOrganicCarbonFractionSlider.addChangeListener(this);
		degradableOrganicCarbonFractionSlider.addChangeListener(getCaller());
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		degradableOrganicCarbonFractionSlider.removeChangeListener(this);
		degradableOrganicCarbonFractionSlider.removeChangeListener(getCaller());
	}
}
