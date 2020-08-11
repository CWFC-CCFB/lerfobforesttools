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
package lerfob.predictor.volume.frenchcommercialvolume2014;

import java.security.InvalidParameterException;

/**
 * A basic implementation of the FrenchCommercialVolume2014Tree interface.
 * @author Mathieu Fortin - April 2020
 */
public class FrenchCommercialVolume2014TreeImpl implements FrenchCommercialVolume2014Tree {

	protected double pred;
	private final FrenchCommercialVolume2014TreeSpecies species;
	private double heightM;
	private double dbhCm;
	private final int id;
	private int monteCarloId;
	
	/**
	 * Protected constructor
	 * @param id an integer that stands for the tree id
	 * @param dbhCm the diameter at breast height (cm)
	 * @param heightM the tree height (m)
	 * @param speciesName the species name. It should be an enum name among the FrenchCommercialVolume2014TreeSpecies enum variable (e.g. Pinus halepensis).
	 * @param pred an optional prediction for test purposes.
	 */
	protected FrenchCommercialVolume2014TreeImpl(int id, double dbhCm, double heightM, String speciesName, double pred) {
		this.id = id;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.pred = pred;
		String newSpeciesName = speciesName.trim().toUpperCase().replace(" ", "_");
		try {
			this.species = FrenchCommercialVolume2014TreeSpecies.valueOf(newSpeciesName);
		} catch (Exception e) {
			throw new InvalidParameterException("The species name " + speciesName + " is invalid. Please see the FrenchCommercialVolume2014TreeSpecies enum variable.");
		}
	}

	/**
	 * Public constructor
	 * @param id an integer that stands for the tree id
	 * @param dbhCm the diameter at breast height (cm)
	 * @param heightM the tree height (m)
	 * @param speciesName the species name. It should be an enum name among the FrenchCommercialVolume2014TreeSpecies enum variable (e.g. Pinus halepensis).
	 */
	public FrenchCommercialVolume2014TreeImpl(int id, double dbhCm, double heightM, String speciesName) {
		this(id, dbhCm, heightM, speciesName, 0d);
	}
	
	
	protected double getPred() {return pred;}
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public String getSubjectId() {return ((Integer) id).toString();}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	@Override
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies() {return species;}

	void setMonteCarloId(int id) {this.monteCarloId = id;}
	
}
