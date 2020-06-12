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

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;
import lerfob.simulation.covariateproviders.plotlevel.FrenchRegion2016Provider;

/**
 * Basic implementation of the FrenchNFIThinnerPlot interface.
 * @author Mathieu Fortin - March 2019
 */
public class FrenchNFIThinnerPlotImpl implements FrenchNFIThinnerPlot {

	private final String plotId;
	private final double basalAreaM2Ha;
	private final double nbStemsHa;
	private final double slopeInclinationPercent;
	private final boolean wasThereAnySiliviculturalTreatmentInTheLast5Years;
	private final double probabilityOfBeingOnPrivateLand;
	private final FrenchRegion2016 region;
	private final Map<FrenchNFIThinnerSpecies, Double> volumeMap;
	
	/**
	 * Constructor.
	 * @param plotId
	 * @param regionName
	 * @param basalAreaM2Ha
	 * @param nbStemsHa
	 * @param slopeInclinationPercent
	 * @param wasThereAnySiliviculturalTreatmentInTheLast5Years
	 * @param probabilityOfBeingOnPrivateLand
	 */
	public FrenchNFIThinnerPlotImpl(String plotId,
			String regionName, 
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
		this.region = FrenchRegion2016Provider.getFrenchRegion2016FromThisString(regionName);
		this.volumeMap = new HashMap<FrenchNFIThinnerSpecies, Double>();
	}

	/**
	 * This method sets the volume of the difference species that compose the plot.
	 * @param speciesName a String that represents the species name
	 * @param volumeHa a double (volume per hectare) 
	 */
	public void setVolumeForThisSpecies(String speciesName, double volumeHa) {
		FrenchNFIThinnerSpecies species = FrenchNFIThinnerPredictor.getFrenchNFIThinnerSpeciesFromThisString(speciesName);
		volumeMap.put(species, volumeHa);
	}
	
	
	@Override
	public FrenchRegion2016 getFrenchRegion2016() {return region;}

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
		return volumeMap;
	}

}
