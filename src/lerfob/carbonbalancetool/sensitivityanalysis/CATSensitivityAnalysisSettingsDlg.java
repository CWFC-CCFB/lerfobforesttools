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

import java.awt.Window;

import javax.swing.BoxLayout;

import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;

@SuppressWarnings("serial")
public class CATSensitivityAnalysisSettingsDlg extends REpiceaDialog {

	static {
		UIControlManager.setTitle(CATSensitivityAnalysisSettingsDlg.class, "Sensitivity analysis", "Analyse de sensibilit\u00E9");
	}
	
	private final CATSensitivityAnalysisSettings caller;
	
	protected CATSensitivityAnalysisSettingsDlg(CATSensitivityAnalysisSettings catSensitivityAnalysisSettings, Window parent) {
		super(parent);
		this.caller = catSensitivityAnalysisSettings;
		initUI();
		pack();
	}

	@Override
	public void listenTo() {}

	@Override
	public void doNotListenToAnymore() {}

	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		for (VariabilitySource source : VariabilitySource.values()) {
			getContentPane().add(caller.sensitivityParameterMap.get(source).getGuiInterface());
		}
	}
	
}
