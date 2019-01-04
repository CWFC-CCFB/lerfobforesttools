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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import repicea.gui.AutomatedHelper;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.lang.REpiceaSystem;
import repicea.net.BrowserCaller;
import repicea.serial.Memorizable;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class BiomassParametersDialog extends REpiceaDialog implements IOUserInterface, OwnedWindow, NumberFieldListener, ActionListener, ItemListener {

	static {
		UIControlManager.setTitle(BiomassParametersDialog.class, "Carbon estimation in biomass", "Estimation du carbone de la biomasse");

		try {
			Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
			String url = "https://sourceforge.net/p/lerfobforesttools/wiki/CAT%20-%20User%20interface/#setting-biomass-parameters";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(BiomassParametersDialog.class, helper);
		} catch (Exception e) {}
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
		BiomassParametersFileExtension("biomass parameters file (*.bpf)", "fichier de param\u00E8tres de biomasse (*.bpf)"),
		AverageBasicWoodDensityForTheSpecies("Average wood density for each species (Tier 1)", "Densit\u00E9 moyenne pour chaque esp\u00E8ce (Tier 1)");

		
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
	
	private final JLabel woodDensityLabel;
	protected final JFormattedNumericField branchExpansionFactorConiferous;
	protected final JFormattedNumericField branchExpansionFactorBroadleaved;
	
	protected final JFormattedNumericField rootExpansionFactorConiferous;
	protected final JFormattedNumericField rootExpansionFactorBroadleaved;

//	protected final JFormattedNumericField basicDensityFactorConiferous;
//	protected final JFormattedNumericField basicDensityFactorBroadleaved;
	protected final JFormattedNumericField carbonContentConiferous;
	protected final JFormattedNumericField carbonContentBroadleaved;

	protected final JCheckBox branchFromModelChkBox;
	protected final JCheckBox rootFromModelChkBox;
	protected final JCheckBox basicDensityFromModelChkBox;
	protected final JCheckBox carbonContentFromModelChkBox;

	
	
	private static final long serialVersionUID = 20130828L;
	protected BiomassParameters caller;
	
	protected final WindowSettings windowSettings;
	
	/**
	 * Constructor.
	 * @param window
	 * @param caller
	 */
	protected BiomassParametersDialog(Window window, BiomassParameters caller) {
		super(window);
		this.caller = caller;
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);

		branchExpansionFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		branchExpansionFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		branchFromModelChkBox = new JCheckBox();

		rootExpansionFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		rootExpansionFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		rootFromModelChkBox = new JCheckBox();
		
//		basicDensityFactorConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
//		basicDensityFactorBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		carbonContentConiferous = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);
		carbonContentBroadleaved = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, Range.Positive, false);

		basicDensityFromModelChkBox = new JCheckBox();
		woodDensityLabel = new JLabel(MessageID.AverageBasicWoodDensityForTheSpecies.toString());
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
//		mainPanel.setLayout(new GridLayout(5,4,5,5));
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mainPanel.setLayout(gridBag);
		
        c.fill = GridBagConstraints.BOTH;

        c.weightx = 1.0;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.Blank), FlowLayout.LEFT), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.ProvidedByTheModel), FlowLayout.CENTER), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(SpeciesType.ConiferousSpecies), FlowLayout.CENTER), gridBag, c));
        c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(SpeciesType.BroadleavedSpecies), FlowLayout.CENTER), gridBag, c));

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.BOTH;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.BranchExpansionFactor), FlowLayout.LEFT), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(branchFromModelChkBox, FlowLayout.CENTER), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(branchExpansionFactorConiferous, FlowLayout.CENTER), gridBag, c));
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(setComponentInGridBag(makePanel(branchExpansionFactorBroadleaved, FlowLayout.CENTER), gridBag, c));

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.BOTH;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.RootExpansionFactor), FlowLayout.LEFT), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(rootFromModelChkBox, FlowLayout.CENTER), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(rootExpansionFactorConiferous, FlowLayout.CENTER), gridBag, c));
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(setComponentInGridBag(makePanel(rootExpansionFactorBroadleaved, FlowLayout.CENTER), gridBag, c));

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.BOTH;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.BasicDensityFactor), FlowLayout.LEFT), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(basicDensityFromModelChkBox, FlowLayout.CENTER), gridBag, c));
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(setComponentInGridBag(woodDensityLabel, gridBag, c));
////		mainPanel.add(makePanel(basicDensityFactorBroadleaved, FlowLayout.CENTER));

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.BOTH;
		mainPanel.add(setComponentInGridBag(makePanel(UIControlManager.getLabel(MessageID.CarbonContent), FlowLayout.LEFT), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(carbonContentFromModelChkBox, FlowLayout.CENTER), gridBag, c));
		mainPanel.add(setComponentInGridBag(makePanel(carbonContentConiferous, FlowLayout.CENTER), gridBag, c));
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(setComponentInGridBag(makePanel(carbonContentBroadleaved, FlowLayout.CENTER), gridBag, c));
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		synchronizeUIWithOwner();
		pack();
		setMinimumSize(getSize());
	}

	private Component setComponentInGridBag(Component panel, GridBagLayout gridBag, GridBagConstraints c) {
        gridBag.setConstraints(panel, c);
		return panel;
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
//		basicDensityFactorConiferous.addNumberFieldListener(this);
//		basicDensityFactorBroadleaved.addNumberFieldListener(this);
		carbonContentConiferous.addNumberFieldListener(this);
		carbonContentBroadleaved.addNumberFieldListener(this);
		reset.addActionListener(this);
		close.addActionListener(this);
		help.addActionListener(this);
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
//		basicDensityFactorConiferous.removeNumberFieldListener(this);
//		basicDensityFactorBroadleaved.removeNumberFieldListener(this);
		carbonContentConiferous.removeNumberFieldListener(this);
		carbonContentBroadleaved.removeNumberFieldListener(this);
		reset.removeActionListener(this);
		close.removeActionListener(this);
		help.removeActionListener(this);
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
	public WindowSettings getWindowSettings() {return windowSettings;}

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

//		basicDensityFactorConiferous.setText(getCaller().basicWoodDensityFactors.get(SpeciesType.ConiferousSpecies).toString());
//		basicDensityFactorBroadleaved.setText(getCaller().basicWoodDensityFactors.get(SpeciesType.BroadleavedSpecies).toString());
		basicDensityFromModelChkBox.setSelected(getCaller().basicWoodDensityFromModel);
		basicDensityFromModelChkBox.setEnabled(getCaller().basicWoodDensityFromModelEnabled && getCaller().permissions.isEnablingGranted());
		woodDensityLabel.setEnabled(!getCaller().basicWoodDensityFromModel && getCaller().permissions.isEnablingGranted());
//		basicDensityFactorConiferous.setEnabled(!getCaller().basicWoodDensityFromModel && getCaller().permissions.isEnablingGranted());
//		basicDensityFactorBroadleaved.setEnabled(!getCaller().basicWoodDensityFromModel && getCaller().permissions.isEnablingGranted());
		
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
//			if (e.getSource().equals(basicDensityFactorConiferous)) {
//				getCaller().basicWoodDensityFactors.put(SpeciesType.ConiferousSpecies, Double.parseDouble(source.getText()));
//			} else if (e.getSource().equals(basicDensityFactorBroadleaved)) {
//				getCaller().basicWoodDensityFactors.put(SpeciesType.BroadleavedSpecies, Double.parseDouble(source.getText()));
//			} else 
			if (e.getSource().equals(carbonContentConiferous)) {
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
		} else if (arg0.getSource().equals(close)) {
			okAction();
		} else if (arg0.getSource().equals(help)) {
			helpAction();
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
