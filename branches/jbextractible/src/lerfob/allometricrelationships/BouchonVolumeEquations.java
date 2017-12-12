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
 * This class implements Bouchon's volume equation for oak and European beech. It Calculates the commercial wood volume for the stem and the branches, using a small end minimum diameter of 7cm.
 * @see Bouchon, J. 1974. Les tarifs de cubage. ENGREF. 57 p.
 * @author Mathieu Fortin - November 2011
 */
public class BouchonVolumeEquations {
	
	/**
	 * Calculates the commercial wood volume for the stem and the branches, using a small end minimum diameter of 7cm.
	 * @see Bouchon, J. 1974. Les tarifs de cubage. ENGREF. 57 p.
	 * @param dbh the diameter at breast height (cm)
	 * @param height the height (m)
	 * @param species a Species enum variable Oak or Beech
	 * @return the volume (m3) 
	 */
	public double getStemAndBranchesMerchantableVolume(double dbh, double height, FgSpecies species) {
		if (species.equals(FgSpecies.OAK)) {
			return getStemAndBranchesMerchantableVolumeForOak(dbh, height);
		} else {
			return getStemAndBranchesMerchantableVolumeForBeech(dbh, height);
		} 
	}

	
	private double getStemAndBranchesMerchantableVolumeForOak(double dbh, double height) {
		double c130 = dbh * Math.PI;
		double hauteur = height;

		double volume = 0.;

		// TODO check where do these four parameters come from
		double d130 = c130 / Math.PI;
		double a6 = 0.602;	// formerly BOIS_FORT_BOUCHON_OAK_A6;
		double b6 = -0.168;	// formerly BOIS_FORT_BOUCHON_OAK_B6;
		double c6 = 6.0;	// formerly BOIS_FORT_BOUCHON_OAK_C6;
		double d6 = -0.00137;	// formerly BOIS_FORT_BOUCHON_OAK_D6;


		if (d130 > 6d && hauteur > 0d) {
			if (d130 <= 18d) {	// c130 <= 56.5
				volume = Math.PI * d130 * d130 / 40000.0 * hauteur * a6
				* (1. - Math.exp (b6 * (d130 - c6)))
				* Math.exp (d6 * d130);
			} else {
				double a18 = 239.752; 			// formerly BOIS_FORT_BOUCHON_OAK_A18;
				double b18 = -10.881; 			// formerly BOIS_FORT_BOUCHON_OAK_B18;
				double c18 = 0.13623; 			// formerly BOIS_FORT_BOUCHON_OAK_C18;
				double d18 = -0.11156E-03; 		// formerly BOIS_FORT_BOUCHON_OAK_D18;
				double e18 = 0.81058E-02; 		// formerly BOIS_FORT_BOUCHON_OAK_E18;
				double f18 = 0.22077E-02; 		// formerly BOIS_FORT_BOUCHON_OAK_F18;

				volume = (a18 + b18 * c130 + c18 * c130 * c130
						+ d18 * Math.pow (c130, 3.)
						+ e18 * Math.pow (hauteur, 3.)
						+ f18 * c130 * c130 * hauteur) / 1000.0;

				// Modified 18.02.2008 by Fred Mothe :
				// According to Patrick Vallet / Jean-François Dhote :
				// volume given by Bouchon model is known
				// to overestimate old trees whereas experimental
				// data show that it should be around 95% of total
				// volume for c130 > 120 cm
				if (c130 > 120.) {	// d130 > 38.2
					ValletTotalAboveGroundVolumeEquations vtagve = new ValletTotalAboveGroundVolumeEquations();
					double volTotal = vtagve.getTotalAboveGroundVolume(dbh, height, FgSpecies.OAK);
					volume = Math.min (volTotal * .95, volume);
				}
			}

			volume = Math.max (0., volume);
		}
		return volume;

	}

	
	// TODO check where do these four parameters come from
	private double getStemAndBranchesMerchantableVolumeForBeech(double dbh, double height) {
		// 2° -> Jean Bouchon's volume tables for "Bois Fort" (Stem + branches) FOR BEECH

		double a22 = 0.444907; 		// formerly BOIS_FORT_BOUCHON_BEECH_A22;
		double b22 = -0.107345e3; 	// formerly BOIS_FORT_BOUCHON_BEECH_B22;
		double c22 = 0.610582e-5; 	// formerly BOIS_FORT_BOUCHON_BEECH_C22;
		double d22 = 0.467061; 		// formerly BOIS_FORT_BOUCHON_BEECH_D22;
		double e22 = 0.126815e-2; 	// formerly BOIS_FORT_BOUCHON_BEECH_E22;

		double vBfT4 = 0.114460e3;	// formerly BOIS_FORT_BOUCHON_BEECH_4;
		double vBfT5 = 0.314282;	// formerly BOIS_FORT_BOUCHON_BEECH_5;
		double vBfT6 = -0.808045;	// formerly BOIS_FORT_BOUCHON_BEECH_6;

		double volume = 0.;
		double c130 = dbh * Math.PI;
		double hauteur = height;

		if (c130 >= 22.) {	// d130 >= 7
			double d130 = c130/Math.PI;

			double x = Math.PI  *  d130 * d130 * hauteur / 4000000.;
			x *= (a22 + b22 / Math.pow (d130, 3.) + c22 * d130 * d130
				+ d22 / hauteur + e22 * hauteur);

			volume = Math.max (0., x * (vBfT4 + vBfT5 * d130 + vBfT6 * hauteur));
		}
		
		return volume;
	}

	
	
	
	/**
	 * Calculates the over-bark wood volume for STEM ONLY, using a small end minimum diameter of 7 cm 
	 * using Bouchon Volume Tables (Bouchon 1982 for beech, Bouchon 1974 for oak)
	 * @see Bouchon, J. 1974. Les tarifs de cubage. ENGREF. 57 p.
	 * @param dbh the diameter at breast height (cm)
	 * @param height the height (m)
	 * @param species a FgSpecies enum variable Oak or Beech
	 * @return the volume (m3) 
	 */
	public double getStemMerchantableVolume(double dbh, double height, FgSpecies species) {
		if (species == FgSpecies.OAK) {
			return getStemMerchantableVolumeForOak(dbh, height);
		} else {
			return getStemMerchantableVolumeForBeech(dbh, height);
		} 
	}


	
	private double getStemMerchantableVolumeForBeech(double dbh, double height) {
		// 1b° -> Jean Bouchon's volume tables for "Bois Fort" stem only FOR OAK
		// from J.Bouchon, 1974 Les tarifs de cubage. INRA et ENGREF, Nancy(France), 57p + ann
		// (Annexe E : Tarifs de cubage à deux entrées pour le chêne de futaie Ligérien)

		// Jean Bouchon's volume table for STEM commercial wood (>7cm) FOR BEECH
//		double c130 = dbh * Math.PI;
		double hauteur = height;

		double stemVolume= 0.;
		// Bouchon Volume Tables (article Bouchon 1982, RFF XXXIV, 1982)
		double a0 = 3.26711e-5;	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_A0;
		double a1 = 5.28906e-5; 	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_A1;
		double a2 = -2.20572e-9; 	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_A2;

		double b1 = -2.41275e2;	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_B1;
		double b2 = 1.37238e-5;	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_B2;
		double b3 = 1.04979;	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_B3;
		double b4 = 2.85037e-3;	// formerly STEM_COMMERCIAL_VOLUME_BOUCHON_BEECH_B4;

//		double d130 = c130 / Math.PI;
		double d130 = dbh;

		stemVolume = a0 * d130 * d130 * hauteur
		+ a1 * d130 * hauteur
		+ a2 * d130 * d130 * d130 * hauteur * hauteur;
		stemVolume *= 1. + b1 /  (d130 * d130 * d130)
		+ b2 * d130 * d130
		+ b3 / hauteur + b4 * hauteur;
		stemVolume = Math.max (stemVolume, 0.);

		return stemVolume;
	}
	
	
	private double getStemMerchantableVolumeForOak(double dbh, double height) {
		
		double stemVolume= 0.;
		double c130 = dbh * Math.PI;
		double hauteur = height;
		
		// from J.Bouchon, 1974 Les tarifs de cubage. INRA et ENGREF, Nancy(France), 57p + ann
		// (Annexe E : Tarifs de cubage à deux entrées pour le chêne de futaie Ligérien)
		double d130 = c130 / Math.PI;
		double a18 = 222.49; 		// formerly BOIS_FORT_STEM_BOUCHON_OAK_A18;
		double b18 = -10.263; 	 	// formerly BOIS_FORT_STEM_BOUCHON_OAK_B18;
		double c18 = 0.14485; 		// formerly BOIS_FORT_STEM_BOUCHON_OAK_C18;
		double d18 = -0.13935E-03; 	// formerly BOIS_FORT_STEM_BOUCHON_OAK_D18;
		double e18 = 0.45401E-02; 	// formerly BOIS_FORT_STEM_BOUCHON_OAK_E18;
		double f18 = 0.93578E-03; 	// formerly BOIS_FORT_STEM_BOUCHON_OAK_F18;

		if (d130 > 6d && hauteur > 0d) {
			stemVolume = (a18 + b18 * c130 + c18 * c130 * c130
					+ d18 * Math.pow (c130, 3.)
					+ e18 * c130 * hauteur * hauteur
					+ f18 * c130 * c130 * hauteur) / 1000d;
			// PV suggestion15.01.2010:
			// (the model overestimates volume for young trees without BF in branches)
			stemVolume = Math.min (stemVolume, getStemAndBranchesMerchantableVolume(dbh, height, FgSpecies.OAK));
			stemVolume = Math.max (0., stemVolume);
		}
		
		return stemVolume;

	}
	
	public static void main(String[] args) {
		BouchonVolumeEquations bve = new BouchonVolumeEquations();
		double volume = bve.getStemMerchantableVolume(50, 30, FgSpecies.OAK);
		System.out.println(volume);
	}
	
	
}
