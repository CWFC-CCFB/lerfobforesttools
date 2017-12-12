/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.carbonbalancetool.woodpiecedispatcher;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import lerfob.carbonbalancetool.AbstractDesigner;
import lerfob.carbonbalancetool.CATFrame;
import lerfob.carbonbalancetool.productionlines.ProductionLineEvent;
import lerfob.carbonbalancetool.productionlines.ProductionLineManager;
import lerfob.carbonbalancetool.productionlines.ProductionLineManagerChangeListener;
import repicea.simulation.treelogger.TreeLoggerWrapper;
import repicea.treelogger.basictreelogger.BasicTreeLoggerParameters;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * The WoodPieceDispatcher is the main class in this package. It sends the different WoodPiece instances
 * to the user-specified markets.
 * @author Mathieu Fortin - October 2010
 */
@Deprecated
public final class WoodPieceDispatcher extends AbstractDesigner<LogCategoryDispatcher> implements ProductionLineManagerChangeListener {

	private static final long serialVersionUID = 20130127L;

	protected static enum MessageID implements TextableEnum {
		WoodPieceDispatcherFileFilterExtension("wood piece dispatcher file (*.wpd)", "fichier de distribution de billons (*.wpd)"),
		Unnamed("Unnamed", "SansNom"),
		Default("Default", "D\u00E9fauts"),
		AvailableTreeLogCategories("Tree log categories", "Cat\u00E9gories de billon")
		;

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


	private static class WoodPieceDispatcherFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".wpd";
		
