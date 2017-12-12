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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaPanel;
import repicea.gui.REpiceaUIObject;
import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaSlider;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.Distribution;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CATSensitivityAnalysisParameterWrapper implements REpiceaUIObject {

	private static enum MessageID implements TextableEnum {
		ErrorMargin("Error margin", "Marge d'erreur"),
		Enable("Enabled", "Activ\u00E9"),
		IPCCDefaultUncertainty("Set to IPCC suggested value", "Fixer \u00E0 la valeur sugg\u00E9r\u00E9e par le GIEC");

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
	
	@SuppressWarnings("serial")
	class CATSensitivityAnalysisParameterWrapperPanel extends REpiceaPanel implements ItemListener, PropertyChangeListener, ActionListener{
		
		private final JCheckBox enabled;
		private final JComboBox<Distribution.Type> distributionComboBox;
		private final REpiceaSlider variabilitySlider;
		private final JButton setToAppropriateValueButton;
		
		CATSensitivityAnalysisParameterWrapperPanel() {
			enabled = new JCheckBox(MessageID.Enable.toString());
			enabled.setSelected(false);	// default value
			Vector<Distribution.Type> dist = new Vector<Distribution.Type>();
			dist.add(Distribution.Type.UNIFORM);
			dist.add(Distribution.Type.GAUSSIAN);
			distributionComboBox = new JComboBox<Distribution.Type>(dist);
			variabilitySlider = new REpiceaSlider(REpiceaSlider.Position.West);
			variabilitySlider.setMinimum(CATSensitivityAnalysisParameterWrapper.this.source.getMinimumValue());
			variabilitySlider.setMaximum(CATSensitivityAnalysisParameterWrapper.this.source.getMaximumValue());
			setToAppropriateValueButton = UIControlManager.createButtonWithRedCircleIcon();
			setToAppropriateValueButton.setToolTipText(MessageID.IPCCDefaultUncertainty.toString());
			
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), CATSensitivityAnalysisParameterWrapper.this.source.toString()));
			add(Box.createHorizontalStrut(5));
			add(enabled);
			add(Box.createHorizontalStrut(10));
			add(new JLabel("Distribution"));
			add(distributionComboBox);
			add(Box.createHorizontalStrut(10));
			add(new JLabel(MessageID.ErrorMargin.toString()));
			add(variabilitySlider);
			add(Box.createHorizontalStrut(10));
			add(setToAppropriateValueButton);
			add(Box.createHorizontalStrut(5));
			
			setEnabled(enabled.isSelected());
		}
		
		
		@Override
		public void refreshInterface() {
			distributionComboBox.setSelectedItem(CATSensitivityAnalysisParameterWrapper.this.selectedDistributionType);
			CATSensitivityAnalysisParameter<?> param = CATSensitivityAnalysisParameterWrapper.this.parameterMap.get(CATSensitivityAnalysisParameterWrapper.this.selectedDistributionType);
			variabilitySlider.setValue((int) (param.getMultiplier() * 100));
		}

		@Override
		public void listenTo() {
			distributionComboBox.addItemListener(this);
			variabilitySlider.addPropertyChangeListener(this);
			enabled.addItemListener(this);
			setToAppropriateValueButton.addActionListener(this);
		}

		@Override
		public void doNotListenToAnymore() {
			distributionComboBox.removeItemListener(this);
			variabilitySlider.removePropertyChangeListener(this);
			enabled.removeItemListener(this);
			setToAppropriateValueButton.removeActionListener(this);
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (arg0.getSource().equals(distributionComboBox)) {
				CATSensitivityAnalysisParameterWrapper.this.selectedDistributionType = (Distribution.Type) distributionComboBox.getSelectedItem();
//				System.out.println("Selected distribution is " + (Distribution.Type) distributionComboBox.getSelectedItem());
			} else if (arg0.getSource().equals(enabled)) {
				for (CATSensitivityAnalysisParameter<?> param : CATSensitivityAnalysisParameterWrapper.this.parameterMap.values()) {
					param.setParametersVariabilityEnabled(enabled.isSelected());
				}
				setEnabled(enabled.isSelected());
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getSource().equals(variabilitySlider)) {
				double multiplier = variabilitySlider.getValue() * .01;
				for (CATSensitivityAnalysisParameter<?> param : CATSensitivityAnalysisParameterWrapper.this.parameterMap.values()) {
					param.setMultiplier(multiplier);
				}
//				System.out.println("Slider value set to " + variabilitySlider.getValue());
			}
		}

		@Override
		public void setEnabled(boolean bool) {
			distributionComboBox.setEnabled(bool);
			variabilitySlider.setEnabled(bool);
			setToAppropriateValueButton.setEnabled(bool);
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(setToAppropriateValueButton)) {
				variabilitySlider.setValue(CATSensitivityAnalysisParameterWrapper.this.source.getSuggestedIPCCValue());
			}
		}
	
	}

	
	
	
	class MonteCarloSimulationCompliantObjectImpl implements MonteCarloSimulationCompliantObject {

		final MonteCarloSimulationCompliantObject root;
		final String subjectId;
		
		MonteCarloSimulationCompliantObjectImpl(MonteCarloSimulationCompliantObject root, String subjectId) {
			this.root = root;
			this.subjectId = subjectId;
		}
		
		@Override
		public String getSubjectId() {return subjectId;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return null;}

		@Override
		public int getMonteCarloRealizationId() {return root.getMonteCarloRealizationId();}
		
	}
	
	private final VariabilitySource source;
	
	@SuppressWarnings("rawtypes")
	private final Map<Distribution.Type, CATSensitivityAnalysisParameter> parameterMap;
	protected Distribution.Type selectedDistributionType = Distribution.Type.UNIFORM; // default value
	private transient REpiceaPanel guiInterface;
	private final Map<String, MonteCarloSimulationCompliantObject> subjectMap;
	
	
		
	@SuppressWarnings({ "incomplete-switch", "rawtypes" })
	protected CATSensitivityAnalysisParameterWrapper(VariabilitySource source) {
		this.source = source;
		parameterMap = new HashMap<Distribution.Type, CATSensitivityAnalysisParameter>();
		subjectMap = new HashMap<String, MonteCarloSimulationCompliantObject>();
		double initialValue = source.getSuggestedIPCCValue() * .01;
		for (Distribution.Type type : Distribution.Type.values()) {
			switch(type) {
			case UNIFORM:
				parameterMap.put(type, new CATUniformSensitivityAnalysisParameter(initialValue));
				break;
			case GAUSSIAN:
				parameterMap.put(type, new CATGaussianSensitivityAnalysisParameter(initialValue));
				break;
			}
		}
	}
	
	@Override
	public REpiceaPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new CATSensitivityAnalysisParameterWrapperPanel();
		}
		return guiInterface;
	}

	
	protected double getValue(MonteCarloSimulationCompliantObject subject, String subjectId) {
		MonteCarloSimulationCompliantObject realSubject;
		if (subjectId != null) {
			if (!subjectMap.containsKey(subjectId)) {
				subjectMap.put(subjectId, new MonteCarloSimulationCompliantObjectImpl(subject, subjectId));
			}
			realSubject = subjectMap.get(subjectId);
		} else {
			realSubject = subject;
		}
		return parameterMap.get(selectedDistributionType).getParameterValueForThisSubject(realSubject).m_afData[0][0];
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	protected CATSensitivityAnalysisParameter<?> getParameter(Distribution.Type type) {
		CATSensitivityAnalysisParameter<?> parm = parameterMap.get(type);
		if (parm == null) {
			throw new InvalidParameterException("The supported distribution are Gaussian and uniform!");
		} else {
			return parm;
		}
	}
	
}
