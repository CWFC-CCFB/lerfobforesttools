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

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.filechooser.FileFilter;

import lerfob.carbonbalancetool.CATAboveGroundBiomassProvider;
import lerfob.carbonbalancetool.CATAboveGroundCarbonProvider;
import lerfob.carbonbalancetool.CATAboveGroundVolumeProvider;
import lerfob.carbonbalancetool.CATBasicWoodDensityProvider;
import lerfob.carbonbalancetool.CATBelowGroundBiomassProvider;
import lerfob.carbonbalancetool.CATBelowGroundCarbonProvider;
import lerfob.carbonbalancetool.CATBelowGroundVolumeProvider;
import lerfob.carbonbalancetool.CATCarbonContentRatioProvider;
import lerfob.carbonbalancetool.CATCompatibleTree;
import lerfob.carbonbalancetool.biomassparameters.BiomassParametersDialog.MessageID;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.Resettable;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.io.IOUserInterfaceableObject;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;

public class BiomassParameters implements REpiceaShowableUIWithParent, IOUserInterfaceableObject, Resettable, Memorizable {

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.CarbonToolCompatibleTree$SpeciesType",	
				"repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider$SpeciesType");
	}
	
	private static class BiomassParametersFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".bpf";
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory() || f.getAbsolutePath().toLowerCase().trim().endsWith(extension)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getDescription() {
			return MessageID.BiomassParametersFileExtension.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

	private static final BiomassParametersFileFilter MyFileFilter = new BiomassParametersFileFilter();
	
	protected final HashMap<SpeciesType, Double> branchExpansionFactors;
	protected final HashMap<SpeciesType, Double> rootExpansionFactors;

	/*
	 * This biomass density factors are for species groups as in the French context (e.g. Broadleaved vs coniferous). The
	 * basic density factors provided by the CATSpecies class should be used instead.
	 */
	@Deprecated
	protected final HashMap<SpeciesType, Double> basicWoodDensityFactors;
	protected final HashMap<SpeciesType, Double> carbonContentFactors;

	protected boolean rootExpansionFactorFromModelEnabled;
	protected boolean branchExpansionFactorFromModelEnabled;
	protected boolean basicWoodDensityFromModelEnabled;
	protected boolean carbonContentFromModelEnabled;
	
	protected boolean rootExpansionFactorFromModel;
	protected boolean branchExpansionFactorFromModel;
	protected boolean basicWoodDensityFromModel;
	protected boolean carbonContentFromModel;

	
	private transient BiomassParametersDialog guiInterface;
	
	private transient Object referent;	// check the compatibility with the referent when deserializing 

	private String filename;
	
	protected transient REpiceaGUIPermission permissions = new DefaultREpiceaGUIPermission(true);

	/**
	 * Empty constructor for class.newInstance() call.
	 */
	public BiomassParameters(REpiceaGUIPermission permissions) {
		if (permissions != null) {
			this.permissions = permissions;
		}
		branchExpansionFactors = new HashMap<SpeciesType, Double>();
		rootExpansionFactors = new HashMap<SpeciesType, Double>();
		basicWoodDensityFactors = new HashMap<SpeciesType, Double>();
		carbonContentFactors = new HashMap<SpeciesType, Double>();
		reset();
	}
	
	/**
	 * Empty constructor for class.newInstance() call.
	 */
	public BiomassParameters() {
		this(new DefaultREpiceaGUIPermission(true));
	}

	
	public void setReferent(Object referent) {
		this.referent = referent; 
		testReferent(referent);
	}
	

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}


	@Override
	public void reset() {
		setFilename(System.getProperty("user.home") + File.separator + BiomassParametersDialog.MessageID.Unnamed.toString());
		branchExpansionFactors.put(SpeciesType.BroadleavedSpecies, 1.612);
		branchExpansionFactors.put(SpeciesType.ConiferousSpecies, 1.300);
		branchExpansionFactorFromModel = false;
		branchExpansionFactorFromModelEnabled = false;
		
		rootExpansionFactors.put(SpeciesType.BroadleavedSpecies, 1.280);
		rootExpansionFactors.put(SpeciesType.ConiferousSpecies, 1.300);
		rootExpansionFactorFromModel = false;
		rootExpansionFactorFromModelEnabled = false;

		basicWoodDensityFactors.put(SpeciesType.BroadleavedSpecies, 0.500);
		basicWoodDensityFactors.put(SpeciesType.ConiferousSpecies, 0.350);
		carbonContentFactors.put(SpeciesType.BroadleavedSpecies, CATCarbonContentRatioProvider.AverageCarbonContent.Hardwood.getRatio());
		carbonContentFactors.put(SpeciesType.ConiferousSpecies, CATCarbonContentRatioProvider.AverageCarbonContent.Softwood.getRatio());
		
		basicWoodDensityFromModel = false;
		basicWoodDensityFromModelEnabled = false;
		carbonContentFromModel = false;
		carbonContentFromModelEnabled = false;
		if (referent != null) {
			testReferent(referent);
		}
	}

	private void testReferent(Object referent) {
		branchExpansionFactorFromModelEnabled = referent instanceof CATAboveGroundVolumeProvider || referent instanceof CATAboveGroundBiomassProvider || referent instanceof CATAboveGroundCarbonProvider;
		rootExpansionFactorFromModelEnabled = referent instanceof CATBelowGroundVolumeProvider || referent instanceof CATBelowGroundBiomassProvider || referent instanceof CATBelowGroundCarbonProvider;
		basicWoodDensityFromModelEnabled = referent instanceof CATBasicWoodDensityProvider;
		carbonContentFromModelEnabled = referent instanceof CATCarbonContentRatioProvider;
	}

	public boolean isValid() {
		if (!branchExpansionFactorFromModelEnabled && branchExpansionFactorFromModel) {
			return false;
		} else if (!rootExpansionFactorFromModelEnabled && rootExpansionFactorFromModel) {
			return false;
		} else if (!basicWoodDensityFromModelEnabled && basicWoodDensityFromModel) {
			return false;
		} else if (!carbonContentFromModelEnabled && carbonContentFromModel) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Component getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new BiomassParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	@Override
	public void save(String filename) throws IOException {
		setFilename(filename);
		XmlSerializer serializer = new XmlSerializer(filename);
		try {
			serializer.writeObject(this);
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while saving the file!");
		}
	}

	public String getName() {
		File file = new File(filename);
		return ObjectUtility.relativizeTheseFile(file.getParentFile(), file).toString();
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer;
		try {
			deserializer = new XmlDeserializer(filename);
		} catch (Exception e) {
			InputStream is = getClass().getResourceAsStream("/" + filename);
			if (is == null) {
				throw new IOException("The filename is not a file and cannot be converted into a stream!");
			} else {
				deserializer = new XmlDeserializer(is);
			}
		}
		BiomassParameters newManager;
		try {
			newManager = (BiomassParameters) deserializer.readObject();
			newManager.setFilename(filename);
			unpackMemorizerPackage(newManager.getMemorizerPackage());
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while loading the file!");
		}
	}

	private void setFilename(String filename) {this.filename = filename;}
	
	@Override
	public String getFilename() {return filename;}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(filename);
		mp.add(branchExpansionFactors);
		mp.add(rootExpansionFactors);
		mp.add(basicWoodDensityFactors);
		mp.add(carbonContentFactors);
		mp.add(branchExpansionFactorFromModel);
		mp.add(rootExpansionFactorFromModel);
		mp.add(basicWoodDensityFromModel);
		mp.add(carbonContentFromModel);
		return mp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		branchExpansionFactors.clear();
		rootExpansionFactors.clear();
		basicWoodDensityFactors.clear();
		carbonContentFactors.clear();
		filename = (String) wasMemorized.get(0);
		branchExpansionFactors.putAll((HashMap) wasMemorized.get(1));
		rootExpansionFactors.putAll((HashMap) wasMemorized.get(2));
		basicWoodDensityFactors.putAll((HashMap) wasMemorized.get(3));
		carbonContentFactors.putAll((HashMap) wasMemorized.get(4));
		branchExpansionFactorFromModel = (Boolean) wasMemorized.get(5);
		rootExpansionFactorFromModel = (Boolean) wasMemorized.get(6);
		basicWoodDensityFromModel = (Boolean) wasMemorized.get(7);
		carbonContentFromModel = (Boolean) wasMemorized.get(8);
	}
	
	
	
	
	/**
	 * This method returns the basic density factor for this tree.
	 * @param tree a CarbonToolCompatibleTree
	 * @return a double (Mg/m3)
	 */
	public double getBasicWoodDensityFromThisTree(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = basicWoodDensityFromModel && tree instanceof CATBasicWoodDensityProvider;
		boolean isStochastic = false;
		double value;
		if (tier2Implementation) {
			CATBasicWoodDensityProvider t = (CATBasicWoodDensityProvider) tree;
			value = t.getBasicWoodDensity();
			isStochastic = t.isBasicWoodDensityPredictorStochastic();
		} else {
			value = tree.getCATSpecies().getBasicWoodDensity();
		}
		if (subject != null && !isStochastic) {	// isStochastic = false if the provider is not stochastic or if the tree does not implement the provider
			return value * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BasicDensity, subject, getSubjectId(VariabilitySource.BasicDensity, tree));
		} else {
			return value;
		}
	}

	/**
	 * This method returns the carbon content ratio for this tree, INCLUDING bark.
	 * @param tree a CarbonToolCompatibleTree
	 * @return a double
	 */
	public double getCarbonContentFromThisTree(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = carbonContentFromModel && tree instanceof CATCarbonContentRatioProvider;
		boolean isStochastic = false;
		double value;
		if (tier2Implementation) {
			CATCarbonContentRatioProvider t = (CATCarbonContentRatioProvider) tree;
			value = t.getCarbonContentRatio();
			isStochastic = t.isCarbonContentRatioPredictorStochastic();
		} else {
			value = carbonContentFactors.get(tree.getCATSpecies().getSpeciesType());
		}
		if (subject != null && !isStochastic) {	// isStochastic = false if the provider is not stochastic or if the tree does not implement the provider
			return value * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.CarbonContent, subject, getSubjectId(VariabilitySource.CarbonContent, tree));
		} else {
			return value;
		}
	}

	
	/**
	 * This method returns the belowground carbon content of a particular instance
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the carbon content (Mg)
	 */
	public double getBelowGroundCarbonMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = rootExpansionFactorFromModel && tree instanceof CATBelowGroundCarbonProvider;
		if (tier2Implementation) {
			CATBelowGroundCarbonProvider t = (CATBelowGroundCarbonProvider) tree;
			double value = t.getBelowGroundCarbonMg() * tree.getNumber() * tree.getPlotWeight();
			if (!t.isBelowGroundCarbonPredictorStochastic()) {	// will rely on sensitivity analysis instead
				double biomassModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, getSubjectId(VariabilitySource.BiomassExpansionFactor, tree));
				double woodDensityModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BasicDensity, subject, getSubjectId(VariabilitySource.BasicDensity, tree));
				double carbonModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.CarbonContent, subject, getSubjectId(VariabilitySource.CarbonContent, tree));
				value *= biomassModifier * woodDensityModifier * carbonModifier;
			}
			return value;
		}
		return getBelowGroundBiomassMg(tree, subject) * getCarbonContentFromThisTree(tree, subject);
	}
	
	/**
	 * This method returns the belowground biomass of a particular instance
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the biomass (Mg)
	 */
	private double getBelowGroundBiomassMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = rootExpansionFactorFromModel && tree instanceof CATBelowGroundBiomassProvider;
		if (tier2Implementation) {
			CATBelowGroundBiomassProvider t = (CATBelowGroundBiomassProvider) tree;
			double value = t.getBelowGroundBiomassMg() * tree.getNumber() * tree.getPlotWeight();
			if (!t.isBelowGroundBiomassPredictorStochastic()) { // will rely on sensitivity analysis instead
				double biomassModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, getSubjectId(VariabilitySource.BiomassExpansionFactor, tree));
				double woodDensityModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BasicDensity, subject, getSubjectId(VariabilitySource.BasicDensity, tree));
				value *= biomassModifier * woodDensityModifier;
			}
			return value;
		}
		return getBelowGroundVolumeM3(tree, subject) * getBasicWoodDensityFromThisTree(tree, subject);
	}

	/**
	 * This method returns the belowground volume of a particular instance, INCLUDING bark and 
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the volume (M3)
	 */
	private double getBelowGroundVolumeM3(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = rootExpansionFactorFromModel && tree instanceof CATBelowGroundVolumeProvider;
		boolean isStochastic = false;
		double value;
		if (tier2Implementation) {
			CATBelowGroundVolumeProvider t = (CATBelowGroundVolumeProvider) tree;
			value = t.getBelowGroundVolumeM3() * tree.getNumber() * tree.getPlotWeight();
			isStochastic = t.isBelowGroundVolumePredictorStochastic();
		} else {
			value = getAboveGroundVolumeM3(tree, subject) * (rootExpansionFactors.get(tree.getCATSpecies().getSpeciesType()) - 1);		// minus 1 is required because we want to get only the belowground part
		}
		if (subject != null && !isStochastic) {	// isStochastic = false if the provider is not stochastic or if the tree does not implement the provider
			return value * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, getSubjectId(VariabilitySource.BiomassExpansionFactor, tree));
		} else {
			return value;
		}
	}

	
	/**
	 * This method returns the aboveground carbon of a particular tree (Mg), INCLUDING bark.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	public double getAboveGroundCarbonMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = branchExpansionFactorFromModel && tree instanceof CATAboveGroundCarbonProvider;
		if (tier2Implementation) {
			CATAboveGroundCarbonProvider t = (CATAboveGroundCarbonProvider) tree;
			double value = t.getAboveGroundCarbonMg() * tree.getNumber() * tree.getPlotWeight();
			if (!t.isAboveGroundCarbonPredictorStochastic()) {	// then rely on sensitivity analysis if enabled
				double biomassModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, getSubjectId(VariabilitySource.BiomassExpansionFactor, tree));
				double woodDensityModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BasicDensity, subject, getSubjectId(VariabilitySource.BasicDensity, tree));
				double carbonModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.CarbonContent, subject, getSubjectId(VariabilitySource.CarbonContent, tree));
				value *= biomassModifier * woodDensityModifier * carbonModifier;
			}
			return value;
		} 
		return getAboveGroundBiomassMg(tree, subject) * getCarbonContentFromThisTree(tree, subject);
	}
	
	/**
	 * This method returns the aboveground biomass of a particular tree in Mg.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	private double getAboveGroundBiomassMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = branchExpansionFactorFromModel && tree instanceof CATAboveGroundBiomassProvider;
		if (tier2Implementation) {
			CATAboveGroundBiomassProvider t = (CATAboveGroundBiomassProvider) tree;
			double value = t.getAboveGroundBiomassMg() * tree.getNumber() * tree.getPlotWeight();
			if (!t.isAboveGroundBiomassPredictorStochastic()) { // then rely on sensitivity analysis if enabled
				double biomassModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, getSubjectId(VariabilitySource.BiomassExpansionFactor, tree));
				double woodDensityModifier = CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BasicDensity, subject, getSubjectId(VariabilitySource.BasicDensity, tree));
				value *= biomassModifier * woodDensityModifier;
			}
			return value;
		} 
		return getAboveGroundVolumeM3(tree, subject) * getBasicWoodDensityFromThisTree(tree, subject);
	}

	
	/**
	 * This method returns the aboveground volume of a particular tree (m3), INCLUDING bark and
	 * all weighting factors.
	 * @param tree a CarbonCompatibleTree
	 * @param subject a MonteCarloSimulationCompliantObject 
	 * @return a double
	 */
	private double getAboveGroundVolumeM3(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		boolean tier2Implementation = branchExpansionFactorFromModel && tree instanceof CATAboveGroundVolumeProvider;
		boolean isStochastic = false;
		double value;
		if (tier2Implementation) {
			CATAboveGroundVolumeProvider t = (CATAboveGroundVolumeProvider) tree;
			value = t.getAboveGroundVolumeM3() * tree.getNumber() * tree.getPlotWeight();
			isStochastic = t.isAboveGroundVolumePredictorStochastic();
		} else {
			value = getCommercialVolumeM3ForThisTree(tree) * branchExpansionFactors.get(tree.getCATSpecies().getSpeciesType());
		}
		
		if (subject != null && !isStochastic) {	// isStochastic = false if the provider is not stochastic or if the tree does not implement the provider
			String subjectId = getSubjectId(VariabilitySource.BiomassExpansionFactor, tree);
			return value * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.BiomassExpansionFactor, subject, subjectId);
		} else {
			return value;
		}
	}

	protected String getSubjectId(VariabilitySource source, CATCompatibleTree tree) {
		switch(source) {
		case BiomassExpansionFactor:
			if (branchExpansionFactorFromModel || rootExpansionFactorFromModel) {
				return tree.getSpeciesName();
			} else {
				return tree.getCATSpecies().getSpeciesType().name();
			}
		case BasicDensity:
			if (basicWoodDensityFromModel) {
				return tree.getSpeciesName();
			} else {
				return tree.getCATSpecies().getSpeciesType().name();
			}
		case CarbonContent:
			if (carbonContentFromModel) {
				return tree.getSpeciesName();
			} else {
				return tree.getCATSpecies().getSpeciesType().name();
			}
		default:
			return null;
		}
	}
	
	
	
	/**
	 * This method returns the overbark commercial volume of the tree weighted by the expansion factor.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	private double getCommercialVolumeM3ForThisTree(CATCompatibleTree tree) {
		return getOverbarkCommercialVolumeM3(tree) * tree.getNumber() * tree.getPlotWeight();
	}
	

	/**
	 * This method returns the volume over bark of a CATCompatibleTree instance.
	 * @param tree a CATCompatibleTree instance
	 * @return the volume of a single tree, i.e. WITHOUT any expansion factor.
	 */
	private double getOverbarkCommercialVolumeM3(CATCompatibleTree tree) {
		double commVolume = tree.getCommercialVolumeM3();
		if (!tree.isCommercialVolumeOverbark()) {
			commVolume += tree.getBarkProportionOfWoodVolume() * commVolume;
		}
		return commVolume;
	}
	
	/**
	 * This method returns the commercial biomass of the tree weighted by the expansion factor.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	private double getCommercialBiomassMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		return getCommercialVolumeM3ForThisTree(tree) * getBasicWoodDensityFromThisTree(tree, subject);
	}

	/**
	 * This method returns the commercial biomass of the tree weighted by the expansion factor.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	public double getCommercialCarbonMg(CATCompatibleTree tree, MonteCarloSimulationCompliantObject subject) {
		return getCommercialBiomassMg(tree, subject) * getCarbonContentFromThisTree(tree, subject);
	}
	


//	/**
//	 * This method returns the aboveground volume for a collection of trees.
//	 * @param trees a Collection of CarbonToolCompatibleTree instances
//	 * @return a double
//	 */
//	public double getAboveGroundVolumeM3(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
//		double totalAboveGroundVolumeM3 = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				totalAboveGroundVolumeM3 += getAboveGroundVolumeM3(tree, subject);
//			}
//		}
//		return totalAboveGroundVolumeM3;
//	}

