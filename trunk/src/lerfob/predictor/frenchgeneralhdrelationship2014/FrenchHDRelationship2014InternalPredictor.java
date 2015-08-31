/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.frenchgeneralhdrelationship2014;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Predictor.SiteIndexClass;
import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Tree.FrenchHdSpecies;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.hdrelationships.HDRelationshipModel;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.TruncatedGaussianEstimate;

@SuppressWarnings("serial")
public class FrenchHDRelationship2014InternalPredictor extends HDRelationshipModel<FrenchHDRelationship2014Stand, FrenchHDRelationship2014Tree> {

	private static final Map<SpeciesType, Double> PhiParameters = new HashMap<SpeciesType, Double>();
	static {
		PhiParameters.put(SpeciesType.ConiferousSpecies, 0.02619872948641); // taken from Quebec HD relationships
		PhiParameters.put(SpeciesType.BroadleavedSpecies, 0.04468342698978); // taken from Quebec HD relationships
	}
	
	private List<Integer> effectList;
	private final FrenchHdSpecies species;
	private final Map<SiteIndexClass, TruncatedGaussianEstimate> siteIndexClasses;
	private Map<HierarchicalLevel, Map<Integer, Estimate<?>>> blupsLibraryBackup;
	private List<Integer> blupEstimationDoneBackup;
	private SiteIndexClass currentSiteIndexClass;
	
