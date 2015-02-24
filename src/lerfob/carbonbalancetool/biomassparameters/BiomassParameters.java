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

import lerfob.carbonbalancetool.AboveGroundBiomassProvider;
import lerfob.carbonbalancetool.AboveGroundCarbonProvider;
import lerfob.carbonbalancetool.AboveGroundVolumeProvider;
import lerfob.carbonbalancetool.BasicWoodDensityProvider;
import lerfob.carbonbalancetool.BelowGroundBiomassProvider;
import lerfob.carbonbalancetool.BelowGroundCarbonProvider;
import lerfob.carbonbalancetool.BelowGroundVolumeProvider;
import lerfob.carbonbalancetool.CarbonContentRatioProvider;
import lerfob.carbonbalancetool.CarbonToolCompatibleTree;
import lerfob.carbonbalancetool.biomassparameters.BiomassParametersDialog.MessageID;
import repicea.gui.Resettable;
import repicea.gui.ShowableObjectWithParent;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.io.IOUserInterfaceableObject;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;

public class BiomassParameters implements ShowableObjectWithParent, IOUserInterfaceableObject, Resettable, Memorizable {

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
	protected final HashMap<SpeciesType, Double> basicWoodDensityFactors;
	protected final HashMap<SpeciesType, Double> carbonContentFactors;

	protected boolean rootExpansionFactorFromModel;
	protected boolean rootExpansionFactorFromModelEnabled;
	
	protected boolean branchExpansionFactorFromModel;
	protected boolean branchExpansionFactorFromModelEnabled;

	protected boolean basicWoodDensityFromModel;
	protected boolean basicWoodDensityFromModelEnabled;
	
	protected boolean carbonContentFromModel;
	protected boolean carbonContentFromModelEnabled;

	
	private transient BiomassParametersDialog guiInterface;
	
	private transient Object referent;	// TODO: check the compatibility with the referent when deserializing 

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
		reset();
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
		carbonContentFactors.put(SpeciesType.BroadleavedSpecies, CarbonContentRatioProvider.AverageCarbonContent.Hardwood.getRatio());
		carbonContentFactors.put(SpeciesType.ConiferousSpecies, CarbonContentRatioProvider.AverageCarbonContent.Softwood.getRatio());
		
