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
package lerfob.carbonbalancetool.woodpiecedispatcher;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The LogCategoryDispatcher class dispatch the different log categories to the different markets
 * according to the preferences specified by the user.
 * @author Mathieu Fortin - October 2010
 */
@Deprecated
public class LogCategoryDispatcher implements Serializable {

	private static final long serialVersionUID = 20101020L;
	
	private String name;
	private ArrayList<LogCategoryToMarketMatch> matches; 
	
	private transient WoodPieceDispatcher manager;		// must be transient because the manager handles the save and load methods
	
	/**
	 * Constructor.
	 * @param manager a WoodPieceDispatcher manager
	 * @param name the name of the log category
	 */
	protected LogCategoryDispatcher(WoodPieceDispatcher manager, String name) {
		setManager(manager);
		this.name = name;
		matches = new ArrayList<LogCategoryToMarketMatch>();
		init();
	}

	protected void setManager(WoodPieceDispatcher manager) {this.manager = manager;}
	protected WoodPieceDispatcher getManager() {return manager;}
		
	protected void init() {
		matches.clear();
		addMatch(new LogCategoryToMarketMatch(this));
	}
	
	protected void addMatch(LogCategoryToMarketMatch match) {
		matches.add(match);
	}
		
	
	protected ArrayList<LogCategoryToMarketMatch> getMatches() {
		return matches;
	}
	
	protected boolean isValid() {
		boolean valid = true;
		double verySmall = 10E-5;
		double sum = 0d;
		if (!matches.isEmpty()) {
			for (LogCategoryToMarketMatch match : matches) {
				if (!match.isMarketAttributed()) {
					valid = false;
					break;
				} else  {
					sum += match.getProportion();
				}
			}
			if (Math.abs(sum - 1d) > verySmall) {
				valid = false;
			}
		}
		return valid;
	}
	
	
	@Override
	public String toString() {
		return name;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogCategoryDispatcher)) {
			return false;
		} else {
			LogCategoryDispatcher lcd = (LogCategoryDispatcher) obj;
			if (!lcd.name.equals(name)) {
				return false;
			}
			if (lcd.matches.size() != matches.size()) {
				return false;
			}
			for (int i = 0; i < lcd.matches.size(); i++) {
				if (!lcd.matches.get(i).equals(matches.get(i))) {
					return false;
				}
			}
			return true;
		}
	}

}
