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
package lerfob.carbonbalancetool.productionlines;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;

/**
 * This class handles the addition of carbon units in its own list. If a similar carbon unit is found in the
 * list, then the carbon unit that was supposed to be added is merged instead. This makes it possible to save
 * memory space. The equals() method serves to define if two carbon units can be merged.
 * @author Mathieu Fortin - April  2011
 */
public class CarbonUnitList extends ArrayList<CarbonUnit> {

	private static final long serialVersionUID = 20110413L;
	
	@Override
	public boolean add(CarbonUnit carbonUnit) {
		boolean matchFound = false;
		for (CarbonUnit unit : this) {
			if (unit.equals(carbonUnit)) {
				unit.addProcessUnit(carbonUnit);
				matchFound = true;
				break;
			}
		}
		
		if (!matchFound) {
			super.add(carbonUnit);
		}
		
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean addAll(Collection coll) {
		if (!coll.isEmpty()) {
			for (Object obj : coll) {
				add((CarbonUnit) obj);
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		double volume = 0;
		for (CarbonUnit unit : this) {
			volume += unit.getAmountMap().get(Element.Volume);
		}
		return "Volume = " + volume;
	}
	

	/**
	 * This method filters the CarbonUnitList instance.
	 * @param clazz
	 * @param methodName
	 * @param expectedValue
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public CarbonUnitList filterList(Class<? extends CarbonUnit> clazz, String methodName, Object expectedValue) {
		try {
			CarbonUnitList subList = new CarbonUnitList();
			Method method = clazz.getDeclaredMethod(methodName);
			for (CarbonUnit carbonUnit : this) {
				Object res = method.invoke(carbonUnit);
				if (expectedValue.equals(res)) {
					subList.add(carbonUnit);
				}
			}
			return subList;
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to filter the CarbonUnitList instance with this method name : " + methodName);
		}
	}
	
}
