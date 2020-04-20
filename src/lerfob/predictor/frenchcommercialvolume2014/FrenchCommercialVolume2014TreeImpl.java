/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2020 Mathieu Fortin for Canadian Forest Service, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.frenchcommercialvolume2014;

import java.security.InvalidParameterException;

/**
 * A basic implementation of the FrenchCommercialVolume2014Tree interface.
 * @author Mathieu Fortin - April 2020
 */
public class FrenchCommercialVolume2014TreeImpl implements FrenchCommercialVolume2014Tree {

	private final FrenchCommercialVolume2014TreeSpecies species;
	private final String subjectId;
	private int monteCarloId;
	private double dbhCm;
	private double heightM;

	/**
	 * General constructor.
	 * @param speciesName a String that represents the species. The valid species name 
	 * are the enum name of the FrenchCommercialVolume2014TreeSpecies enum variable.
	 * @param subjectId a String that stands for the tree id
	 * @param dbhCm the diameter at breast height (cm)
	 * @param heightM the tree height (m)
	 * 
	 * @see the FrenchCommercialVolume2014TreeSpecies enum
	 */
	public FrenchCommercialVolume2014TreeImpl(String speciesName, String subjectId, double dbhCm, double heightM) {
		try {
			species = FrenchCommercialVolume2014TreeSpecies.valueOf(speciesName.toUpperCase().replace(" ", "_"));
		} catch (Exception e) {
			throw new InvalidParameterException("The species " + speciesName + "is not a valid species name! Please see the FrenchCommercialVolume2014TreeSpecies enum.");
		}
		this.subjectId = subjectId;
		this.monteCarloId = 0;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
	}
	
	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getSquaredDbhCm() {
		return getDbhCm() * getDbhCm();
	}

	@Override
	public double getHeightM() {
		return heightM;
	}

	@Override
	public String getSubjectId() {
		return subjectId;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloId;
	}

	public void setMonteCarloRealizationId(int i) {
		monteCarloId = i;
	}
	
	@Override
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies() {
		return species;
	}

}
