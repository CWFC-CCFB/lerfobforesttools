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

import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("serial")
public class CarbonUnitMap<T extends Enum<?>> extends HashMap<T, CarbonUnitList> {

	@SuppressWarnings("unchecked")
	protected CarbonUnitMap(T enumExample) {
		super();
		for (Enum<?> type : enumExample.getClass().getEnumConstants()) {
			put((T) type, new CarbonUnitList());
		}
	}

	protected void add(CarbonUnitMap<T> carbonUnitMap) {
		for (T carbonUnitType : carbonUnitMap.keySet()) {
			get(carbonUnitType).addAll(carbonUnitMap.get(carbonUnitType));
		}
	}

	protected void add(Collection<CarbonUnit> carbonUnits) {
		for (CarbonUnit carbonUnit : carbonUnits) {
			get(carbonUnit.getLastStatus()).add(carbonUnit);
		}
	}

	
	@Override
	public void clear() {
		for (CarbonUnitList list : values()) {
			list.clear();
		}
	}
	
}
