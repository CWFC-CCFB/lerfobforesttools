/** 
 * Copyright (C) 2010-2012 LERFoB INRA/AgroParisTech - FVA Baden-Wurttemberg 
 * 
 * Authors: Mathieu Fortin, Axel Albrecht 
 * 
 * This file is part of the lerfob library. You can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * The awsmodel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should find a copy of the GNU lesser General Public License at
 * <http://www.gnu.org/licenses/>.
 */
package lerfob.windstormdamagemodels.awsmodel;

import java.util.ArrayList;
import java.util.Collection;

import lerfob.windstormdamagemodels.awsmodel.AWSStand;
import lerfob.windstormdamagemodels.awsmodel.AWSTreatment;
import lerfob.windstormdamagemodels.awsmodel.AWSTree;
import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;



/**
 * This class is an implementation of the AWSStand and AWSTreatment. Use only for testing.
 * @author Mathieu Fortin - February 2011
 */
public abstract class AWSStandImpl implements AWSStand, AWSTreatment {

	protected static enum TestStandVariable {hdRelRatio}
	
	private AWSTreeSpecies dominantSpecies;
	private double d100;
	private double h100;
	private int age;
	private double v;
	private double g;
	private boolean stagnantMoisture;
	private double topex;
	private double wind50;
	private double wind99;
	private boolean carbonateInUpperSoil;
	private int year;
	
	private double cumulatedRemovals;
	private double relativeRemovedVolume;
	private double thinningQuotient;
	private double relativeRemovedVolumeOfPreviousIntervention;
	private int nbYrsSincePreviousIntervention;
	private double relativeRemovedVolumeInPast10Yrs;
	private double thinningQuotientOfPast10Yrs;
	
	private Double relHDRatio;
	
	private Collection<AWSTree> trees;

	private String id;
	
	protected AWSStandImpl(String id,
			AWSTreeSpecies dominantSpecies,
			double d100,
			double h100,
			int age,
			double v,
			double g,
			boolean stagnantMoisture,
			double topex,
			double wind50,
			double wind99,
			boolean carbonateInUpperSoil,
			int year,
			double cumulatedRemovals,
			double relativeRemovedVolume,
			double thinningQuotient,
			double relativeRemovedVolumeOfPreviousIntervention,
			int nbYrsSincePreviousIntervention,
			double relativeRemovedVolumeInPast10Yrs,
			double thinningQuotientOfPast10Yrs,
			Double relHDRatio,
			double[] predictedProbabilities) {
		this.id = id;
		this.dominantSpecies = dominantSpecies ;
		this.d100 = d100;
		this.h100 = h100;
		this.age = age;
		this.v = v;
		this.g = g;
		this.stagnantMoisture = stagnantMoisture;
		this.topex = topex;
		this.wind50 = wind50;
		this.wind99 = wind99;
		this.carbonateInUpperSoil = carbonateInUpperSoil;
		this.year = year;
		this.cumulatedRemovals = cumulatedRemovals;
		this.relativeRemovedVolume = relativeRemovedVolume;
		this.thinningQuotient = thinningQuotient;
		this.relativeRemovedVolumeOfPreviousIntervention = relativeRemovedVolumeOfPreviousIntervention;
		this.nbYrsSincePreviousIntervention = nbYrsSincePreviousIntervention;
		this.relativeRemovedVolumeInPast10Yrs = relativeRemovedVolumeInPast10Yrs;
		this.thinningQuotientOfPast10Yrs = thinningQuotientOfPast10Yrs;
		this.relHDRatio = relHDRatio;
		
		init();
	}
	
	private void init() {
		trees = new ArrayList<AWSTree>();
	}
	
	protected void addTree(AWSTreeImpl tree) {
		trees.add(tree);
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAWSStandVariable(Enum variable) {
		if (variable instanceof StandVariable) {
			StandVariable standVariable = (StandVariable) variable;
			switch (standVariable) {
			case age:
				return age;
			case carbonateInUpperSoil:
				return carbonateInUpperSoil;
//			case d100:
//				return d100;
			case DominantSpecies:
				return dominantSpecies;
//			case h100:
//				return h100;
			case stagnantMoisture:
				return stagnantMoisture;
			case topex:
				return topex;
			case v:
				return v;
			case wind50:
				return wind50;
			case wind99:
				return wind99;
			case year:
				return year;
			default:
				return null;
			}
		} else if (variable instanceof TestStandVariable) {
			TestStandVariable testStandVariable = (TestStandVariable) variable;
			switch (testStandVariable) {
			case hdRelRatio:
				return relHDRatio;
			default:
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public Collection<AWSTree> getAlbrechtWindStormModelTrees() {return trees;}

	@Override
	public Object getAWSTreatmentVariable(TreatmentVariable variable) {
		switch (variable) {
		case cumulatedRemovals:
			return cumulatedRemovals;
		case nbYrsSincePreviousIntervention:
			return nbYrsSincePreviousIntervention;
		case relativeRemovedVolume:
			return relativeRemovedVolume;
		case relativeRemovedVolumeInPast10Yrs:
			return relativeRemovedVolumeInPast10Yrs;
		case relativeRemovedVolumeOfPreviousIntervention:
			return relativeRemovedVolumeOfPreviousIntervention;
		case thinningQuotient:
			return thinningQuotient;
		case thinningQuotientOfPast10Yrs:
			return thinningQuotientOfPast10Yrs;
		default:
			return null;
		}
	}

	protected AWSTreeSpecies getDominantSpecies() {return dominantSpecies;}

	@Override
	public double getBasalAreaM2Ha() {return g;}
	
	@Override
	public double getDominantHeightM() {return h100;}
	
	@Override
	public double getDominantDiameterCm() {return d100;}
	
	@Override
	public String getStandAndMonteCarloID() {return id;}

//	@Override
//	public void registerProbabilities(double[] probabilities) {}
//
//	@Override
//	public double[] getProbabilities() {return null;}

}