		@Override
		public boolean accept(File f) {
			if (f.getAbsolutePath().toLowerCase().trim().endsWith(extension)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getDescription() {
			return MessageID.WoodPieceDispatcherFileFilterExtension.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

	
	
	private static final WoodPieceDispatcherFileFilter MyFileFilter = new WoodPieceDispatcherFileFilter();

	
	private Vector<String> marketNames;
	private List<String> referenceLogCategoryNames;
	private List<String> logCategoryNames;
	private List<LogCategoryDispatcher> dispatchers;
	
	
	/**
	 * General constructor.
	 */
	public WoodPieceDispatcher(TreeLoggerWrapper treeLoggerWrapper, ProductionLineManager plm) {
		this();
		setName("");
		setProductionLineNames(new Vector<String>());
		setLogCategoryNames(new ArrayList<String>());
		setReferenceLogCategoryNames(new ArrayList<String>());
		plm.addProductionLineManagerChangeListener(this);
		reset();
	}

	/**
	 * Empty contructor for deserialization.
	 */
	public WoodPieceDispatcher() {
		super();
	}
	
	/**
	 * This method resets the singleton of this class. The reset consists in taking the reference log category names as current log category names and creating a 
	 * dispatcher for each one of them.
	 */
	@Override
	public void reset() {
		setName("");
		dispatchers.clear();
		logCategoryNames.clear();
		
		LogCategoryDispatcher lcd = new LogCategoryDispatcher(this, BasicTreeLoggerParameters.MessageID.ShortLived.toString());
		LogCategoryToMarketMatch match = lcd.getMatches().get(0);
		match.setMarketName(CATFrame.MessageID.ShortLived.toString());
		match.setProportion(1d);
		dispatchers.add(lcd);
		logCategoryNames.add(BasicTreeLoggerParameters.MessageID.ShortLived.toString());

		lcd = new LogCategoryDispatcher(this, BasicTreeLoggerParameters.MessageID.LongLived.toString());
		match = lcd.getMatches().get(0);
		match.setMarketName(CATFrame.MessageID.LongLived.toString());
		match.setProportion(1d);
		dispatchers.add(lcd);
		logCategoryNames.add(BasicTreeLoggerParameters.MessageID.LongLived.toString());
	}
	
	/**
	 * This method sets the log category names and instantiates a dispatcher for each one of them.
	 * @param logCategoryNames an ArrayList object that contains the names of the log categories
	 */
	private void setLogCategoryNames(List<String> logCategoryNames) {
		this.logCategoryNames = logCategoryNames;
		dispatchers = new ArrayList<LogCategoryDispatcher>();
		if (!this.logCategoryNames.isEmpty()) {
			for (String logCategoryName : this.logCategoryNames) {
				addDispatcher(logCategoryName);
			}
		}
	}
	
	/**
	 * This method adds a new dispatcher with its name being the log category name.
	 * @param logCategoryName the name of the log category
	 */
	protected void addDispatcher(String logCategoryName) {
		if (!logCategoryNames.contains(logCategoryName)) {
			logCategoryNames.add(logCategoryName);
		}
		dispatchers.add(new LogCategoryDispatcher(this, logCategoryName));
	}
	
	/**
	 * This method sets the reference log category names. The valid method checks if the current log category names match those of the reference list.
	 * If they do not, the WoodPieceDispatcher object is not valid.
	 * @param referenceLogCategoryNames an ArrayList object that contains the reference names
	 */
	private void setReferenceLogCategoryNames(List<String> referenceLogCategoryNames) {
		this.referenceLogCategoryNames = referenceLogCategoryNames;
	}
	
	/**
	 * This method refreshes the production line names in the WoodPieceDispatcher instance.
	 * @param wpmm a WoodProductMarketManager instance
	 */
	private void setProductionLineNames(ProductionLineManager wpmm) {
		Vector<String> productionLineNames = wpmm.getProductionLineNames();
		setProductionLineNames(productionLineNames);
	}
	
	private void setProductionLineNames(Vector<String> marketNames) {
		this.marketNames = marketNames;
	}
	
	
	@Override
	public List<LogCategoryDispatcher> getContent() {return dispatchers;} 
	
	/**
	 * This method produces a Map whose keys are the markets and values are the proportion of the 
	 * wood piece that is sent to a particular market.
	 * @param logCategoryName a String that defines the log category as specified in the tree logger parameters
	 * @return a Map instance
	 */
	public Map<String, Double> dispatchThisWoodPiece(String logCategoryName) {
		if (!logCategoryNames.contains(logCategoryName)) {
			throw new InvalidParameterException("This log category is unknown : " + logCategoryName);
		} else if (!isValid()) {
			throw new InvalidParameterException("The dispatcher is not valid!");
		} 
		Map<String, Double> oMap = new HashMap<String, Double>();
		LogCategoryDispatcher dispatcher = dispatchers.get(logCategoryNames.indexOf(logCategoryName));
		for (LogCategoryToMarketMatch match : dispatcher.getMatches()) {
			oMap.put(match.getMarketName(), match.getProportion());
		}
		return oMap;
	}

	protected Vector<String> getProductionLineNames() {
		return ObjectUtility.convertFromListToVector(marketNames);
	}
	
	@Override
	protected void loadFrom(AbstractDesigner<LogCategoryDispatcher> designer) {
		super.loadFrom(designer);
		WoodPieceDispatcher wpd = (WoodPieceDispatcher) designer;
		dispatchers = wpd.dispatchers;
//		setName(wpd.getFilename());
		postLoading();
	}
	
//	@Override
//	public void load(String filename) throws IOException {
//		XmlDeserializer deserializer = new XmlDeserializer(filename);
//		WoodPieceDispatcher newDispatcher;
//		try {
//			newDispatcher = (WoodPieceDispatcher) deserializer.readObject();
//			loadFrom(newDispatcher);
//		} catch (XmlMarshallException e) {
//			throw new IOException("A XmlMarshallException occurred while loading the file!");
//		}
//	}
	
	private void postLoading() {
		ArrayList<String> logCategoryNames = new ArrayList<String>();
		for (LogCategoryDispatcher dispatcher : dispatchers) {
			dispatcher.setManager(this);
			logCategoryNames.add(dispatcher.toString());
		}
		this.logCategoryNames = logCategoryNames;
	}

//	@Override
//	public void save(String filename) throws IOException {
//		setName(filename);
//		XmlSerializer serializer = new XmlSerializer(filename);
//		try {
//			serializer.writeObject(this);
//		} catch (XmlMarshallException e) {
//			throw new IOException("A XmlMarshallException occurred while saving the file!");
//		}
//	}


	protected List<String> getCurrentLogCategoryNames() {
		return logCategoryNames;
	}
	
	

	/**
	 * This method returns true if the WoodPieceDispatcher is operational. If
	 * the tree log categories have been changed or if the outlet names have been
	 * changed, the WoodPieceDispatcher instance might not be synchronized with
	 * the TreeLogger instance and the WoodProductMarketManager instance.
	 * @return a boolean
	 */
	public boolean isValid() {
		boolean valid = true;
		if (!checkIfMatchesWithReferenceLogCategoryNames()) {
			return false;
		}
		if (!dispatchers.isEmpty()) {
			for (LogCategoryDispatcher dispatcher : dispatchers) {
				if (!dispatcher.isValid()) {
					valid = false;
					break;
				}
			}
		} else {
			valid = false;
		}
		return valid;
	}
	
	
	protected Vector<String> getLogCategoryNamesFromTreeLogger() {
		return ObjectUtility.convertFromListToVector(referenceLogCategoryNames);
	}
	
	/**
	 * This method checks if the log categories are all found in the reference table.
	 * @return true if everything is alright or false otherwise
	 */
	private boolean checkIfMatchesWithReferenceLogCategoryNames() {
		if (referenceLogCategoryNames == null || referenceLogCategoryNames.isEmpty()) {
			referenceLogCategoryNames = logCategoryNames;
		} 
		
		Vector<String> names = new Vector<String>();
		for (String logCategoryName : logCategoryNames) {
			names.add(logCategoryName.trim().toLowerCase());
		}
		
		Vector<String> refNames = new Vector<String>();
		for (String refLogCategoryNames : referenceLogCategoryNames) {
			refNames.add(refLogCategoryNames.trim().toLowerCase());
		}
		
		for (String logRefName : refNames) {
			if (!names.contains(logRefName)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}

	@Override
	public void productionLineManagerChanged(ProductionLineEvent evt) {
		ProductionLineManager manager = (ProductionLineManager) evt.getSource();
		setProductionLineNames(manager);
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WoodPieceDispatcher)) {
			return false;
		} else {
			WoodPieceDispatcher wpd = (WoodPieceDispatcher) obj;
			if (wpd.logCategoryNames.size() != logCategoryNames.size()) {
				return false;
			}
			for (int i = 0; i < wpd.logCategoryNames.size(); i++) {
				if (!wpd.logCategoryNames.get(i).equals(logCategoryNames.get(i))) {
					return false;
				}
			}
			if (wpd.dispatchers.size() != dispatchers.size()) {
				return false;
			}
			for (int i = 0; i < wpd.dispatchers.size(); i++) {
				if (!wpd.dispatchers.get(i).equals(dispatchers.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see lerfob.carbonbalancetool.AbstractDesigner#fireDesignerChangeEvent()
	 */
	@Override
	protected void fireDesignerChangeEvent() {}

	

}
