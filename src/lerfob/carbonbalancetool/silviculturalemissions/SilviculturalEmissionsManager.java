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
package lerfob.carbonbalancetool.silviculturalemissions;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.ListManager;
import repicea.gui.ShowableObject;


public class SilviculturalEmissionsManager implements ShowableObject, ListManager<SilviculturalEmissions> {

	protected final List<SilviculturalEmissions> emissions;
	private transient SilviculturalEmissionsManagerDialog guiInterface;
	
	public SilviculturalEmissionsManager() {
		emissions = new ArrayList<SilviculturalEmissions>();
	}

	@Override
	public Container getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new SilviculturalEmissionsManagerDialog(this, null);
		}
		return guiInterface;
	}

	@Override
	public void showInterface() {
		getGuiInterface().setVisible(true);
	}

	public static void main(String[] args) {
		SilviculturalEmissionsManager manager = new SilviculturalEmissionsManager();
		manager.showInterface();
	}

	protected void removeSilviculturalEmissions(SilviculturalEmissions emission) {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SilviculturalEmissions> getList() {return (List) emissions;}

	@Override
	public void registerObject(SilviculturalEmissions obj) {
		emissions.add(obj);
	}

	@Override
	public void removeObject(SilviculturalEmissions obj) {
		((ArrayList<SilviculturalEmissions>) emissions).remove(obj);
	}
	
 }
