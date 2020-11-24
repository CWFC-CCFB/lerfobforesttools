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
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class CATTimeTable extends ArrayList<Integer> {

	private final int lastStandDate;
	private final int initialAgeYr;
	
	
	/*
	 * Former implementation with non annual steps.
	 */
	protected CATTimeTable(int lastStandDate, int initialAgeYr) {
		this.lastStandDate = lastStandDate;
		this.initialAgeYr = initialAgeYr;
	}

	
	
	protected CATTimeTable(List<CATCompatibleStand> stands, int initialAgeYr, int nbExtraYears, int averageTimeStep) {
		CATCompatibleStand lastStand = stands.get(stands.size() - 1);
		this.lastStandDate = lastStand.getDateYr();
		int size = stands.size() + nbExtraYears / averageTimeStep;
		for (int i = 0; i < size; i++) {
			if (i < stands.size()) {
				add(stands.get(i).getDateYr());
			} else  {
				add(get(i - 1) + averageTimeStep);
			}
		}
		this.initialAgeYr = initialAgeYr;
	}

	
	
//	/*
//	 * Former implementation with annual steps.
//	 */
//	protected CATTimeTable(int lastStandDate, int initialAgeYr, int nbExtraYears, int averageTimeStep) {
//		this.lastStandDate = lastStandDate;
//		this.initialAgeYr = initialAgeYr;
//		if (nbExtraYear != -1) {
//			int size = stands.size() + nbExtraYears / averageTimeStep;
//			for (int i = 0; i < size; i++) {
//				if (i < stands.size()) {
//					timeTable.add(stands.get(i).getDateYr());
//				} else  {
//					timeTable.add(timeTable.get(i - 1) + averageTimeStep);
//				}
//			}
//			
//		}
//		this.isFinal = nbExtraYear != -1;
//	}

	
	
	protected int getLastStandDate() {return lastStandDate;}
	protected int getInitialAgeYr() {return initialAgeYr;}
	
	protected Vector<Integer> getListOfDatesUntilLastStandDate() {
		Vector<Integer> dates = new Vector<Integer>();
		for (int i = 0; i <= lastIndexOf(getLastStandDate()); i++) {
			if (!dates.contains(get(i))) {
				dates.add(get(i));
			}
		}
		return dates;
	}

//	@Override
//	public boolean add(Integer i) {
//		if (!isFinal) {
//			return super.add(i);
//		} else {
//			return false;
//		}
//	}
//	
//	@Override
//	public boolean remove(Object obj) {
//		if (!isFinal) {
//			return super.remove(obj);
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public Integer remove(int i) {
//		if (!isFinal) {
//			return super.remove(1);
//		} else {
//			return null;
//		}
//	}

	
}
