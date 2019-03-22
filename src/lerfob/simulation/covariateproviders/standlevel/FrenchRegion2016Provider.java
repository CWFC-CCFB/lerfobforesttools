/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.simulation.covariateproviders.standlevel;

public abstract interface FrenchRegion2016Provider {

	/**
	 * This method returns a FrenchRegion2016 instance that matches the region name. 
	 * The region name is processed to replace all occurrences of " ", "'", and "-" by
	 * "_". 
	 * @param regionName a String that represents the 2016 new administrative region in French.
	 * @return a FrenchRegion2016 enum
	 */
	public static FrenchRegion2016 getFrenchRegion2016FromThisString(String regionName) {
		regionName = regionName.trim().toUpperCase().replace(" ", "_");
		regionName = regionName.replace("'", "_");
		regionName = regionName.replace("-", "_");
		FrenchRegion2016 region = FrenchRegion2016.valueOf(regionName);
		return region;
	}

	public static enum FrenchRegion2016 {
		AUVERGNE_RHONE_ALPES,
		BOURGOGNE_FRANCHE_COMTE,
		BRETAGNE,
		CENTRE_VAL_DE_LOIRE,
		CORSE,
		GRAND_EST,
		HAUTS_DE_FRANCE,
		ILE_DE_FRANCE,
		NORMANDIE,
		NOUVELLE_AQUITAINE,
		OCCITANIE,
		PAYS_DE_LA_LOIRE,
		PROVENCE_ALPES_COTE_D_AZUR
		;
	}

	/**
	 * This method returns the region (version 2016) the plot is located in.
	 * @return a FrenchRegion2016 enum
	 */
	public FrenchRegion2016 getFrenchRegion2016();
}
