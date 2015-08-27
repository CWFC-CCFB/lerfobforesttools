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

import repicea.gui.REpiceaPanel;

/**
 * An abstract for all Processor-derived classes (except the LogCategoryProcessor), which ensures
 * the Processor-derived class contains a CarbonUnitFeature instance and that this CarbonUnitFeature
 * instance can be shown in the GUI.
 * @author Mathieu Fortin - May 2014
 */
@SuppressWarnings("serial")
public abstract class AbstractProductionLineProcessor extends AbstractProcessor {

	protected CarbonUnitFeature woodProductFeature;

	protected CarbonUnitFeature getEndProductFeature() {return woodProductFeature;}

	
	
	@Override
	protected REpiceaPanel getProcessFeaturesPanel() {
		return getEndProductFeature().getGuiInterface();
	}

}
