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

import javax.swing.Icon;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager.EnhancedMode;
import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.simulation.processsystem.DnDCompatibleButton;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.SystemPanel;
import repicea.simulation.processsystem.ToolButton;
import repicea.simulation.processsystem.ToolPanel;
import repicea.simulation.processsystem.UISetup;

@SuppressWarnings("serial")
public class ProductionProcessorToolPanel extends ToolPanel {
	
	protected class CreateProductionLineProcessorButton extends DnDCompatibleButton {
		
		protected CreateProductionLineProcessorButton(REpiceaGUIPermission permission) {
			super(permission);
		}
		
		@Override
		public Processor createNewProcessor() {return new ProductionLineProcessor();}
		
	}

	protected class CreateEndOfLifeLinkButton extends ToolButton {

		protected CreateEndOfLifeLinkButton(REpiceaGUIPermission permission) {
			super(permission);
			mode = EnhancedMode.CreateEndOfLifeLinkLine;
		}
		
		@Override
		protected Icon getDefaultIcon() {
			return UISetup.Icons.get("LinkButtonIcon");
		}

	}

	protected class CreateLeftInForestProcessorButton extends DnDCompatibleButton {
		
		protected CreateLeftInForestProcessorButton(REpiceaGUIPermission permission) {
			super(permission);
		}
		
		@Override
		public Processor createNewProcessor() {return new LeftInForestProcessor();}
	}

	protected class CreateLandfillProcessorButton extends DnDCompatibleButton {
		
		protected CreateLandfillProcessorButton(REpiceaGUIPermission permission) {
			super(permission);
		}
		
		@Override
		public Processor createNewProcessor() {return new LandfillProcessor();}
		
	}

	
	private DnDCompatibleButton createProductionLineProcessorButton;
	private DnDCompatibleButton createLeftInForestButton;
	private DnDCompatibleButton createLandfillProcessorButton;
	
	private ToolButton createEndOfLifeLinkButton;
	
	protected ProductionProcessorToolPanel(SystemPanel owner) {
		super(owner);
	}

	
	@Override
	protected void init() {
		super.init();
		dndButtons.clear();
		createProductionLineProcessorButton = new CreateProductionLineProcessorButton(getGUIPermission());
		dndButtons.add(createProductionLineProcessorButton);
		createLeftInForestButton = new CreateLeftInForestProcessorButton(getGUIPermission());
		dndButtons.add(createLeftInForestButton);
		createLandfillProcessorButton = new CreateLandfillProcessorButton(getGUIPermission());
		dndButtons.add(createLandfillProcessorButton);
		
		createEndOfLifeLinkButton = new CreateEndOfLifeLinkButton(getGUIPermission());
		selectableButtons.add(createEndOfLifeLinkButton);
	}
	
	
	
}