		basicWoodDensityFromModel = false;
		basicWoodDensityFromModelEnabled = false;
		carbonContentFromModel = false;
		carbonContentFromModelEnabled = false;
		if (referent != null) {
			testReferent(referent);
		}
	}

	private void testReferent(Object referent) {
		branchExpansionFactorFromModelEnabled = referent instanceof AboveGroundVolumeProvider || referent instanceof AboveGroundBiomassProvider || referent instanceof AboveGroundCarbonProvider;
		rootExpansionFactorFromModelEnabled = referent instanceof BelowGroundVolumeProvider || referent instanceof BelowGroundBiomassProvider || referent instanceof BelowGroundCarbonProvider;
		basicWoodDensityFromModelEnabled = referent instanceof BasicWoodDensityProvider;
		carbonContentFromModelEnabled = referent instanceof CarbonContentRatioProvider;
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

//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof BiomassParameters)) {
//			return false;
//		} else {
//			BiomassParameters bp = (BiomassParameters) obj;
//			if (getContent().size() != bp.getContent().size()) {
//				return false;
//			} else {
//				for (int i = 0; i < getContent().size(); i++) {
//					BiomassCompartmentParameters thisCompartment = getContent().get(i);
//					BiomassCompartmentParameters thatCompartment = bp.getContent().get(i);
//					if (!thisCompartment.equals(thatCompartment)) {
//						return false;
//					}
//				}
//				return true;
//			}
//		}
//	}

	
	@Override
	public Component getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new BiomassParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public void showInterface(Window parent) {
		getGuiInterface(parent).setVisible(true);
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
			InputStream is = ClassLoader.getSystemResourceAsStream(filename);
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
	 * @return a double
	 */
	public double getBasicWoodDensityFromThisTree(CarbonToolCompatibleTree tree) {
		if (basicWoodDensityFromModel) {
			return ((BasicWoodDensityProvider) tree).getBasicWoodDensity();
		} else {
			return basicWoodDensityFactors.get(tree.getSpeciesType());
		}
	}

	/**
	 * This method returns the carbon content ratio for this tree.
	 * @param tree a CarbonToolCompatibleTree
	 * @return a double
	 */
	public double getCarbonContentFromThisTree(CarbonToolCompatibleTree tree) {
		if (carbonContentFromModel) {
			return ((CarbonContentRatioProvider) tree).getCarbonContentRatio();
		} else {
			return carbonContentFactors.get(tree.getSpeciesType());
		}
	}

	
	/**
	 * This method returns the belowground carbon content of a particular instance
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the carbon content (Mg)
	 */
	public double getBelowGroundCarbonMg(CarbonToolCompatibleTree tree) {
		if (carbonContentFromModel) {
			if (tree instanceof BelowGroundCarbonProvider) {
				return ((BelowGroundCarbonProvider) tree).getBelowGroundCarbonMg() * tree.getNumber();
			} 
		}
		return getBelowGroundBiomassMg(tree) * getCarbonContentFromThisTree(tree);
	}
	
	/**
	 * This method returns the belowground biomass of a particular instance
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the biomass (Mg)
	 */
	public double getBelowGroundBiomassMg(CarbonToolCompatibleTree tree) {
		if (basicWoodDensityFromModel) {
			if (tree instanceof BelowGroundBiomassProvider) {
				return ((BelowGroundBiomassProvider) tree).getBelowGroundBiomassMg() * tree.getNumber();
			}
		}
		return getBelowGroundVolumeM3(tree) * getBasicWoodDensityFromThisTree(tree);
	}

	/**
	 * This method returns the belowground volume of a particular instance
	 * @param tree a CarbonToolCompatibleTree instance
	 * @return the volume (M3)
	 */
	public double getBelowGroundVolumeM3(CarbonToolCompatibleTree tree) {
		if (rootExpansionFactorFromModel) {
			if (tree instanceof BelowGroundVolumeProvider) {
				return ((BelowGroundVolumeProvider) tree).getBelowGroundVolumeM3() * tree.getNumber();
			}
		} 
		return getAboveGroundVolumeM3(tree) * (rootExpansionFactors.get(tree.getSpeciesType()) - 1);		// minus 1 is required because we want to get only the belowground part
	}

	
	/**
	 * This method returns the aboveground carbon of a particular tree in Mg.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	public double getAboveGroundCarbonMg(CarbonToolCompatibleTree tree) {
		if (carbonContentFromModel) {
			if (tree instanceof AboveGroundCarbonProvider) {
				return ((AboveGroundCarbonProvider) tree).getAboveGroundCarbonMg() * tree.getNumber();
			} 
		} 
		return getAboveGroundBiomassMg(tree) * getCarbonContentFromThisTree(tree);
	}
	
	/**
	 * This method returns the aboveground biomass of a particular tree in Mg.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	public double getAboveGroundBiomassMg(CarbonToolCompatibleTree tree) {
		if (basicWoodDensityFromModel) {
			if (tree instanceof AboveGroundBiomassProvider) {
				return ((AboveGroundBiomassProvider) tree).getAboveGroundBiomassMg() * tree.getNumber();
			} 
		} 
		return getAboveGroundVolumeM3(tree) * getBasicWoodDensityFromThisTree(tree);
	}

	/**
	 * This method returns the aboveground volume of a particular tree in M3.
	 * @param tree a CarbonCompatibleTree
	 * @return a double
	 */
	public double getAboveGroundVolumeM3(CarbonToolCompatibleTree tree) {
		if (branchExpansionFactorFromModel) {
			return ((AboveGroundVolumeProvider) tree).getAboveGroundVolumeM3() * tree.getNumber();
		} else {
			return tree.getCommercialVolumeM3() * tree.getNumber() * branchExpansionFactors.get(tree.getSpeciesType());
		}
	}

	
	/**
	 * This method returns the aboveground volume for a collection of trees.
	 * @param trees a Collection of CarbonToolCompatibleTree instances
	 * @return a double
	 */
	public double getAboveGroundVolumeM3(Collection<CarbonToolCompatibleTree> trees) {
		double totalAboveGroundVolumeM3 = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalAboveGroundVolumeM3 += getAboveGroundVolumeM3(tree);
			}
		}
		return totalAboveGroundVolumeM3;
	}

	/**
	 * This method returns the aboveground biomass for a collection of trees.
	 * @param trees a Collection of CarbonToolCompatibleTree instances
	 * @return a double
	 */
	public double getAboveGroundBiomassMg(Collection<CarbonToolCompatibleTree> trees) {
		double totalAboveGroundBiomassMg = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalAboveGroundBiomassMg += getAboveGroundBiomassMg(tree);
			}
		}
		return totalAboveGroundBiomassMg;
	}

	/**
	 * This method returns the aboveground carbon for a collection of trees.
	 * @param trees a Collection of CarbonToolCompatibleTree instances
	 * @return a double
	 */
	public double getAboveGroundCarbonMg(Collection<CarbonToolCompatibleTree> trees) {
		double totalAboveGroundCarbonMg = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalAboveGroundCarbonMg += getAboveGroundCarbonMg(tree);
			}
		}
		return totalAboveGroundCarbonMg;
	}

	public double getBelowGroundCarbonMg(Collection<CarbonToolCompatibleTree> trees) {
		double totalBelowGroundCarbonMg = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalBelowGroundCarbonMg += getBelowGroundCarbonMg(tree);
			}
		}
		return totalBelowGroundCarbonMg;
	}

	public double getBelowGroundBiomassMg(Collection<CarbonToolCompatibleTree> trees) {
		double totalBelowGroundBiomassMg = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalBelowGroundBiomassMg += getBelowGroundBiomassMg(tree);
			}
		}
		return totalBelowGroundBiomassMg;
	}

	public double getBelowGroundVolumeM3(Collection<CarbonToolCompatibleTree> trees) {
		double totalBelowGroundVolumeM3 = 0d;
		if (trees != null) {
			for (CarbonToolCompatibleTree tree : trees) {
				totalBelowGroundVolumeM3 += getBelowGroundVolumeM3(tree);
			}
		}
		return totalBelowGroundVolumeM3;
	}

	public static void main(String[] args) {
		BiomassParameters bp = new BiomassParameters();
		bp.showInterface(null);
	}

}
