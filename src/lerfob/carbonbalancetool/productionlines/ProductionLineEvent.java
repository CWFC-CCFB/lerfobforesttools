/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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

import java.util.EventObject;

/**
 * A ProductionLineEvent object is generated every time there is a change within a production line manager.
 * @author Mathieu Fortin - January 2013
 */
@SuppressWarnings({"serial"})
@Deprecated
public class ProductionLineEvent extends EventObject {

	/**
	 * Protected constructor since this class is instantiated in this package only.
	 * @param source a ProductionLineManager instance
	 */
	protected ProductionLineEvent(ProductionLineManager source) {
		super(source);
	}

}
