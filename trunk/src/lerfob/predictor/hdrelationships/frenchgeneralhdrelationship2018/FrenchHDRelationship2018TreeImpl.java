/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

/**
 * The FrenchHDRelationship2018TreeImpl class is a basic implementation of the
 * FrenchHDRelationship2018Tree. It facilitates the connection to JNI.
 * @author Mathieu Fortin - December 2018
 */
public class FrenchHDRelationship2018TreeImpl implements FrenchHDRelationship2018Tree {

	protected double heightM;
	protected double dbhCm;
	protected final FrenchHdSpecies species;
	protected final FrenchHDRelationship2018PlotImpl plot;
	protected final int id;
	protected Double gOther;

	/**
	 * Constructor.
	 * @param heightM the height of the tree (m) or -1 if it was not observed
	 * @param dbhCm the diameter at breast height (cm)
	 * @param gOther the basal area of other trees (m2/ha)
	 * @param speciesName a String that corresponds to a FrenchHd2018Species enum
	 * @param plot a FrenchHDRelationship2018StandImpl instance that hosts the tree
	 */
	protected FrenchHDRelationship2018TreeImpl(int id,
			double heightM, 
			double dbhCm, 
			Double gOther, 
			String speciesName, 
			FrenchHDRelationship2018PlotImpl plot) {
		this.id = id;
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = FrenchHdSpecies.getFrenchHdSpeciesFromThisString(speciesName);
		this.plot = plot;
		plot.addTree(this); 
	}

	/**
	 * Constructor for NFI data. The basal area of other trees is calculated using the plot basal area.
	 * @param id an integer
	 * @param heightM the height of the tree (m) or -1 if it was not observed
	 * @param dbhCm the diameter at breast height (cm)
	 * @param speciesName a String that corresponds to a FrenchHd2018Species enum
	 * @param plot a FrenchHDRelationship2018StandImpl instance that hosts the tree
	 */
	public FrenchHDRelationship2018TreeImpl(int id,
			double heightM, 
			double dbhCm, 
			String speciesName, 
			FrenchHDRelationship2018PlotImpl plot) {
		this.id = id;
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = null;
		this.species = FrenchHdSpecies.getFrenchHdSpeciesFromThisString(speciesName);
		this.plot = plot;
		plot.addTree(this); 
	}

	
	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return null;
	}

	@Override
	public String getSubjectId() {return ((Integer) id).toString();}

	@Override
	public int getMonteCarloRealizationId() {return plot.getMonteCarloRealizationId();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCm = getLnDbhCmPlus1();
		return lnDbhCm * lnDbhCm;
	}

	@Override
	public FrenchHdSpecies getFrenchHDTreeSpecies() {return species;}

	protected double getGOther() {return gOther;}
	protected double getBasalAreaM2() {return getDbhCm() * getDbhCm() * Math.PI * 0.000025;}

}
