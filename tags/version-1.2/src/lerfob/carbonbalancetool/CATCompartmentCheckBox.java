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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;

@SuppressWarnings("serial")
class CATCompartmentCheckBox extends JCheckBox {

	protected static final Set<CompartmentInfo> DEFAULT_TRUE_OPTIONS_SET = new HashSet<CompartmentInfo>();
	static {
		DEFAULT_TRUE_OPTIONS_SET.add(CompartmentInfo.TotalProducts);
		DEFAULT_TRUE_OPTIONS_SET.add(CompartmentInfo.LivingBiomass);
	}

	private CompartmentInfo compartmentID;

	protected CATCompartmentCheckBox(CompartmentInfo compartmentID) {
		super(compartmentID.toString());
		this.compartmentID = compartmentID;
		if (DEFAULT_TRUE_OPTIONS_SET.contains(compartmentID)) {
			setSelected(true);
		} else {
			setSelected(false);
		}
	}

	protected CompartmentInfo getCompartmentID() {return compartmentID;}


}
