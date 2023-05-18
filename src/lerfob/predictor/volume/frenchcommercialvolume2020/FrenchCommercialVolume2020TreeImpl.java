/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 * 		Mathieu Fortin for Canadian WoodFibre Centre,
 * 							Canadian Forest Service, 
 * 							Natural Resources Canada
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
package lerfob.predictor.volume.frenchcommercialvolume2020;

import java.security.InvalidParameterException;

/**
 * A basic implementation of the FrenchCommercialVolume2020Tree interface.
 * @author Mathieu Fortin - August 2020
 */
public class FrenchCommercialVolume2020TreeImpl implements FrenchCommercialVolume2020Tree {

	final double pred;
	final double se;
	final double var;
	private final FrenchCommercialVolume2020TreeSpecies species;
	private double heightM;
	private double dbhCm;
	private final int id;
	private int monteCarloId;
	
	
	/**
	 * Protected constructor
	 * @param id an integer that stands for the tree id
	 * @param dbhCm the diameter at breast height (cm)
	 * @param heightM the tree height (m)
	 * @param speciesName the species name. It should be an enum name among the FrenchCommercialVolume2020TreeSpecies enum variable (e.g. Pinus halepensis).
	 * @param pred an optional prediction for test purposes.
	 * @param se the standard error of the prediction for test purposes.
	 * @param var prediction variance (mean + residual) for test purposes.
	 */
	protected FrenchCommercialVolume2020TreeImpl(int id, double dbhCm, double heightM, String speciesName, double pred, double se, double var) {
		this.id = id;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.pred = pred;
		this.se = se;
		this.var = var;
		String newSpeciesName = speciesName.trim().toUpperCase().replace(" ", "_");
		if (newSpeciesName.endsWith(".")) {
			newSpeciesName = newSpeciesName.substring(0, newSpeciesName.length() - 1);
		}
		try {
			this.species = FrenchCommercialVolume2020TreeSpecies.valueOf(newSpeciesName);
		} catch (Exception e) {
			throw new InvalidParameterException("The species name " + speciesName + " is invalid. Please see the FrenchCommercialVolume2014TreeSpecies enum variable.");
		}
	}

	/**
	 * Public constructor
	 * @param id an integer that stands for the tree id
	 * @param dbhCm the diameter at breast height (cm)
	 * @param heightM the tree height (m)
	 * @param speciesName the species name. It should be an enum name among the FrenchCommercialVolume2020TreeSpecies enum variable (e.g. Pinus halepensis).
	 */
	public FrenchCommercialVolume2020TreeImpl(int id, double dbhCm, double heightM, String speciesName) {
		this(id, dbhCm, heightM, speciesName, 0d, 0d, 0d);
	}
	
	
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
	public FrenchCommercialVolume2020TreeSpecies getFrenchCommercialVolume2020TreeSpecies() {return species;}

	void setMonteCarloId(int id) {this.monteCarloId = id;}
	
}
