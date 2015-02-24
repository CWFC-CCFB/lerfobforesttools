/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.carbonbalancetool.productionlines;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import repicea.simulation.processsystem.AnchorProvider;
import repicea.simulation.processsystem.PreProcessorLinkLine;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.simulation.processsystem.ValidProcessorLinkLine;

@SuppressWarnings("serial")
public class PreEndOfLifeLinkLine extends PreProcessorLinkLine {

	protected static final Stroke PreEndOfLifeLinkLineStroke = new BasicStroke(3, 
			BasicStroke.CAP_SQUARE, 
			BasicStroke.JOIN_MITER,
			1, 
			new float[]{12,12},
			0);
	
	
	protected PreEndOfLifeLinkLine(SystemPanel panel, AnchorProvider fatherAnchor) {
		super(panel, fatherAnchor);
	}

	
	@Override
	protected void setStroke(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.setStroke(PreEndOfLifeLinkLineStroke);
	}

	@Override
	protected ValidProcessorLinkLine convertIntoProcessorLinkLine() {
		return new EndOfLifeLinkLine(panel, getFatherAnchor().getOwner(), ((ProcessorButton) getSonAnchor()).getOwner());
	}

}
