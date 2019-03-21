/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2019 Mathieu Fortin for Canadian Forest Service, 
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
package lerfob.predictor.thinners.frenchnfithinner2018;

import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;

public class FrenchNFIThinnerPlotImpl implements FrenchNFIThinnerPlot {

	private final String plotId;
	private final double basalAreaM2Ha;
	private final double nbStemsHa;
	private final double slopeInclinationPercent;
	private final boolean wasThereAnySiliviculturalTreatmentInTheLast5Years;
	private final double probabilityOfBeingOnPrivateLand;
	
	public FrenchNFIThinnerPlotImpl(String plotId,
			String region, 
			double basalAreaM2Ha, 
			double nbStemsHa, 
			double slopeInclinationPercent,
			boolean wasThereAnySiliviculturalTreatmentInTheLast5Years,
			double probabilityOfBeingOnPrivateLand) {
		this.plotId = plotId;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.nbStemsHa = nbStemsHa;
		this.slopeInclinationPercent = slopeInclinationPercent;
		this.wasThereAnySiliviculturalTreatmentInTheLast5Years = wasThereAnySiliviculturalTreatmentInTheLast5Years;
		this.probabilityOfBeingOnPrivateLand = probabilityOfBeingOnPrivateLand;
		// TODO See how the volume by species can be constructed here
	}
	
	@Override
	public FrenchRegion2016 getFrenchRegion2016() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public double getNumberOfStemsHa() {return nbStemsHa;}

	@Override
	public double getSlopeInclinationPercent() {return slopeInclinationPercent;}

	@Override
	public String getSubjectId() {return plotId;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years() {
		return wasThereAnySiliviculturalTreatmentInTheLast5Years;
	}

	@Override
	public double getProbabilityOfBeingOnPrivateLand() {
		return probabilityOfBeingOnPrivateLand;
	}

	@Override
	public Map<FrenchNFIThinnerSpecies, Double> getVolumeM3BySpecies() {
		// TODO Auto-generated method stub
		return null;
	}

}