//	/**
//	 * This method returns the aboveground biomass for a collection of trees.
//	 * @param trees a Collection of CarbonToolCompatibleTree instances
//	 * @return a double
//	 */
//	public double getAboveGroundBiomassMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
//		double totalAboveGroundBiomassMg = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				totalAboveGroundBiomassMg += getAboveGroundBiomassMg(tree, subject);
//			}
//		}
//		return totalAboveGroundBiomassMg;
//	}

	/**
	 * This method returns the aboveground carbon for a collection of trees.
	 * @param trees a Collection of CarbonToolCompatibleTree instances
	 * @return a double
	 */
	public double getAboveGroundCarbonMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
		double totalAboveGroundCarbonMg = 0d;
		if (trees != null) {
			for (CATCompatibleTree tree : trees) {
				totalAboveGroundCarbonMg += getAboveGroundCarbonMg(tree, subject);
			}
		}
		return totalAboveGroundCarbonMg;
	}

	public double getBelowGroundCarbonMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
		double totalBelowGroundCarbonMg = 0d;
		if (trees != null) {
			for (CATCompatibleTree tree : trees) {
				totalBelowGroundCarbonMg += getBelowGroundCarbonMg(tree, subject);
			}
		}
		return totalBelowGroundCarbonMg;
	}

