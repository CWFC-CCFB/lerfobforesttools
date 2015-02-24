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

import repicea.gui.UserInterfaceableObject;
import repicea.simulation.processsystem.SystemLayout;
import repicea.simulation.processsystem.SystemManager;
import repicea.simulation.processsystem.SystemPanel;

@SuppressWarnings("serial")
public class ExtendedSystemPanel extends SystemPanel {

	protected ExtendedSystemPanel(SystemManager manager, SystemLayout systemLayout) {
		super(manager, systemLayout);
	}

	@Override
	protected void addManagerComponents() {
		super.addManagerComponents();
		for (UserInterfaceableObject obj : getListManager().getList()) {
			if (obj instanceof ProductionLineProcessor) {
				ProductionLineProcessor process = (ProductionLineProcessor) obj;
				if (process.disposedToProcessor != null) {
					addLinkLine(new EndOfLifeLinkLine(this, process, process.disposedToProcessor));
				}
			}
		}
	}

}
