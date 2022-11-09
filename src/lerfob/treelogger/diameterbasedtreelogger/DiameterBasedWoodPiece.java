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
package lerfob.treelogger.diameterbasedtreelogger;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class DiameterBasedWoodPiece extends WoodPiece {

	
	/**
	 * Constructor.
	 * @param logCategory a MaritimePineBasicTreeLogCategory instance
	 * @param tree a MaritimePineBasicTree instance
	 * @param volumeForThisWoodPieceM3 the volume without any expansion factor
	 */
	public DiameterBasedWoodPiece(DiameterBasedTreeLogCategory logCategory, LoggableTree tree, boolean overbark, double volumeForThisWoodPieceM3) {
		super(logCategory, tree, overbark, volumeForThisWoodPieceM3);
	}

	
		
}