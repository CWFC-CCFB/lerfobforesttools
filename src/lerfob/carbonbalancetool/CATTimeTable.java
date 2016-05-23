/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool;

import java.util.ArrayList;
import java.util.Vector;

@SuppressWarnings("serial")
public class CATTimeTable extends ArrayList<Integer> {

	private final int lastStandDate;

	protected CATTimeTable(int lastStandDate) {
		this.lastStandDate = lastStandDate;
	}

	protected int getLastStandDate() {return lastStandDate;}
	
	protected Vector<Integer> getListOfDatesUntilLastStandDate() {
		Vector<Integer> dates = new Vector<Integer>();
		for (int i = 0; i <= lastIndexOf(getLastStandDate()); i++) {
			if (!dates.contains(get(i))) {
				dates.add(get(i));
			}
		}
		return dates;
	}
	
}
