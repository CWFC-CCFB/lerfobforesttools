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

import java.awt.Container;

import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.simulation.treelogger.TreeLogCategory;

/**
 * The LogCategoryProcessor is a specific implementation of Processor for a particular log grade category
 * that comes out of TreeLogger instance.
 * @author Mathieu Fortin - May 2014
 */
@SuppressWarnings("serial")
public class LogCategoryProcessor extends LeftHandSideProcessor {

	/**
	 * The LogCategoryProcessorButton class is the GUI implementation for 
	 * LogCategoryProcessor. It has a specific icon for better identification in the GUI.
	 * @author Mathieu Fortin - May 2014
	 */
	public static class LogCategoryProcessorButton extends LeftHandSideProcessorButton {

		/**
		 * Constructor.
		 * @param panel	a SystemPanel instance
		 * @param processor the LogCategoryProcessor that owns this button
		 */
		protected LogCategoryProcessorButton(SystemPanel panel, LogCategoryProcessor processor) {
			super(panel, processor);
		}


	}

	protected final TreeLogCategory logCategory;
	
	/**
	 * Constructor.
	 * @param speciesName
	 * @param logCategory
	 */
	protected LogCategoryProcessor(TreeLogCategory logCategory) {
		super();
		this.logCategory = logCategory;
	}
	
	@Override
	public String getName() {
		return logCategory.getSpecies() + " - " + logCategory.getName();
	}
	
	@Override
	public ProcessorButton getUI(Container container) {
		if (guiInterface == null) {
			guiInterface = new LogCategoryProcessorButton((SystemPanel) container, this);
		}
		return guiInterface;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogCategoryProcessor) {
			LogCategoryProcessor processor = (LogCategoryProcessor) obj;
			if (this.logCategory.equals(processor.logCategory)) {
				return true;
			}
		}
		return false;
	}

}
