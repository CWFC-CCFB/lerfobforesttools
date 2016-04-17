/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool.sensitivityanalysis;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CATSensitivityAnalysisSettingsDlg extends REpiceaDialog implements ItemListener {

	private static enum MessageID implements TextableEnum {
		EnableSensitivityAnalysis("Enable sensitivity analysis", "Activer l'analyse de sensibilit\u00E9");

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
	
	private final JCheckBox enableSensitivityAnalysis;
	
	private final CATSensitivityAnalysisSettings caller;
	
	protected CATSensitivityAnalysisSettingsDlg(CATSensitivityAnalysisSettings catSensitivityAnalysisSettings, Window parent) {
		super(parent);
		this.caller = catSensitivityAnalysisSettings;
		enableSensitivityAnalysis = new JCheckBox(MessageID.EnableSensitivityAnalysis.toString());
		initUI();
		checkWhichFeaturesShouldBeEnabled();
		pack();
	}

	private void checkWhichFeaturesShouldBeEnabled() {
		for (Component comp : getContentPane().getComponents()) {
			if (comp instanceof CATSensitivityParameterWrapper.CATSensitivityParameterWrapperPanel) {
				((CATSensitivityParameterWrapper.CATSensitivityParameterWrapperPanel) comp).setEnabled(enableSensitivityAnalysis.isSelected());
			}
		}
	}

	@Override
	public void listenTo() {
		enableSensitivityAnalysis.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		enableSensitivityAnalysis.removeItemListener(this);
	}

	@Override
	protected void initUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel pane = new JPanel();
		pane.add(this.enableSensitivityAnalysis);
		pane.add(Box.createGlue());
		getContentPane().add(pane);
		for (VariabilitySource source : VariabilitySource.values()) {
			getContentPane().add(caller.sensitivityParameterMap.get(source).getGuiInterface());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(enableSensitivityAnalysis)) {
			checkWhichFeaturesShouldBeEnabled();
		}
	}
	
}