//	public double getBelowGroundBiomassMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
//		double totalBelowGroundBiomassMg = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				totalBelowGroundBiomassMg += getBelowGroundBiomassMg(tree, subject);
//			}
//		}
//		return totalBelowGroundBiomassMg;
//	}

	//	public double getBelowGroundVolumeM3(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
//		double totalBelowGroundVolumeM3 = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				totalBelowGroundVolumeM3 += getBelowGroundVolumeM3(tree, subject);
//			}
//		}
//		return totalBelowGroundVolumeM3;
//	}

	
//	public double getCommercialVolumeM3(Collection<CATCompatibleTree> trees) {
//		double commercialVolumeM3 = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				commercialVolumeM3 += getCommercialVolumeM3ForThisTree(tree);
//			}
//		}
//		return commercialVolumeM3;
//	}
	

//	public double getCommercialBiomassMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
//		double commercialBiomassMg = 0d;
//		if (trees != null) {
//			for (CATCompatibleTree tree : trees) {
//				commercialBiomassMg += getCommercialBiomassMg(tree, subject);
//			}
//		}
//		return commercialBiomassMg;
//	}

	public double getCommercialCarbonMg(Collection<CATCompatibleTree> trees, MonteCarloSimulationCompliantObject subject) {
		double commercialCarbonMg = 0d;
		if (trees != null) {
			for (CATCompatibleTree tree : trees) {
				commercialCarbonMg += getCommercialCarbonMg(tree, subject);
			}
		}
		return commercialCarbonMg;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	public static void main(String[] args) {
		BiomassParameters bp = new BiomassParameters();
		bp.showUI(null);
	}

}
