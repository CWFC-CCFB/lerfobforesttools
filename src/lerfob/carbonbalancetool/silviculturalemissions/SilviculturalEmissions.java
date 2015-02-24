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

import lerfob.carbonbalancetool.detailedlci.DetailedLCI;
import repicea.gui.UserInterfaceableObject;

public class SilviculturalEmissions implements UserInterfaceableObject {

	protected final DetailedLCI lci;
	protected int date;
	private transient SilviculturalEmissionsPanel guiInterface;
	
	protected SilviculturalEmissions(DetailedLCI lci) {
		this.lci = lci;
		date = -1;
	}
	
	@Override
	public SilviculturalEmissionsPanel getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new SilviculturalEmissionsPanel(this);
		}
		return guiInterface;
	}

}
