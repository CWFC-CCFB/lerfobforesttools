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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import lerfob.carbonbalancetool.productionlines.CarbonUnitFeature.LifetimeMode;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CarbonUnitFeaturePanel extends REpiceaPanel {
	
	private static final long serialVersionUID = 20101020L;

	public static enum MessageID implements TextableEnum {
		WoodProductFeatureLabel("Specific features", "Caract\u00E9ristiques sp\u00E9cifiques"),
//		AverageLifeTime("Lifetime (yr)", "Dur\u00E9e de vie (ann\u00E9es)")
		;
		
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
	
	protected JComboBox<LifetimeMode> lifetimeModeList;
	
	protected JFormattedNumericField averageLifetimeTextField;
	
	private CarbonUnitFeature caller;
	
	protected JPanel mainPanel;
	
	protected CarbonUnitFeaturePanel(CarbonUnitFeature caller) {
		super();
		setCaller(caller);
		initializeFields();
		createUI();
	}

	protected CarbonUnitFeature getCaller() {return caller;}
	protected void setCaller(CarbonUnitFeature caller) {this.caller = caller;}
	
	protected void initializeFields() {
		averageLifetimeTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, false);
		averageLifetimeTextField.setText(((Double) getCaller().getAverageLifetime(null)).toString());
		averageLifetimeTextField.setPreferredSize(new Dimension(100, averageLifetimeTextField.getFontMetrics(averageLifetimeTextField.getFont()).getHeight() + 2));
		
		lifetimeModeList = new JComboBox<LifetimeMode>(LifetimeMode.values());
		lifetimeModeList.setSelectedItem(getCaller().getLifetimeMode());
	}
	
	protected void createUI() {
		setLayout(new BorderLayout());
		setBorder(UIControlManager.getTitledBorder(MessageID.WoodProductFeatureLabel.toString()));
		JPanel setupPanel = new JPanel(new BorderLayout());
		add(setupPanel, BorderLayout.CENTER);
		mainPanel = new JPanel();
		setupPanel.add(mainPanel, BorderLayout.NORTH);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		JPanel lifetimePanel = UIControlManager.createSimpleHorizontalPanel(lifetimeModeList, averageLifetimeTextField, 5, true);
		
		mainPanel.add(Box.createVerticalStrut(5));
		mainPanel.add(lifetimePanel);
		mainPanel.add(Box.createVerticalStrut(5));
	}

	
	
	
	
	
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		averageLifetimeTextField.setEnabled(b);
	}

	@Override
	public void listenTo() {
		averageLifetimeTextField.addNumberFieldListener(getCaller());
		lifetimeModeList.addItemListener(getCaller());
	}

	@Override
	public void doNotListenToAnymore() {
		averageLifetimeTextField.removeNumberFieldListener(getCaller());
		lifetimeModeList.removeItemListener(getCaller());
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.gui.Refreshable#refreshInterface()
	 */
	@Override
	public void refreshInterface() {}


}
