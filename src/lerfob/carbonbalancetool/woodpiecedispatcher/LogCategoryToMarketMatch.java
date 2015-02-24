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

/**
 * A LogCategoryToMarketMatch contains two elements: the proportion of the log sent to a particular as well as the name of this market.
 * @author Mathieu Fortin - October 2010
 */
@Deprecated
public class LogCategoryToMarketMatch implements Serializable {

	private static final long serialVersionUID = 20101020L;
	
	private double proportion;
	private String marketName;
	private LogCategoryDispatcher dispatcher;
	
	/**
	 * Constructor.
	 * @param caller a LogCategoryDispatcher instance
	 */
	protected LogCategoryToMarketMatch(LogCategoryDispatcher caller) {
		dispatcher = caller;
		setProportion(1d);
		String defaultMarketName = caller.getManager().getProductionLineNames().get(1); // left in the forest by default
		setMarketName(defaultMarketName);
	}

	protected void setLogDispatcher(LogCategoryDispatcher dispatcher) {this.dispatcher = dispatcher;}
	protected LogCategoryDispatcher getLogDispatcher() {return dispatcher;}
	
	

	protected String getMarketName() {return marketName;}
	protected double getProportion() {return proportion;}

	protected void setProportion(double proportion) {
		this.proportion = proportion;
	}
	
	protected boolean isMarketAttributed() {return getLogDispatcher().getManager().getProductionLineNames().contains(marketName);}
	
	protected void setMarketName(String marketName) {
		this.marketName = marketName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogCategoryToMarketMatch)) {
			return false;
		} else {
			LogCategoryToMarketMatch match = (LogCategoryToMarketMatch) obj;
			if (!match.marketName.equals(marketName)) {
				return false;
			}
			if (match.proportion != proportion) {
				return false;
			}
		}
		return true;
	}
}