	protected FrenchHDRelationship2014InternalPredictor(boolean isParametersVariabilityEnabled,	
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			FrenchHdSpecies species) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.species = species;
		siteIndexClasses = new HashMap<SiteIndexClass, TruncatedGaussianEstimate>();
		currentSiteIndexClass = SiteIndexClass.Unknown;
		blupsLibraryBackup = new HashMap<HierarchicalLevel, Map<Integer, Estimate<?>>>();
		blupEstimationDoneBackup = new ArrayList<Integer>();
	}

	
	protected void setDefaultBeta(GaussianEstimate defaultBeta) {
		this.defaultBeta = defaultBeta;
		oXVector = new Matrix(1, this.defaultBeta.getMean().m_iRows);
	}
	
	protected void setDefaultRandomEffects(HierarchicalLevel level, GaussianEstimate estimate) {
		defaultRandomEffects.put(level, estimate);
		if (level == HierarchicalLevel.Plot) {
			for (SiteIndexClass siteIndex : SiteIndexClass.values()) {
				setSiteIndexGaussianEstimate(level, siteIndex);
			}
		}
	}
	
	/**
	 * This method allows to tweak the plot random effect in order to reproduce a sort of site index. Does nothing if 
	 * the siteIndexClass parameter is null.
	 * @param siteIndexClass a SiteIndexClass enum
	 */
	protected void emulateSiteIndexClassForThisSpecies(SiteIndexClass siteIndexClass) {
		if (siteIndexClass != null && this.currentSiteIndexClass != siteIndexClass) {
			if (siteIndexClass == SiteIndexClass.Unknown) {		// we are going back to normal
				blupsLibrary.clear();
				blupsLibrary.putAll(blupsLibraryBackup);
				blupEstimationDone.clear();
				blupEstimationDone.addAll(blupEstimationDoneBackup);
			} else if (currentSiteIndexClass == SiteIndexClass.Unknown) {	// we are setting a site index 
				blupsLibraryBackup.clear();
				blupsLibraryBackup.putAll(blupsLibrary);
				blupsLibrary.clear();
				blupEstimationDoneBackup.clear();
				blupEstimationDoneBackup.addAll(blupEstimationDone);
				blupEstimationDone.clear();
			}
			currentSiteIndexClass = siteIndexClass;
		}
	}
	
	@Override
	protected synchronized void predictHeightRandomEffects(FrenchHDRelationship2014Stand stand) {
		if (currentSiteIndexClass == SiteIndexClass.Unknown) {
			super.predictHeightRandomEffects(stand);
		} else {	// we have tweaked the plot random effect to account for the site index class
			if (!blupsLibrary.containsKey(HierarchicalLevel.Plot)) {
				blupsLibrary.put(HierarchicalLevel.Plot, new HashMap<Integer, Estimate<?>>());
			}
			Map<Integer, Estimate<?>> innerMap = blupsLibrary.get(HierarchicalLevel.Plot);
			if (!innerMap.containsKey(stand.getSubjectId())) {
				innerMap.put(stand.getSubjectId(), siteIndexClasses.get(currentSiteIndexClass));
				blupEstimationDone.add(stand.getSubjectId());
			}
		}
	}
	
	private void setSiteIndexGaussianEstimate(HierarchicalLevel level, SiteIndexClass siteIndex) {
		GaussianEstimate levelRandomEffects = defaultRandomEffects.get(level);
		TruncatedGaussianEstimate truncatedEstimate;
		Matrix stdMatrix = levelRandomEffects.getVariance().elementwisePower(0.5);
		switch(siteIndex) {
		case I:
			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setLowerBound(stdMatrix.scalarMultiply(0.999));
			siteIndexClasses.put(siteIndex, truncatedEstimate);
			break;
		case II:
			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setLowerBound(stdMatrix.scalarMultiply(-0.7388));
			truncatedEstimate.setUpperBound(stdMatrix.scalarMultiply(0.999));
			siteIndexClasses.put(siteIndex, truncatedEstimate);
			break;
		case III:
			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setUpperBound(stdMatrix.scalarMultiply(-0.7388));
			siteIndexClasses.put(siteIndex, truncatedEstimate);
			break;
		case Unknown:
			break;
		default:
			break;
		}
	}
	
	protected void setResidualVariance(Matrix sigma2) {
		double correlationParameters = PhiParameters.get(species.getSpeciesType());
		GaussianErrorTermEstimate estimate = new GaussianErrorTermEstimate(sigma2, correlationParameters, TypeMatrixR.LINEAR);
		defaultResidualError.put(ErrorTermGroup.Default, estimate);
	}

	protected void setEffectList(Matrix mat) {
		effectList = new ArrayList<Integer>();
		for (int i = 0; i < mat.m_iRows; i++) {
			effectList.add((int) mat.m_afData[i][0]); 
		}
	}
	
	/**
	 * This method computes the fixed effect prediction and put the prediction, the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies in any cases no matter
	 * it is deterministic or stochastic.
	 * @param stand a HeightableStand instance
	 * @param tree a HeightableTree instance
	 * @return a RegressionElement instance
	 */
	@Override
	protected synchronized RegressionElements fixedEffectsPrediction(FrenchHDRelationship2014Stand stand, FrenchHDRelationship2014Tree tree) {
		Matrix modelParameters = getParametersForThisRealization(stand);
		
		double basalAreaMinusSubj = stand.getBasalAreaM2HaMinusThisSubject(tree);
		double slope = stand.getSlopePercent();
		
		oXVector.resetMatrix();
		int pointer = 0;
		
		double lnDbh = tree.getLnDbhCmPlus1();
		double socialIndex = tree.getDbhCm() - stand.getMeanQuadraticDiameterCm();
		double lnDbh2 = tree.getSquaredLnDbhCmPlus1();

		double harvested = 0d;
		if (stand.isInterventionResult()) {
			harvested = 1d;
		}
		
		for (Integer integer : effectList) {
			switch(integer) {
			case 1:
				oXVector.m_afData[0][pointer] = basalAreaMinusSubj * lnDbh2;
				pointer++;
				break;
			case 2:
				oXVector.m_afData[0][pointer] = lnDbh;
				pointer++;
				break;
			case 3:
				oXVector.m_afData[0][pointer] = basalAreaMinusSubj * lnDbh;
				pointer++;
				break;
			case 4:
				oXVector.m_afData[0][pointer] = harvested * lnDbh;
				pointer++;
				break;
			case 5:
				oXVector.m_afData[0][pointer] = lnDbh * slope;
				pointer++;
				break;
			case 6:
				oXVector.m_afData[0][pointer] = socialIndex * lnDbh;
				pointer++;
				break;
			case 7:
				oXVector.m_afData[0][pointer] = socialIndex * socialIndex * lnDbh;
				pointer++;
				break;
			case 8:
				oXVector.m_afData[0][pointer] = lnDbh2;
				pointer++;
				break;
			default:
				throw new InvalidParameterException("This effect index " + integer + " is not recognized!");
			}
		}

		double fResult = 1.3 + oXVector.multiply(modelParameters).m_afData[0][0];
		
		Matrix Z_i = new Matrix(1,1);
		Z_i.m_afData[0][0] = lnDbh;	// design vector for the plot random effect

		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.Z_tree = Z_i;

		return regElements;
	}


	protected Matrix getBlups(FrenchHDRelationship2014Stand stand) {
		if (blupsLibrary.get(HierarchicalLevel.Plot) != null) {
			return blupsLibrary.get(HierarchicalLevel.Plot).get(stand.getSubjectId()).getMean();
		} else {
			return null;
		}
	}


	@Override
	protected Collection<FrenchHDRelationship2014Tree> getTreesFromStand(FrenchHDRelationship2014Stand stand) {
		Collection<FrenchHDRelationship2014Tree> treesToBeReturned = new ArrayList<FrenchHDRelationship2014Tree>();
		Collection<?> trees = stand.getTrees(StatusClass.alive);
		if (trees != null && !trees.isEmpty()) {
			for (Object tree : trees) {
				if (tree instanceof FrenchHDRelationship2014Tree) {
					FrenchHDRelationship2014Tree t = (FrenchHDRelationship2014Tree) tree;
					if (t.getFrenchHDTreeSpecies() == species) {
						treesToBeReturned.add(t);
					}
				}
			}
		}
		return treesToBeReturned;
	}
	
}
