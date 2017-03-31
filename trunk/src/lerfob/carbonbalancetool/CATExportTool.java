/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.app.SettingMemory;
import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.io.GExportFieldDetails;
import repicea.io.GExportRecord;
import repicea.io.REpiceaRecordSet;
import repicea.io.tools.REpiceaExportTool;
import repicea.io.tools.REpiceaExportToolDialog;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CATExportTool extends REpiceaExportTool {

	protected static enum MessageID implements TextableEnum {
		Year("Year", "Annee"),
		Compartment("Compart", "Compart"),
		CarbonHaMean("tCHaMean", "tCHaMoy"),
		StockEvolutionOption("Carbon stock and flux evolution", "Evolution des stocks et des flux de carbone"),
		AverageStockOption("Average carbon stocks and fluxes", "Stocks et flux moyens"),
		ProductProportionOption("Proportions of the different wood product classes", "Proportions des diff\u00E9rentes classes de produits"),
		AnnualFluxOption("Annual volume and nutrient fluxes", "Flux annuels en volume et min\u00E9ralomasse"),
		LogCategoryVolume("Log category volumes", "Volumes des cat\u00E9gories de billons"),
		WoodProductEvolution("Wood product evolution", "Evolution des produits bois"),
		Variance("Variance", "Variance");
			
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
	
	
	private static class InternalSwingWorker extends InternalSwingWorkerForRecordSet {

		private CATExportTool caller;
		
		@SuppressWarnings("rawtypes")
		protected InternalSwingWorker(CATExportTool caller, Enum selectedOption, REpiceaRecordSet recordSet) {
			super(selectedOption, recordSet);
			this.caller = caller;
		}

		@Override
		protected void doThisJob() throws Exception {
			switch ((ExportOption) getExportOption()) {
			case CarbonStockEvolution:
				createCarbonStockEvolutionRecordSet();
				break;
			case AverageCarbonStock:
				createAverageCarbonStockRecordSet();
				break;
			case ProductProportion:
				createWoodProductProportionRecordSet();
				break;
			case AnnualVolumeNutrientFluxes:
				createNutrientFluxRecordSet();
				break;
			case LogCategoryVolume:
				createLogCategoryVolumeRecordSet();
				break;
			case WoodProductEvolution:
				createWoodProductEvolutionRecordSet();
				break;
			default:
				throw new Exception("Unrecognized Export Format");
			}
			
		}
		
		
		
		private void createWoodProductEvolutionRecordSet() throws Exception {
			GExportRecord r;
						
			Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> productMap = caller.summary.getProductEvolutionPerHa();
			
			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);
			List<Integer> dates = new ArrayList<Integer>();
			dates.addAll(productMap.keySet());
			Collections.sort(dates);
			
			for (Integer date : dates) {
				GExportFieldDetails dateIDField = new GExportFieldDetails("Date", date);
				Map<UseClass, Map<Element, MonteCarloEstimate>> innerMap = productMap.get(date);
				Map<Element, MonteCarloEstimate> carrier;
				for (UseClass useClass : UseClass.values()) {
					if (innerMap.containsKey(useClass)) {
						carrier = innerMap.get(useClass);
						MonteCarloEstimate volumeEstimate = carrier.get(Element.Volume);
						MonteCarloEstimate biomassEstimate = carrier.get(Element.Biomass);
						int nbRealizations = volumeEstimate.getNumberOfRealizations();
						for (int j = 0; j < nbRealizations; j++) {
							r = new GExportRecord();
							r.addField(standIDField);
							r.addField(dateIDField);
							r.addField(new GExportFieldDetails("UseClass", useClass.name()));
							r.addField(new GExportFieldDetails("Volume_m3ha", volumeEstimate.getRealizations().get(j).m_afData[0][0]));
							r.addField(new GExportFieldDetails("Biomass_kgha", biomassEstimate.getRealizations().get(j).m_afData[0][0] * 1000));
							if (nbRealizations > 0) {
								r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
							}
							addRecord(r);
						}
					}
				}
			}
		}

		private void createCarbonStockEvolutionRecordSet() throws Exception {
			GExportRecord r;
			
			CATTimeTable timeScale = caller.summary.getTimeTable();
			
			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);
			for (CompartmentInfo compartmentInfo : CompartmentInfo.values()) {
				MonteCarloEstimate estimate = caller.summary.getEvolutionMap().get(compartmentInfo);
				int nbRealizations = estimate.getNumberOfRealizations();
				for (int i = 0; i < timeScale.size(); i++) {
					for (int j = 0; j < nbRealizations; j++) {
						double value = estimate.getRealizations().get(j).m_afData[i][0];
						if (caller.summary.isEvenAged() && i == 0) {
							r = new GExportRecord();
							r.addField(standIDField);
							r.addField(new GExportFieldDetails(MessageID.Year.toString(), (Integer) 0));
							r.addField(new GExportFieldDetails(MessageID.Compartment.toString(), compartmentInfo.toString()));
							r.addField(new GExportFieldDetails(MessageID.CarbonHaMean.toString(), (Double) 0d));
							if (nbRealizations > 0) {
								r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
							}
							addRecord(r);
						}
						r = new GExportRecord();
						r.addField(standIDField);
						r.addField(new GExportFieldDetails(MessageID.Year.toString(), timeScale.get(i)));
						r.addField(new GExportFieldDetails(MessageID.Compartment.toString(), compartmentInfo.toString()));
						r.addField(new GExportFieldDetails(MessageID.CarbonHaMean.toString(), value));
						if (nbRealizations > 0) {
							r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
						}
				
						addRecord(r);
					}
				}
			}
		}
		
		
		private void createAverageCarbonStockRecordSet() throws Exception {
			GExportRecord r;

			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);

			for (CompartmentInfo compartmentInfo : CompartmentInfo.values()) {
				Estimate<?> estimate = caller.summary.getBudgetMap().get(compartmentInfo);
				if (estimate instanceof MonteCarloEstimate) {
					int nbRealizations = ((MonteCarloEstimate) estimate).getNumberOfRealizations();
					for (int j = 0; j < nbRealizations; j++) {
						double value = ((MonteCarloEstimate) estimate).getRealizations().get(j).m_afData[0][0];
						r = new GExportRecord();
						r.addField(standIDField);
						r.addField(new GExportFieldDetails(MessageID.Compartment.toString(), compartmentInfo.toString()));
						r.addField(new GExportFieldDetails(MessageID.CarbonHaMean.toString(), value));
						if (nbRealizations > 0) {
							r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
						}
						addRecord(r);
					}
				} else {
					r = new GExportRecord();
					r.addField(standIDField);
					r.addField(new GExportFieldDetails(MessageID.Compartment.toString(), compartmentInfo.toString()));
					r.addField(new GExportFieldDetails(MessageID.CarbonHaMean.toString(), estimate.getMean().m_afData[0][0]));
					r.addField(new GExportFieldDetails(MessageID.Variance.toString(), estimate.getVariance().m_afData[0][0]));
				}
			}
		}
		

		private void createWoodProductProportionRecordSet() throws Exception {
			GExportRecord r;
			
			Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> volumeProducts = caller.summary.getHWPPerHaByUseClass();		// no recycling
			
			double nutrientKg;
			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);
			
			for (CarbonUnitStatus type : CarbonUnitStatus.values()) {
				if (volumeProducts.containsKey(type)) {
					Map<UseClass, Map<Element, MonteCarloEstimate>> innerVolumeMap = volumeProducts.get(type);
					Map<Element, MonteCarloEstimate> carrier;
					for (UseClass useClass : UseClass.values()) {
						if (innerVolumeMap.containsKey(useClass)) {
							carrier = innerVolumeMap.get(useClass);
							MonteCarloEstimate volumeEstimate = carrier.get(Element.Volume);
							MonteCarloEstimate biomassEstimate = carrier.get(Element.Biomass);
							int nbRealizations = volumeEstimate.getNumberOfRealizations();
							for (int j = 0; j < nbRealizations; j++) {
								r = new GExportRecord();
								r.addField(standIDField);
								r.addField(new GExportFieldDetails("Type", type.name()));
								r.addField(new GExportFieldDetails("Class", useClass.toString()));
								r.addField(new GExportFieldDetails("Volume_m3ha", volumeEstimate.getRealizations().get(j).m_afData[0][0]));
								r.addField(new GExportFieldDetails("Biomass_kgha", biomassEstimate.getRealizations().get(j).m_afData[0][0] * 1000));
								for (Element nutrient : Element.getNutrients()) {
									nutrientKg = 0d;
									if (carrier != null && carrier.containsKey(nutrient)) {
										nutrientKg = carrier.get(nutrient).getRealizations().get(j).m_afData[0][0];
									}
									if (nutrient.equals(Element.C)) {
										nutrientKg *= 1000;
									}
									r.addField(new GExportFieldDetails(nutrient.name() + "_kg_ha", (Double) nutrientKg));
								}
								if (nbRealizations > 0) {
									r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
								}
								addRecord(r);
							}
						}
					}
				}
			}
		}

		
		private void createNutrientFluxRecordSet() throws Exception {
			GExportRecord r;
			double annualFactor = 1d / caller.summary.getRotationLength();
			
			Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> volumeProducts = caller.summary.getHWPPerHaByUseClass();
			
			double nutrientKg;
			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);
			
			for (CarbonUnitStatus type : CarbonUnitStatus.values()) { 
				if (volumeProducts.containsKey(type)) {
					Map<UseClass, Map<Element, MonteCarloEstimate>> innerVolumeMap = volumeProducts.get(type);
					Map<Element, MonteCarloEstimate> carrier;
					for (UseClass useClass : UseClass.values()) {
						if (innerVolumeMap.containsKey(useClass)) {
							carrier = innerVolumeMap.get(useClass);
							MonteCarloEstimate volumeEstimate = carrier.get(Element.Volume);
							MonteCarloEstimate biomassEstimate = carrier.get(Element.Biomass);
							int nbRealizations = volumeEstimate.getNumberOfRealizations();
							for (int j = 0; j < nbRealizations; j++) {
								r = new GExportRecord();
								r.addField(standIDField);
								r.addField(new GExportFieldDetails("Type", type.name()));
								r.addField(new GExportFieldDetails("Class", useClass.toString()));
								r.addField(new GExportFieldDetails("Volume_m3hayr", volumeEstimate.getRealizations().get(j).m_afData[0][0] * annualFactor));
								r.addField(new GExportFieldDetails("Biomass_kghayr", biomassEstimate.getRealizations().get(j).m_afData[0][0] * 1000 * annualFactor));
								for (Element nutrient : Element.getNutrients()) {
									nutrientKg = 0d;
									if (carrier != null && carrier.containsKey(nutrient)) {
										nutrientKg = carrier.get(nutrient).getRealizations().get(j).m_afData[0][0];
									}
									if (nutrient.equals(Element.C)) {
										nutrientKg *= 1000;
									}
									r.addField(new GExportFieldDetails(nutrient.name() + "_kghayr", (Double) (nutrientKg * annualFactor)));
								}
								if (nbRealizations > 0) {
									r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
								}
								addRecord(r);
							}
						}
					}
				}
			}
		}

		private void createLogCategoryVolumeRecordSet() throws Exception {
			GExportRecord r;
			
			String standID = caller.summary.getStandID();
			if (standID == null || standID.isEmpty()) {
				standID = "Unknown";
			}
			GExportFieldDetails standIDField = new GExportFieldDetails("StandID", standID);
			Map<Element, MonteCarloEstimate> carrier;
			
			List<String> logNames = new ArrayList<String>(caller.summary.getLogGradePerHa().keySet());
			Collections.sort(logNames);
			
			for (String logName : logNames) {
				carrier = caller.summary.getLogGradePerHa().get(logName);
				MonteCarloEstimate volumeEstimate = carrier.get(Element.Volume);
				MonteCarloEstimate biomassEstimate = carrier.get(Element.Biomass);
				int nbRealizations = volumeEstimate.getNumberOfRealizations();
				for (int j = 0; j < nbRealizations; j++) {
					r = new GExportRecord();
					r.addField(standIDField);
					r.addField(new GExportFieldDetails("LogCategory", logName));
					r.addField(new GExportFieldDetails("Volume_m3ha", volumeEstimate.getRealizations().get(j).m_afData[0][0]));
					r.addField(new GExportFieldDetails("Biomass_kgha", biomassEstimate.getRealizations().get(j).m_afData[0][0] * 1000));
					if (nbRealizations > 0) {
						r.addField(new GExportFieldDetails("RealizationID", (Integer) j+1));
					}
			
					addRecord(r);
				}
			}
		}
	}
	
	
	
	private final CATSimulationResult summary;
	private final SettingMemory settings;
	
	public enum ExportOption {
		CarbonStockEvolution, 
		AverageCarbonStock, 
		ProductProportion,
		AnnualVolumeNutrientFluxes,
		LogCategoryVolume,
		WoodProductEvolution;
		
	
		private static Map<ExportOption, String> optionNames;
		
		@Override
		public String toString() {return getOptionNames().get(this);}
		
		private static Map<ExportOption, String> getOptionNames() {
			if (optionNames == null) {
				optionNames = new HashMap<ExportOption, String>();
				optionNames.put(ExportOption.CarbonStockEvolution, MessageID.StockEvolutionOption.toString());
				optionNames.put(ExportOption.AverageCarbonStock, MessageID.AverageStockOption.toString());
				optionNames.put(ExportOption.ProductProportion, MessageID.ProductProportionOption.toString());
				optionNames.put(ExportOption.AnnualVolumeNutrientFluxes, MessageID.AnnualFluxOption.toString());
				optionNames.put(ExportOption.LogCategoryVolume, MessageID.LogCategoryVolume.toString());
				optionNames.put(ExportOption.WoodProductEvolution, MessageID.WoodProductEvolution.toString());
						}
			return optionNames;
		}
	
	}
	
	/**
	 * Constructor. 
	 * @param summary a CarbonAccountingToolExportSummary instance
	 * @throws Exception 
	 */
	protected CATExportTool(CATSettings carbonSettings, CATSimulationResult summary) throws Exception { 
		super(true);			// enables multiple selection 
		this.summary = summary;
		this.settings = carbonSettings.getSettingMemory();
		String defaultPath = settings.getProperty("CarbonBudgetExportDialog.defaultPath", "export.dbf");
		setFilename(defaultPath);
	}

	/**
	 * This method sets the helper of this dialog.
	 * @param helper an AutomatedHelper instance
	 */
	protected void setHelper(AutomatedHelper helper) {
		UIControlManager.setHelpMethod(REpiceaExportToolDialog.class, helper);
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public Map<Enum, REpiceaRecordSet> exportRecordSets() throws Exception {
		Map<Enum, REpiceaRecordSet> outputMap =	super.exportRecordSets();
		settings.setProperty("CarbonBudgetExportDialog.defaultPath", getFilename());
		return outputMap;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Vector<Enum> defineAvailableExportOptions() {
		Vector<Enum> availableOptions = new Vector<Enum>();
		for (ExportOption exportOption : ExportOption.values()) {
			availableOptions.add(exportOption);
		}
		return availableOptions;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List<Enum> getAvailableExportOptions() {
		if (summary instanceof CATSimulationDifference) {
			Vector<Enum> hackedVector = new Vector<Enum>();
			hackedVector.add(ExportOption.AverageCarbonStock);
			return hackedVector;
		} else {
			return super.getAvailableExportOptions();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption, REpiceaRecordSet recordSet) {
		return new InternalSwingWorker(this, selectedOption, recordSet);
	}

}