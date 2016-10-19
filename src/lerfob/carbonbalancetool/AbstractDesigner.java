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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import repicea.gui.Resettable;
import repicea.io.IOUserInterfaceableObject;
import repicea.io.Workspaceable;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import repicea.serial.xml.XmlSerializer;
import repicea.simulation.Parameterizable;

/**
 * Thie AbstractDesigner is the basic class for all the subcomponents of the 
 * carbon balance assessment tool.
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings({ "serial"})
@Deprecated
public abstract class AbstractDesigner<C> 
									implements 	Workspaceable, 
												Resettable, 
												Parameterizable,
												Memorizable,
												Serializable,
												IOUserInterfaceableObject {

	private static boolean SetWorkSpaceEnabled = true;
			
	private String workSpace;
	private String managerName;

	@Override
	public String getWorkspace() {return workSpace;}

	@Override
	public void setWorkspace(String workspace) {this.workSpace = workspace;}

	@Override
	public String getFilename() {return getWorkspace() + File.separator + getName();}
	
	@Override
	public abstract FileFilter getFileFilter();
	
	/**
	 * This method determines whether the designer is valid or not
	 * @return a boolean
	 */
	public abstract boolean isValid();

	@Override
	public abstract void reset();
	
	/**
	 * This method sets the name of the designer.
	 * @param managerName a String
	 */
	public void setName(String managerName) {
		if (managerName.contains(File.separator)) {
			int lastIndex = managerName.lastIndexOf(File.separator);
			managerName = managerName.substring(lastIndex + 1, managerName.length());
		}
		this.managerName = managerName;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * In this case, this method returns the short filename of this designer. If it has no name,
	 * it returns an empty string.
	 * @return a String
	 */
	@Override
	public String getName() {return managerName;}
	
	/**
	 * This method enables or disables the set workspace option in the 
	 * UI of the designer. BY DEFAULT it is enabled.
	 * @param b a boolean
	 */
	public static void setWorkSpaceChangeableInGUI(boolean b) {
		SetWorkSpaceEnabled = b;
	}

	/**
	 * This method returns true if the set workspace option is enabled or 
	 * false otherwise.
	 * @return a boolean
	 */
	public static boolean isWorkSpaceChangeableInGUI() {
		return SetWorkSpaceEnabled;
	}

	
	/**
	 * This method returns a Collection of UserInterfaceableObject instances that are to be set in the different tabs of the dialog.
	 * @return a Collection of UserInterfaceableObject instances
	 */
	public abstract List<C> getContent();

	
	
	@Override
	public void loadFromFile(String filename) throws IOException {
		load(filename);
	}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(this);
		return mp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		AbstractDesigner<C> designer = (AbstractDesigner<C>) wasMemorized.get(0);
		loadFrom(designer);
	}

	
	protected void loadFrom(AbstractDesigner<C> designer) {
		setName(designer.getFilename());
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		AbstractDesigner<C> newManager;
		try {
			newManager = (AbstractDesigner<C>) deserializer.readObject();
			newManager.setName(filename);
			if (newManager.getContent().isEmpty()) {
				throw new InvalidParameterException("The content of the designer is empty!");
			}
			loadFrom(newManager);
			fireDesignerChangeEvent();
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while loading the file!");
		}
	}

	protected abstract void fireDesignerChangeEvent();
	
	@Override
	public void save(String filename) throws IOException {
		setName(filename);
		XmlSerializer serializer = new XmlSerializer(filename);
		try {
			serializer.writeObject(this);
			fireDesignerChangeEvent();
		} catch (XmlMarshallException e) {
			throw new IOException("A XmlMarshallException occurred while saving the file!");
		}
	}

}
