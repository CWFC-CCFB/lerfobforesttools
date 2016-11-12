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

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CATSensitivityAnalysisSettingsDlg extends REpiceaDialog implements NumberFieldListener {

	static {
		UIControlManager.setTitle(CATSensitivityAnalysisSettingsDlg.class, "Sensitivity analysis", "Analyse de sensibilit\u00E9");
	}
	
	private static enum MessageID implements TextableEnum {
		NumberMonteCarloRealizations("Number of realizations (1-1000)", "Nombre de r\u00E9alisations (1-1000)"),
		NumberMonteCarloRealizationsWarning("The number of realizations must be between 1 and 1000.", "Le nombre de r\u00E9alisations doit \u00EAtre entre 1 et 1000.");

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
	
	
	private final CATSensitivityAnalysisSettings caller;
	private JFormattedNumericField mcRealField;
	
	protected CATSensitivityAnalysisSettingsDlg(CATSensitivityAnalysisSettings catSensitivityAnalysisSettings, Window parent) {
		super(parent);
		this.caller = catSensitivityAnalysisSettings;
		initUI();
		pack();
	}

	@Override
	public void listenTo() {
		mcRealField.addNumberFieldListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		mcRealField.removeNumberFieldListener(this);
	}

	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		mcRealField = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Integer, Range.Positive, false);
		mcRealField.setPreferredSize(new Dimension(100, mcRealField.getFontMetrics(mcRealField.getFont()).getHeight() + 2));
	
		JPanel monteCarloRealPanel = UIControlManager.createSimpleHorizontalPanel(MessageID.NumberMonteCarloRealizations, 
				mcRealField, 
				5, 
				false);
		getContentPane().add(monteCarloRealPanel);
		
		for (VariabilitySource source : VariabilitySource.values()) {
			getContentPane().add(caller.sensitivityParameterMap.get(source).getUI());
		}
		synchronizeUIWithOwner();
	}

	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(mcRealField)) {
			caller.nbMonteCarloRealizations = Integer.parseInt(mcRealField.getText());
		} 
	}

	protected void synchronizeUIWithOwner() {
		mcRealField.setText(((Integer) caller.nbMonteCarloRealizations).toString());
		mcRealField.setEnabled(!caller.isModelStochastic);
	}
}
