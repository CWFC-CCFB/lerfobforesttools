/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin 
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
package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategoryPanel;

@SuppressWarnings("serial")
public class CATDiameterBasedTreeLogCategoryPanel extends DiameterBasedTreeLogCategoryPanel {

	protected CATDiameterBasedTreeLogCategoryPanel(CATDiameterBasedTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setEditable(false);	// override here the log names cannot be changed
	}

}
