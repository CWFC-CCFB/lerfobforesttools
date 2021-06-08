/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.carbonbalancetool.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATCompatibleStand;
import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import repicea.gui.components.REpiceaMatchSelector;
import repicea.io.tools.ImportFieldElement;
import repicea.io.tools.ImportFieldElement.FieldType;
import repicea.io.tools.LevelProviderEnum;
import repicea.io.tools.REpiceaRecordReader;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CATGrowthSimulationRecordReader extends REpiceaRecordReader {

	protected static enum MessageID implements TextableEnum {
		DateDescription("Date (years)","Date (ann\u00E9es)"),
		DateHelp("This field must contains the date. It is an integer.", "Ce champ doit contenir la date. Il s'agit d'un entier."),
		RealizationDescription("Realization", "R\u00E9alisation"),
		RealizationHelp("This field must contain the realization identifier. It is an integer. This field is optional. If it is not specified, CAT considers that the simulation is deterministic.", 
				"Ce champ doit contenir l'identifiant de la r\u00E9alisation. Il s'agit d'un entier. Ce champ est facultatif. S'il n'est pas sp\u00E9cifi\u00E9, CAT consid\u00E8re qu'il s'agit d'un simulation d\u00E9terministe."),
		PlotDescription("Plot ID", "Identifiant de placette"),
		PlotHelp("This field must contain the plot identifier. It is a string.", "Ce champ doit contenir l'identifiant de la placette. Il s'agit d'une cha\u00EEne de caract\u00E8res."),
		PlotAreaDescription("Plot area (ha)", "Surface de la placette (ha)"),
		PlotAreaHelp("This field must contain the plot area in hectare. It is a double.", "Ce champ doit contenir la surface de la placette en hectare. Il s'agit d'un double."),
		TreeSpeciesDescription("Species", "Esp\u00E8ce"),
		TreeSpeciesHelp("This field must contain the species name. It is a string", "Ce champ doit contenir le nom de l'esp\u00E8ce. Il s'agit d'une cha\u00EEine de caract\u00E8res."),
		TreeStatusDescription("Tree status", "Etat de l'arbre"),
		TreeStatusHelp("This field must contain the status of the tree. It must be one of these four values:  alive, cut, dead, or windfall.", 
				"Ce champ doit contenir l'\u00E9tat de l'arbre. La valeur doit \u00EAtre l'une des quatre valeurs suivantes: alive, cut, dead, or windfall."),
		TreeDBHDescription("Tree DBH", "Diam\u00E8tre d'arbre"),
		TreeDBHHelp("This field must contain the diameter at breast height (cm). It is a double. This field is optional.",
				"Ce champ doit contenir le diam\u00E8tre \u00E0 1,3 m (cm). Il s'agit d'un double. Ce champ est facultatif."),
		TreeFreqDescription("Tree frequency", "Fr\u00E9quence d'arbre"),
		TreeFreqHelp("This field must contain the number of trees represented by the record in the plot. It is a double. This field is optional. If it is not specified, CAT assumes that the frequency is one.",
				"Ce champ doit contenir le nombre d'arbres repr\u00E9sent\u00E9s par cet enregistrement dans la placette. Il s'agit d'un double. Ce champ est facultatif. S'il n'est pas sp\u00E9cifi\u00E9, CAT utilise une fr\u00E9quence unitaire."),
		TreeVolumeDescription("Tree overbark volume (dm3)", "Volume d'arbre sur \u00E9corce (dm3)"),
		TreeVolumeHelp("This field contains the commercial volume (dm3) for a single tree. It is a double.", "Ce champ contient le volume commercial (dm3) d'un arbre individuel. Il s'agit d'un double."),
		InconsistentGrowthSimulation("The number of realizations is inconsistent along the projection!", "Le nombre de r\u00E9alisations n'est pas constant tout au long de la simulation!");
		
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

	protected static enum CATGrowthSimulationFieldLevel {Stand, Tree;}

	protected static enum CATGrowthSimulationFieldID implements LevelProviderEnum {
		Date(CATGrowthSimulationFieldLevel.Stand),
		Realization(CATGrowthSimulationFieldLevel.Stand),
		PlotID(CATGrowthSimulationFieldLevel.Stand),
		PlotArea(CATGrowthSimulationFieldLevel.Stand),
		Species(CATGrowthSimulationFieldLevel.Tree),
		Status(CATGrowthSimulationFieldLevel.Tree),
		DBH(CATGrowthSimulationFieldLevel.Tree),
		Freq(CATGrowthSimulationFieldLevel.Tree),
		Volume(CATGrowthSimulationFieldLevel.Tree);
		
		private final CATGrowthSimulationFieldLevel level;
		
		CATGrowthSimulationFieldID(CATGrowthSimulationFieldLevel level) {
			this.level = level;
		}

		@Override
		public CATGrowthSimulationFieldLevel getFieldLevel() {return level;}
	}

	
	private final Map<Integer, CATGrowthSimulationCompositeStand> standMap;
	
	private CATGrowthSimulationSpeciesSelector selector;
	private final List<String> speciesList;
	
	
	/**
	 * General constructor.
	 */
	public CATGrowthSimulationRecordReader() {
		super();
		setPopUpWindowEnabled(true);
		standMap = new TreeMap<Integer, CATGrowthSimulationCompositeStand>();
		speciesList = new ArrayList<String>();
	}

	/**
	 * This method returns the selector for the species in CAT.
	 * @return a REpiceaMatchSelector instance
	 */
	public REpiceaMatchSelector<CATSpecies> getSelector() {
		if (selector == null) {
			selector = new CATGrowthSimulationSpeciesSelector(speciesList.toArray());
		}
		return selector;
	}
	
	@Override
	protected List<ImportFieldElement> defineFieldsToImport() throws Exception {
		List<ImportFieldElement> ifeList = new ArrayList<ImportFieldElement>();
		ImportFieldElement ife;
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Date,
				MessageID.DateDescription.toString(), 
				getClass().getSimpleName() + ".dateDescription", 
				false, 
				MessageID.DateHelp.toString(),
				FieldType.Integer);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Realization,
				MessageID.RealizationDescription.toString(), 
				getClass().getSimpleName() + ".realizationDescription", 
				true, 
				MessageID.RealizationHelp.toString(),
				FieldType.Integer);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.PlotID,
				MessageID.PlotDescription.toString(), 
				getClass().getSimpleName() + ".plotDescription", 
				false, 
				MessageID.PlotHelp.toString(),
				FieldType.String);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.PlotArea,
				MessageID.PlotAreaDescription.toString(), 
				getClass().getSimpleName() + ".plotAreaDescription", 
				false, 
				MessageID.PlotAreaHelp.toString(),
				FieldType.Double);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Species,
				MessageID.TreeSpeciesDescription.toString(), 
				getClass().getSimpleName() + ".treeSpeciesDescription", 
				false, 
				MessageID.TreeSpeciesHelp.toString(),
				FieldType.String);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Status,
				MessageID.TreeStatusDescription.toString(), 
				getClass().getSimpleName() + ".treeStatusDescription", 
				false, 
				MessageID.TreeStatusHelp.toString(),
				FieldType.String);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.DBH,
				MessageID.TreeDBHDescription.toString(), 
				getClass().getSimpleName() + ".treeDBHDescription", 
				true, 
				MessageID.TreeDBHHelp.toString(),
				FieldType.String);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Freq,
				MessageID.TreeFreqDescription.toString(), 
				getClass().getSimpleName() + ".treeFreqDescription", 
				true, 
				MessageID.TreeFreqHelp.toString(),
				FieldType.Double);
		ifeList.add(ife);
		ife = new ImportFieldElement(CATGrowthSimulationFieldID.Volume,
				MessageID.TreeVolumeDescription.toString(), 
				getClass().getSimpleName() + ".treeVolumeDescription", 
				false, 
				MessageID.TreeVolumeHelp.toString(),
				FieldType.Double);
		ifeList.add(ife);
		return ifeList;
	}

	@Override
	protected Enum<?> defineGroupFieldEnum() {return null;}

	@Override
	protected void readLineRecord(Object[] oArray, int lineCounter) throws VariableValueException, Exception {
		int index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Date);
		int dateYr = Integer.parseInt(oArray[index].toString());

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Realization);
		int realization = 0;			// default value for this optional field
		if (oArray[index] != null) {	// means that a realization field has been specified
			realization = Integer.parseInt(oArray[index].toString());
		} 

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.PlotID);
		String plotID = oArray[index].toString();

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.PlotArea);
		double plotAreaHa = Double.parseDouble(oArray[index].toString());
		
		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Species);
		String originalSpeciesName = oArray[index].toString();
		
		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Status);
		StatusClass statusClass = StatusClass.valueOf(oArray[index].toString().toLowerCase().trim());

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.DBH);
		Double dbhCm = null;
		if (oArray[index] != null) { 	// means that a realization field has been specified
			dbhCm = Double.parseDouble(oArray[index].toString());
		}

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Freq);
		double numberOfTrees = 1d;		// default value for this optional field
		if (oArray[index] != null) { 	// means that a realization field has been specified
			numberOfTrees = Double.parseDouble(oArray[index].toString());
		}

		index = getImportFieldManager().getIndexOfThisField(CATGrowthSimulationFieldID.Volume);
		double treeOverbarkVolumeDm3 = Double.parseDouble(oArray[index].toString());

		instantiatePlotAndTree(getImportFieldManager().getFileSpecifications()[0], dateYr, realization, plotID, plotAreaHa, 
				statusClass, treeOverbarkVolumeDm3, numberOfTrees, originalSpeciesName, dbhCm);
	}
	
	protected void instantiatePlotAndTree(String standIdentification, int dateYr, int realization, String plotID, double plotAreaHa,
			StatusClass statusClass, double treeOverbarkVolumeDm3, double numberOfTrees, String originalSpeciesName, Double dbhCm) {
		if (!standMap.containsKey(dateYr)) {
			standMap.put(dateYr, new CATGrowthSimulationCompositeStand(dateYr, standIdentification, this));
		}
		
		CATGrowthSimulationCompositeStand compositeStand = standMap.get(dateYr);
		compositeStand.createRealization(realization);
		CATGrowthSimulationPlotSample plotSample = compositeStand.getRealization(realization);
		plotSample.createPlot(plotID, plotAreaHa);
		CATGrowthSimulationPlot plot = plotSample.getPlot(plotID);

		CATGrowthSimulationTree tree;
		if (dbhCm == null) {
			tree = new CATGrowthSimulationTree(plot, statusClass, treeOverbarkVolumeDm3, numberOfTrees, originalSpeciesName);
		} else {
			tree = new CATGrowthSimulationTreeWithDBH(plot, statusClass, treeOverbarkVolumeDm3, numberOfTrees, originalSpeciesName, dbhCm);
		}
		
		plot.addTree(tree);
		if (!speciesList.contains(originalSpeciesName)) {
			speciesList.add(originalSpeciesName);
		}

	}

	@Override
	public void readRecordsForThisGroupId(int groupId) throws Exception {
		standMap.clear();
		super.readRecordsForThisGroupId(groupId);
	}

	@Override 
	public void readAllRecords() throws Exception {
		super.readAllRecords();
		ensureValidityInCasesOfEmptyPlots();
	}
	
	
	private void ensureValidityInCasesOfEmptyPlots() throws Exception {
		Map<String, Double> plotIDAndAreaHa = new HashMap<String, Double>();
		List<Integer> refList = null;
		for (CATGrowthSimulationCompositeStand stand : standMap.values()) {
			List<Integer> monteCarloIds = stand.getRealizationIds();
			if (refList == null) {
				refList = monteCarloIds;
			} else if (!refList.equals(monteCarloIds)) {
				throw new Exception(MessageID.InconsistentGrowthSimulation.toString());
			}
			for (Integer i : monteCarloIds) {
				CATGrowthSimulationPlotSample plotSample = stand.getRealization(i);
				for (String pId : plotSample.getPlotMap().keySet()) {
					if (!plotIDAndAreaHa.containsKey(pId)) {
						plotIDAndAreaHa.put(pId, plotSample.getPlot(pId).getAreaHa());
					}
				}
			}
		}
		
		for (CATGrowthSimulationCompositeStand stand : standMap.values()) {
			for (Integer i : refList) {
				CATGrowthSimulationPlotSample sample = stand.getRealization(i);
				for (String pId : plotIDAndAreaHa.keySet()) {
					if (!sample.getPlotMap().containsKey(pId)) {
						sample.createPlot(pId, plotIDAndAreaHa.get(pId));
					}
				}
			}
		}
	}

	/**
	 * This method returns the stand list that was last read.
	 * @return a list of CATCompatibleStand instances
	 */
	public List<CATCompatibleStand> getStandList() {
		List<CATCompatibleStand> standList = new ArrayList<CATCompatibleStand>();
		for (CATGrowthSimulationCompositeStand stand : standMap.values()) {
			standList.add(stand);
		}
		return standList;
	}
	
	protected List<String> getSpeciesList() {return speciesList;}
}
