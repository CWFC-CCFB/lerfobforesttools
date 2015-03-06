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
package lerfob.carbonbalancetool.biomassparameters;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import repicea.app.SettingMemory;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.serial.Memorizable;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class BiomassParametersDialog extends REpiceaDialog implements IOUserInterface, OwnedWindow, NumberFieldListener, ActionListener, ItemListener {

	static {
		UIControlManager.setTitle(BiomassParametersDialog.class, "Carbon estimation in biomass", "Estimation du carbone de la biomasse");
	}
	
	protected static enum MessageID implements TextableEnum {
		Blank(" ", " "),
		Unnamed("Unnamed", "Sansnom"),
		Volume("Total volume", "Volume total"),
		Biomass("Total biomass", "Biomasse totale"),
		Carbon("Total carbon", "Carbone total"),
		VolumeBelowGround("Belowground volume", "Volume souterrain"),
		BiomassBelowGround("Belowground biomass", "Biomasse souterraine"),
		CarbonBelowGround("Belowground carbon", "Carbone souterrain"),
		VolumeAboveGround("Aboveground volume", "Volume a\u00E9rien"),
		BiomassAboveGround("Aboveground biomass", "Biomasse a\u00E9rienne"),
		CarbonAboveGround("Aboveground carbon", "Carbone a\u00E9rien"),
		BranchExpansionFactor("Branch expansion factors", "Facteurs d'expansion des branches"),
		RootExpansionFactor("Root expansion factors", "Facteurs d'expansion des racines"),
		BasicDensityFactor("Basic densities", "Infra densit\u00E9s"),
		CarbonContent("Carbon contents", "Teneurs en carbone"),
		ProvidedByTheModel("Provided by the model", "Fournis par le mod\u00E8le"),
		AboveGround("Aboveground biomass", "Biomasse a\u00E9rienne"),
		BelowGround("Belowgound biomass", "Biomasse souterraine"),
		BiomassConversion("HWP biomass", "Biomasses des HWPs"),
		BiomassParametersFileExtension("biomass parameters file (*.bpf)", "fichier de param\u00E8tres de biomasse (*.bpf)");

		
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
	
	private final JMenuItem reset;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final JMenuItem close;
	private final JMenuItem help;
	
	
	protected final JFormattedNumericField branchExpansionFactorConiferous;
	protected final JFormattedNumericField branchExpansionFactorBroadleaved;
	
	protected final JFormattedNumericField rootExpansionFactorConiferous;
	protected final JFormattedNumericField rootExpansionFactorBroadleaved;

	protected final JFormattedNumericField basicDensityFactorConiferous;
	protected final JFormattedNumericField basicDensityFactorBroadleaved;
	protected final JFormattedNumericField carbonContentConiferous;
	protected final JFormattedNumericField carbonContentBroadleaved;

	protected final JCheckBox branchFromModelChkBox;
	protected final JCheckBox rootFromModelChkBox;
	protected final JCheckBox basicDensityFromModelChkBox;
	protected final JCheckBox carbonContentFromModelChkBox;

	
	
	private static final long serialVersionUID = 20130828L;
	protected BiomassParameters caller;
	/**
	 * Constructor.
	 * @param window
	 * @param caller
	 */
	protected BiomassParametersDialog(Window window, BiomassParameters caller) {
		super(window);
		this.caller = caller;
		
		branchExpansionFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		branchExpansionFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		branchFromModelChkBox = new JCheckBox();

		rootExpansionFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		rootExpansionFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		rootFromModelChkBox = new JCheckBox();
		
		basicDensityFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		basicDensityFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		carbonContentConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		carbonContentBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);

		basicDensityFromModelChkBox = new JCheckBox();
		carbonContentFromModelChkBox = new JCheckBox();
		
	
		reset = UIControlManager.createCommonMenuItem(CommonControlID.Reset);
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		close = UIControlManager.createCommonMenuItem(CommonControlID.Close);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		initUI();
	}


	protected BiomassParameters getCaller() {return caller;}
	
	@Override
	protected void initUI() {
		refreshTitle();
		getContentPane().setLayout(new BorderLayout());
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu file = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		menuBar.add(file);
		
		file.add(load);
		file.add(save);
		file.add(saveAs);
		file.add(new JSeparator());
		file.add(close);
		
		file.setEnabled(getCaller().permissions.isEnablingGranted());
		
		JMenu edit = UIControlManager.createCommonMenu(CommonMenuTitle.Edit);
		menuBar.add(edit);
		edit.add(reset);
		edit.setEnabled(getCaller().permissions.isEnablingGranted());
		
		JMenu about = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(about);
		
		about.add(help);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(5,4,5,5));
		
		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.Blank), FlowLayout.LEFT));
		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.ProvidedByTheModel), FlowLayout.CENTER));
		mainPanel.add(makePanel(UIControlManager.getLabel(SpeciesType.ConiferousSpecies), FlowLayout.CENTER));
		mainPanel.add(makePanel(UIControlManager.getLabel(SpeciesType.BroadleavedSpecies), FlowLayout.CENTER));

		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.BranchExpansionFactor), FlowLayout.LEFT));
		mainPanel.add(makePanel(branchFromModelChkBox, FlowLayout.CENTER));
		mainPanel.add(makePanel(branchExpansionFactorConiferous, FlowLayout.CENTER));
		mainPanel.add(makePanel(branchExpansionFactorBroadleaved, FlowLayout.CENTER));
		
		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.RootExpansionFactor), FlowLayout.LEFT));
		mainPanel.add(makePanel(rootFromModelChkBox, FlowLayout.CENTER));
		mainPanel.add(makePanel(rootExpansionFactorConiferous, FlowLayout.CENTER));
		mainPanel.add(makePanel(rootExpansionFactorBroadleaved, FlowLayout.CENTER));

		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.BasicDensityFactor), FlowLayout.LEFT));
		mainPanel.add(makePanel(basicDensityFromModelChkBox, FlowLayout.CENTER));
		mainPanel.add(makePanel(basicDensityFactorConiferous, FlowLayout.CENTER));
		mainPanel.add(makePanel(basicDensityFactorBroadleaved, FlowLayout.CENTER));

		mainPanel.add(makePanel(UIControlManager.getLabel(MessageID.CarbonContent), FlowLayout.LEFT));
		mainPanel.add(makePanel(carbonContentFromModelChkBox, FlowLayout.CENTER));
		mainPanel.add(makePanel(carbonContentConiferous, FlowLayout.CENTER));
		mainPanel.add(makePanel(carbonContentBroadleaved, FlowLayout.CENTER));
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		synchronizeUIWithOwner();
		pack();
		setMinimumSize(getSize());
	}

	protected JPanel makePanel(Component comp, int orientation) {
		JPanel panel = new JPanel(new FlowLayout(orientation));
		panel.add(comp);
		return panel;
	}


	@Override
	public void listenTo() {
		branchExpansionFactorConiferous.addNumberFieldListener(this);
		branchExpansionFactorBroadleaved.addNumberFieldListener(this);
		rootExpansionFactorConiferous.addNumberFieldListener(this);
		rootExpansionFactorBroadleaved.addNumberFieldListener(this);
		basicDensityFactorConiferous.addNumberFieldListener(this);
		basicDensityFactorBroadleaved.addNumberFieldListener(this);
		carbonContentConiferous.addNumberFieldListener(this);
		carbonContentBroadleaved.addNumberFieldListener(this);
		reset.addActionListener(this);
		branchFromModelChkBox.addItemListener(this);
		rootFromModelChkBox.addItemListener(this);;
		basicDensityFromModelChkBox.addItemListener(this);
		carbonContentFromModelChkBox.addItemListener(this);

	}


	@Override
	public void doNotListenToAnymore() {
		branchExpansionFactorConiferous.removeNumberFieldListener(this);
		branchExpansionFactorBroadleaved.removeNumberFieldListener(this);
		rootExpansionFactorConiferous.removeNumberFieldListener(this);
		rootExpansionFactorBroadleaved.removeNumberFieldListener(this);
		basicDensityFactorConiferous.removeNumberFieldListener(this);
		basicDensityFactorBroadleaved.removeNumberFieldListener(this);
		carbonContentConiferous.removeNumberFieldListener(this);
		carbonContentBroadleaved.removeNumberFieldListener(this);
		reset.addActionListener(this);
		branchFromModelChkBox.removeItemListener(this);
		rootFromModelChkBox.removeItemListener(this);;
		basicDensityFromModelChkBox.removeItemListener(this);
		carbonContentFromModelChkBox.removeItemListener(this);
	}




	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public void postLoadingAction() {
		synchronizeUIWithOwner();
	}


	@Override
	public SettingMemory getSettingMemory() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The method sets the title of the dialog.
	 */
	protected void refreshTitle() {
		String filename = getCaller().getName();
		if (filename.isEmpty()) {
			setTitle(UIControlManager.getTitle(getClass()));
		} else {
			if (filename.length() > 40) {
				filename = "..." + filename.substring(filename.length()-41, filename.length());
			}
			setTitle((UIControlManager.getTitle(getClass())) + " - " + filename);
		}
	}


	@Override
	public void synchronizeUIWithOwner() {
		refreshTitle();

		branchExpansionFactorConiferous.setText(getCaller().branchExpansionFactors.get(SpeciesType.ConiferousSpecies).toString());
		branchExpansionFactorBroadleaved.setText(getCaller().branchExpansionFactors.get(SpeciesType.BroadleavedSpecies).toString());
		branchFromModelChkBox.setSelected(getCaller().branchExpansionFactorFromModel);
		branchFromModelChkBox.setEnabled(getCaller().branchExpansionFactorFromModelEnabled && getCaller().permissions.isEnablingGranted());
		branchExpansionFactorConiferous.setEnabled(!getCaller().branchExpansionFactorFromModel && getCaller().permissions.isEnablingGranted());
		branchExpansionFactorBroadleaved.setEnabled(!getCaller().branchExpansionFactorFromModel && getCaller().permissions.isEnablingGranted());
		
		rootExpansionFactorConiferous.setText(getCaller().rootExpansionFactors.get(SpeciesType.ConiferousSpecies).toString());
		rootExpansionFactorBroadleaved.setText(getCaller().rootExpansionFactors.get(SpeciesType.BroadleavedSpecies).toString());
		rootFromModelChkBox.setSelected(getCaller().rootExpansionFactorFromModel);
		rootFromModelChkBox.setEnabled(getCaller().rootExpansionFactorFromModelEnabled && getCaller().permissions.isEnablingGranted());
		rootExpansionFactorConiferous.setEnabled(!getCaller().rootExpansionFactorFromModel && getCaller().permissions.isEnablingGranted());
		rootExpansionFactorBroadleaved.setEnabled(!getCaller().rootExpansionFactorFromModel && getCaller().permissions.isEnablingGranted());

		basicDensityFactorConiferous.setText(getCaller().basicWoodDensityFactors.get(SpeciesType.ConiferousSpecies).toString());
		basicDensityFactorBroadleaved.setText(getCaller().basicWoodDensityFactors.get(SpeciesType.BroadleavedSpecies).toString());
		basicDensityFromModelChkBox.setSelected(getCaller().basicWoodDensityFromModel);
		basicDensityFromModelChkBox.setEnabled(getCaller().basicWoodDensityFromModelEnabled && getCaller().permissions.isEnablingGranted());
		basicDensityFactorConiferous.setEnabled(!getCaller().basicWoodDensityFromModel && getCaller().permissions.isEnablingGranted());
		basicDensityFactorBroadleaved.setEnabled(!getCaller().basicWoodDensityFromModel && getCaller().permissions.isEnablingGranted());
		
		carbonContentConiferous.setText(getCaller().carbonContentFactors.get(SpeciesType.ConiferousSpecies).toString());
		carbonContentBroadleaved.setText(getCaller().carbonContentFactors.get(SpeciesType.BroadleavedSpecies).toString());
		carbonContentFromModelChkBox.setSelected(getCaller().carbonContentFromModel);
		carbonContentFromModelChkBox.setEnabled(getCaller().carbonContentFromModelEnabled && getCaller().permissions.isEnablingGranted());
		carbonContentConiferous.setEnabled(!getCaller().carbonContentFromModel && getCaller().permissions.isEnablingGranted());
		carbonContentBroadleaved.setEnabled(!getCaller().carbonContentFromModel && getCaller().permissions.isEnablingGranted());
	}


	@Override
	public Memorizable getWindowOwner() {
		return getCaller();
	}
	
	
	
	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource() instanceof JFormattedNumericField) {
			JFormattedNumericField source = (JFormattedNumericField) e.getSource();
			if (e.getSource().equals(basicDensityFactorConiferous)) {
				getCaller().basicWoodDensityFactors.put(SpeciesType.ConiferousSpecies, Double.parseDouble(source.getText()));
			} else if (e.getSource().equals(basicDensityFactorBroadleaved)) {
				getCaller().basicWoodDensityFactors.put(SpeciesType.BroadleavedSpecies, Double.parseDouble(source.getText()));
			} else if (e.getSource().equals(carbonContentConiferous)) {
				getCaller().carbonContentFactors.put(SpeciesType.ConiferousSpecies, Double.parseDouble(source.getText()));
			} else if (e.getSource().equals(carbonContentBroadleaved)) {
				getCaller().carbonContentFactors.put(SpeciesType.BroadleavedSpecies, Double.parseDouble(source.getText()));
			} else 	if (source.equals(branchExpansionFactorConiferous)) {
				getCaller().branchExpansionFactors.put(SpeciesType.ConiferousSpecies, Double.parseDouble(source.getText()));
			} else if (e.getSource().equals(branchExpansionFactorBroadleaved)) {
				getCaller().branchExpansionFactors.put(SpeciesType.BroadleavedSpecies, Double.parseDouble(source.getText()));
			} else if (source.equals(rootExpansionFactorConiferous)) {
				getCaller().rootExpansionFactors.put(SpeciesType.ConiferousSpecies, Double.parseDouble(source.getText()));
			} else if (e.getSource().equals(rootExpansionFactorBroadleaved)) {
				getCaller().rootExpansionFactors.put(SpeciesType.BroadleavedSpecies, Double.parseDouble(source.getText()));
			}
		}
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(reset)) {
			getCaller().reset();
			synchronizeUIWithOwner();
		}
	}


	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(branchFromModelChkBox)) {
			getCaller().branchExpansionFactorFromModel = branchFromModelChkBox.isSelected();
			synchronizeUIWithOwner();
		} else if (arg0.getSource().equals(rootFromModelChkBox)) {
			getCaller().rootExpansionFactorFromModel = rootFromModelChkBox.isSelected();
			synchronizeUIWithOwner();
		} else if (arg0.getSource().equals(basicDensityFromModelChkBox)) {
			getCaller().basicWoodDensityFromModel = basicDensityFromModelChkBox.isSelected();
			synchronizeUIWithOwner();
		} else if (arg0.getSource().equals(carbonContentFromModelChkBox)) {
			getCaller().carbonContentFromModel = carbonContentFromModelChkBox.isSelected();
			synchronizeUIWithOwner();
		}  
	}

	
}