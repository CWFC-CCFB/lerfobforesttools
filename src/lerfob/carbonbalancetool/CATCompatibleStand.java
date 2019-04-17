/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for AgroParisTech/INRA UMR LERFoB,
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

import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.standlevel.TreeStatusCollectionsProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This method ensures the stand is compatible with LERFoB-CAT
 * @author Mathieu Fortin - August 2013
 */
public interface CATCompatibleStand extends AreaHaProvider, 
											TreeStatusCollectionsProvider, 
											InterventionResultProvider {

	public static enum Management implements TextableEnum {
		UnevenAged("Uneven-aged", "Irr\u00E9gulier"), 
		EvenAged("Even-aged", "R\u00E9gulier");

		Management(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	
	/**
	 * This method returns the current management of the stand. An even-aged management, allows for 
	 * carbon balances calculated in infinite sequence. By default, it returns Management.UnevenAged.
	 * @return a Management enum
	 */
	public default Management getManagement() {return Management.UnevenAged;}
	
	/**
	 * This method returns a CarbonToolCompatibleStand with all its trees
	 * set to cut. It is called only if the management mode is Management.EvenAged.
	 * Since the default Management is Management.UnevenAged, this method returns
	 * null by default.
	 * @return a CarbonToolCompatibleStand stand
	 */
	public default CATCompatibleStand getHarvestedStand() {return null;}

	
	/**
	 * This method returns the identification of the stand.
	 * @return a String
	 */
	public String getStandIdentification();
	
	
	/**
	 * This method returns the date at which the plot was measured in years.
	 * @return an integer
	 */
	public int getDateYr();

}
