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
package lerfob.allometricrelationships;

import lerfob.fagacees.FagaceesSpeciesProvider.FgSpecies;



/**
 * This method implements the models developed in Vallet et al. to predict the total aboveground over-bark volume.
 * @see <a href=https://www.sciencedirect.com/science/article/pii/S0378112706002040> 
 * Vallet, P., Dhote, J.-F., Le Moguedec, G., Ravart, M., Pignard, G. 2006. Development of total aboveground volume equations for seven important forest tree species in France. Forest Ecology Management 229: 98-110.
 * </a> 
 * @author Mathieu Fortin - November 2011
 */
public class ValletTotalAboveGroundVolumeEquations {
	
	/**
	 * This method implements the models developed in Vallet et al. to predict the total aboveground over-bark volume.
	 * @param dbh the diameter at breast height (cm)
	 * @param height the height (m)
	 * @param species a FgSpecies enum variable Oak or Beech
	 * @return the volume (m3) 
	 */
	public double getTotalAboveGroundVolume(double dbh, double height, FgSpecies species) {
		if (species == FgSpecies.OAK) {
			return getTotalAboveGroundVolumeForOak(dbh, height);
		} else {
			return getTotalAboveGroundVolumeForBeech(dbh, height);
		} 
	}
	
	
	
	private double getTotalAboveGroundVolumeForOak(double dbh, double height) {
		
		// see Table 4 for parameter estimates
		final double alpha = 0.471;
		final double beta = -0.000345;
		final double gamma = 0.377;
		final double delta = 0d;

		double c130 = dbh * Math.PI;			// tree circumference at breast height
		double htot = height;
		
		double vol;
		double form;
		double correction;
		
		vol = 1 / (40000d * Math.PI) * c130 * c130 * htot;		// eq. 3 in Vallet et al. 2006
		form = (alpha + beta * c130 + gamma * Math.sqrt(c130) / htot);		// eq. 6 in Vallet et al. 2006
		correction = 1. + delta / Math.pow(c130,2.);
		form *= correction;		// eq. 7 in Vallet et al. 2006
		vol *= form;

		return vol;
	}
	

	private double getTotalAboveGroundVolumeForBeech(double dbh, double height) {
		
		// see Table 4 for parameter estimates
		final double alpha = 0.395;
		final double beta = 0.000266;
		final double gamma = 0.421;
		final double delta = 45.4;

		double c130 = dbh * Math.PI;
		double htot = height;
		
		double vol;
		double form;
		double correction;

		vol = 1 /  (40000d * Math.PI) * c130 * c130 * htot; // eq. 3 in Vallet et al. 2006
		form = (alpha + beta * c130 + gamma * Math.sqrt (c130) / htot);	// eq. 6 in Vallet et al. 2006
		correction = 1. + delta / Math.pow (c130,2.);
		form *= correction;	// eq. 7 in Vallet et al. 2006
		vol *= form;

		return vol;
	}

}
