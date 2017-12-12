/** 
 * Copyright (C) 2010-2012 LERFoB INRA/AgroParisTech - FVA Baden-Württemberg 
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lerfob.windstormdamagemodels.awsmodel.AWSStand.StandVariable;
import lerfob.windstormdamagemodels.awsmodel.AWSStandImpl.TestStandVariable;
import lerfob.windstormdamagemodels.awsmodel.AWSTreatment.TreatmentVariable;
import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;
import lerfob.windstormdamagemodels.awsmodel.AWSTree.TreeVariable;
import lerfob.windstormdamagemodels.awsmodel.CommonUtility.AWSTreeComparator;




/**
 * This class implements the equations of the different submodels that are part of Albrecht et al.'s wind storm model. This model
 * predict an average probability of wind-induced damage without knowledge or assumption of wind storm occurrence. The wind storm
 * occurrence is implicitly derived from the observed occurrence in the Baden-Wuerttemberg over the 1950-2005 period.
 * IMPORTANT : This model applies to 0.25-ha plot, with regular structure and homogeneous composition.
 * @see <a href=http://www.springerlink.com/content/4028611133q67450/fulltext.pdf> 
 * Albrecht, A., M. Hanewinkel, J. Bauhus, and U. Kohnle. 2010. How does silviculture affect storm damage in forests of south-western Germany? Results
 * from empirical modeling based on long-term observations. European Journal of Forest Research.
 * </a> 
 * @author M. Fortin and A. Albrecht - August 2010
 */
public class AlbrechtWindStormModel extends AWSModelCore implements ItemListener {

	private List<AWSTree> treeList;
	private List<Double> cumulativeFreq;
	private boolean treeListHasBeenSet;
//	private double numberOfTrees;
	
	private double[] predictionsAtStandLevel;
	private double predictionAtTreeLevel;
	
	private transient AlbrechtWindStormModelUI guiInterface;
	
	
	/**
	 * General constructor.
	 */
	public AlbrechtWindStormModel() {
		super();
		treeList = new ArrayList<AWSTree>();
		cumulativeFreq = new ArrayList<Double>();
		treeListHasBeenSet = false;
	}

	/**
	 * This method makes it possible to enable the stochastic mode. BY DEFAULT, 
	 * THE STOCHASTIC MODE IS DISABLED.
	 * @param enabled
	 */
	public void setStochasticModeEnabled(boolean enabled) {
		isStochasticModeEnabled = enabled;
	}

	public boolean isStochasticModeEnabled() {return isStochasticModeEnabled;}
	
	/**
	 * This method returns the occurrence of observing stand damage. 
	 * @return a Boolean (true = damaged, false = no damage)
	 */
	@SuppressWarnings("rawtypes")
	private Boolean getProbabilityOfStandDamage() {
		try {
			double pred = predictionsAtStandLevel[0];
			AWSTreeSpecies species = (AWSTreeSpecies) getStand().getAWSStandVariable(StandVariable.DominantSpecies);
			double deterministicCutPoint = (Double) ((Map) getCutPoint(SubModelID.firstStep)).get(species);
			return getOutcome(pred, deterministicCutPoint);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	
	private boolean getOutcome(double pred, double deterministicCutPoint) {
		double cutPoint;
		
		if (isStochasticModeEnabled()) {
			cutPoint = RANDOM_GENERATOR.nextDouble();
		} else {
			cutPoint = deterministicCutPoint;
		}
		
		return cutPoint < pred;
	}
	
	/**
	 * This method returns the occurrence of total damage (>75% of basal area damaged) given that stand damage occurred. 
	 * @return a Boolean (true = total damaged, false = not totally damage)
	 */
	@SuppressWarnings("rawtypes")
	private Boolean getProbabilityOfTotalStandDamage() {
		try {
			double pred = predictionsAtStandLevel[1];
			AWSTreeSpecies species = (AWSTreeSpecies) getStand().getAWSStandVariable(StandVariable.DominantSpecies);
			if (((Map) getCutPoint(SubModelID.secondStep)).get(species) != null) {
				double deterministicCutPoint = (Double) ((Map) getCutPoint(SubModelID.secondStep)).get(species);
				return getOutcome(pred, deterministicCutPoint);
			} else {				// at this point it means we are dealing with oak, pine or larch stands
				return false;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * This method returns the predicted proportion of stand damage (<75% of basal area damaged) given that the stand is not totally damaged. 
	 * @return the proportion (double)
	 */
	private double getProportionOfStandDamageIfNotTotallyDamaged() {
		try {
			return predictionsAtStandLevel[2];
		} catch (Exception e) {
			return -1d;
		}
	}
	
	/**
	 * This method generates three predictions : 1- the probability of stand damage, 
	 * 2- the probability of total damage (>75% of basal area damaged) conditional on the occurrence of stand damaged, and
	 * 3- the proportion of stand damage (<75% of basal area damaged) given that the stand is not totally damaged. NOTE: if
	 * there is no tree in this stand, the method returns an array containing null probabilities.
	 * @param stand a AWSStand object
	 * @param treatment a AWSTreatment object
	 * @return an array of three doubles corresponding to the above mentioned probabilities 
	 * @throws Exception
	 */
	public double[] getPredictionForThisStand(AWSStand stand, AWSTreatment treatment) throws Exception {
		boolean isNewTreatment;
		boolean isNewStand;

		try {
			if (getStand() == null || !getStand().equals(stand)) {
				setStand(stand);
				isNewStand = true;
				treeList.clear();
				cumulativeFreq.clear();				// the array is set to null
				treeListHasBeenSet = false;
			} else {
				isNewStand = false;
			}

			if (getTreatment() == null || !getTreatment().equals(treatment)) {
				setTreatment(treatment);
				isNewTreatment = true; 
			} else {
				isNewTreatment = false;
			}

			if (isNewStand || isNewTreatment) {
				predictionsAtStandLevel = new double[3];

				AWSTreeSpecies species = (AWSTreeSpecies) getStandVariable(StandVariable.DominantSpecies);

				predictionsAtStandLevel[0] = computeProbabilityOfStandDamage(species);
				predictionsAtStandLevel[1] = computeProbabilityOfTotalDamage(species);
				predictionsAtStandLevel[2] = computeProportionOfDamageIfNotTotallyDamaged(species);
				if (predictionsAtStandLevel[2] > 0.75) {
					predictionsAtStandLevel[2] = 0.75;			// protection in case of overestimation
				}
			}		
		} catch (Exception e) {
			System.out.println("Error while attempting to calculate prediction for stand " + stand.toString() + " : " + e.getMessage());
			e.printStackTrace();
			throw e;
		} 

		
		stand.registerProbabilities(predictionsAtStandLevel);
		return predictionsAtStandLevel;
	}
	

	/**
	 * This method returns a collection of damaged trees. The probability of damage is also recorded in the tree
	 * through the registerProbability method.
	 * @param stand a AWSStand instance
	 * @param treatment a AWSTreatment instance
	 * @return the list of damaged trees
	 * @throws Exception
	 */
	public Collection<AWSTree> getDamagedTreesOfThisStand(AWSStand stand, AWSTreatment treatment) throws Exception {
		Collection<AWSTree> damagedTrees = new ArrayList<AWSTree>();
		
		getPredictionForThisStand(stand, treatment);		// make sure that the stand predictions are up to date
		
		if (getProbabilityOfStandDamage()) {
			Collection<AWSTree> awsTrees = getStand().getAlbrechtWindStormModelTrees();
			if (getProbabilityOfTotalStandDamage()) {
				for (AWSTree tree : awsTrees) {
					tree.registerProbability(1d);		// a probability of 1 is recorded in this kind of event
					damagedTrees.add(tree);
				}
			} else {
				for (AWSTree tree : awsTrees) {
					if (getResultForThisTree(tree)) {
						damagedTrees.add(tree);
					}
				}
			}
		}
		return damagedTrees;
	}
	
	
	/**
	 * This method provides a prediction of damage for an individual tree. The method assumes the stand probabilities
	 * have been calculated prior to this.
	 * @param tree a AWSTree object
	 * @return a boolean (true = the tree is damaged, otherwise is false)
	 * @throws Exception
	 */
	protected Boolean getResultForThisTree(AWSTree tree) throws Exception {
		try {
			boolean isNewTree;
			if (getTree() == null || !getTree().equals(tree)) {
				setTree(tree);
				isNewTree = true;
			} else  {
				isNewTree = false;
			}

			if (isNewTree) {
				predictionAtTreeLevel = getProbabilityOfDamageForThisTree(tree);
				tree.registerProbability(predictionAtTreeLevel);
			}
			
			double deterministicCutPoint = (Double) getCutPoint(SubModelID.fourthStep);
			return getOutcome(predictionAtTreeLevel, deterministicCutPoint);
			
		} catch (Exception e) {
			System.out.println("Error while attempting to calculate prediction for tree " + tree.toString() + " : " + e.getMessage());
			e.printStackTrace();
			throw e;
		} 
	}

	
	/**
	 * This method computes the probability of observing damage in the stand for a particular tree species.
	 * @param species a TreeSpecies enum variable
	 * @return the probability (double)
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private double computeProbabilityOfStandDamage(AWSTreeSpecies species) throws Exception {
		
		double randomEffect = 0d;
		double[] beta = (double[]) ((Map) getParameters(SubModelID.firstStep)).get(species);
		double[] xVector = new double[beta.length];
		switch (species) {
		case Beech:
//			xVector[0] = 1d;	// intercept
//			xVector[1] = (Double) getStandVariable(StandVariable.d100);
//			xVector[2] = (Double) getTreatmentVariable(TreatmentVariable.cumulatedRemovals);
//			xVector[3] = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolume);
//			if (getStandVariable(TestStandVariable.hdRelRatio) != null) {
//				xVector[4] = (Double) getStandVariable(TestStandVariable.hdRelRatio);
//			} else {
//				xVector[4] = getReferenceTables().getRelativeH100D100Ratio(species, 
//						(Double) getStandVariable(StandVariable.d100),
//						(Double) getStandVariable(StandVariable.h100),
//						(Integer) getStandVariable(StandVariable.year));
//			}
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantDiameterCm();
			if (getStandVariable(TestStandVariable.hdRelRatio) != null) {
				xVector[2] = (Double) getStandVariable(TestStandVariable.hdRelRatio);
			} else {
				xVector[2] = getReferenceTables().getRelativeH100D100Ratio(species, 
						getStand().getDominantDiameterCm(),
						getStand().getDominantHeightM(),
						(Integer) getStandVariable(StandVariable.year));
			}
			break;
		case DouglasFir:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			xVector[2] = getStand().getDominantHeightM() * getStand().getDominantDiameterCm();
			xVector[3] = (Double) getTreatmentVariable(TreatmentVariable.thinningQuotient);
			xVector[4] = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolumeOfPreviousIntervention);
			randomEffect = getRandomEffect(RandomEffectID.Step1DouglasFir);
			break;
		case Oak:
			xVector[0] = 1d;	// intercept
			xVector[1] = (Double) getStandVariable(StandVariable.v);
			randomEffect = getRandomEffect(RandomEffectID.Step1Oak);
			break;
		case Spruce:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			xVector[2] = (Double) getTreatmentVariable(TreatmentVariable.thinningQuotient);
			xVector[3] = (Integer) getTreatmentVariable(TreatmentVariable.nbYrsSincePreviousIntervention);
			boolean stagnantMoisture = (Boolean) getStandVariable(StandVariable.stagnantMoisture);
			if (stagnantMoisture) {						// implementation according to the EFFECT option in the CLASS statement of the LOGISTIC procedure in SAS System
				xVector[4] = 1d;
			} else {
				xVector[4] = -1d;
			}
			xVector[5] = getReferenceTables().getStockDensity(species, 
					(Integer) getStandVariable(StandVariable.age), 
					getStand().getDominantHeightM(),
					getStand().getBasalAreaM2Ha());
			if (getStandVariable(TestStandVariable.hdRelRatio) != null) {
				xVector[6] = (Double) getStandVariable(TestStandVariable.hdRelRatio);
			} else {
				xVector[6] = getReferenceTables().getRelativeH100D100Ratio(species, 
						getStand().getDominantDiameterCm(),
						getStand().getDominantHeightM(),
						(Integer) getStandVariable(StandVariable.year));
			}
			randomEffect = getRandomEffect(RandomEffectID.Step1Spruce);
			break;
		case ScotsPine:
		case EuropeanLarch:
		case JapanLarch:
			xVector[0] = 1d;	// intercept
			xVector[1] = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolumeInPast10Yrs);
			xVector[2] = (Integer) getTreatmentVariable(TreatmentVariable.nbYrsSincePreviousIntervention);
			randomEffect = getRandomEffect(RandomEffectID.Step1PineAndLarch);
			break;
		case SilverFir:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			xVector[2] = getReferenceTables().getStockDensity(species, 
					(Integer) getStandVariable(StandVariable.age), 
					getStand().getDominantHeightM(),
					getStand().getBasalAreaM2Ha());
			xVector[3] = (Double) getStandVariable(StandVariable.topex);
			randomEffect = getRandomEffect(RandomEffectID.Step1SilverFir);
			break;
		}
		
		double xBeta = CommonUtility.multiplyTwoArraysOfDouble(xVector, beta) + randomEffect;
		double prob = Math.exp(xBeta) / (1d + Math.exp(xBeta));
		if (Double.isNaN(prob)) {
			throw new Exception("computeProbabilityOfStandDamage() yields NaN");
		}
		return prob;
	}
	
	/**
	 * This method computes the probability of observing more than 75% of damage in the stand for a particular tree species.
	 * @param species a TreeSpecies enum variable
	 * @return the probability (double)
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected double computeProbabilityOfTotalDamage(AWSTreeSpecies species) throws Exception {
		double randomEffect = 0d;
		double[] beta = (double[]) ((Map) getParameters(SubModelID.secondStep)).get(species);
		double[] xVector = null;
		if (beta !=null) {
			xVector = new double[beta.length];
		}
		switch (species) {
		case Beech:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			xVector[2] = (Double) getStandVariable(StandVariable.topex);
			xVector[3] = (Double) getStandVariable(StandVariable.wind99);
			break;
		case DouglasFir:
//			xVector[0] = 1d;	// intercept
//			xVector[1] = (Double) getStandVariable(StandVariable.h100);
//			xVector[2] = (Double) getTreatmentVariable(TreatmentVariable.cumulatedRemovals);
//			xVector[3] = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolumeOfPreviousIntervention);
//			boolean carbonateInUpperSoil = (Boolean) getStandVariable(StandVariable.carbonateInUpperSoil);
//			if (carbonateInUpperSoil) {
//				xVector[4] = 1d;
//			} else  {
//				// implementation according to the EFFECT option in the CLASS statement of the LOGISTIC procedure in SAS System
//				xVector[4] = -1d;
//			}
//			randomEffect = getRandomEffect(RandomEffectID.Step2DouglasFir);
			xVector[0] = 1d;	// intercept
			xVector[1] = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolume);
			double relativeRemovedVolumeOfPreviousIntervention = (Double) getTreatmentVariable(TreatmentVariable.relativeRemovedVolumeOfPreviousIntervention);
			xVector[2] = relativeRemovedVolumeOfPreviousIntervention;
			xVector[3] = relativeRemovedVolumeOfPreviousIntervention * ((Integer) getStandVariable(StandVariable.age));
			break;
		case Spruce:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			if (getStandVariable(TestStandVariable.hdRelRatio) != null) {
				xVector[2] = (Double) getStandVariable(TestStandVariable.hdRelRatio);
			} else {
				xVector[2] = getReferenceTables().getRelativeH100D100Ratio(species, 
						getStand().getDominantDiameterCm(),
						getStand().getDominantHeightM(),
						(Integer) getStandVariable(StandVariable.year));
			}
			break;
		case SilverFir:
			xVector[0] = 1d;	// intercept
			xVector[1] = getStand().getDominantHeightM();
			xVector[2] = (Double) getStandVariable(StandVariable.wind50);
			randomEffect = getRandomEffect(RandomEffectID.Step2SilverFir);
			break;
		case Oak:				// nothing to do for those two
		case ScotsPine:
		case EuropeanLarch:
		case JapanLarch:
			return 0d;
		}
		
		double xBeta = CommonUtility.multiplyTwoArraysOfDouble(xVector, beta) + randomEffect; 
		double prob = Math.exp(xBeta) / (1d + Math.exp(xBeta));
		if (Double.isNaN(prob)) {
			throw new Exception("computeProbabilityOfTotalDamage() yields NaN");
		}
		return prob;
	}
	

	/**
	 * This method computes the damage proportion from 0 to 75% for a particular tree species.
	 * @param species a TreeSpecies enum variable
	 * @return the probability (double)
	 * @throws Exception
	 */
	protected double computeProportionOfDamageIfNotTotallyDamaged(AWSTreeSpecies species) throws Exception {
		double randomEffect = 0d;
		double[] beta = (double[]) (getParameters(SubModelID.thirdStep));
		
		double[] xVector = new double[beta.length];
		xVector[0] = 1d;  // intercept
		if (species == AWSTreeSpecies.DouglasFir) {
			xVector[1] = 1d;
		} else  {
			xVector[1] = 0d;
		}
		xVector[2] = getStand().getDominantHeightM();
		xVector[3] = (Double) getTreatmentVariable(TreatmentVariable.thinningQuotientOfPast10Yrs);
		randomEffect = getRandomEffect(RandomEffectID.Step3VflLevel) + getRandomEffect(RandomEffectID.Step3FeldLevel);
		
		double xBeta = CommonUtility.multiplyTwoArraysOfDouble(xVector, beta) + randomEffect;
		double prob = Math.exp(xBeta) / (1d + Math.exp(xBeta));
		if (Double.isNaN(prob)) {
			throw new Exception("computeProportionOfDamageIfNotTotallyDamaged() yields NaN");
		}
		return prob;
	}
	
	
	/**
	 * This method computes the probability of observing damage for a particular tree.
	 * @param tree a AlbrechtWindStormModelTree object
	 * @return the probability (double)
	 * @throws Exception
	 */
	private double getProbabilityOfDamageForThisTree(AWSTree tree) throws Exception {
		
		AWSTreeSpecies species = (AWSTreeSpecies) getTreeVariable(TreeVariable.Species);
		
		double[] beta = (double[]) (getParameters(SubModelID.fourthStep));
		double[] xVector = new double[beta.length];
		switch (species) {
		case SilverFir:
			xVector[0] = 1d;
			xVector[1] = 0d;
			xVector[2] = 0d;
			xVector[3] = 0d;
			break;
		case Beech:
		case Oak:
			xVector[0] = 0d;
			xVector[1] = 1d;
			xVector[2] = 0d;
			xVector[3] = 0d;
			break;
		case ScotsPine:
		case EuropeanLarch:
		case JapanLarch:
			xVector[0] = 0d;
			xVector[1] = 0d;
			xVector[2] = 1d;
			xVector[3] = 0d;
			break;
		case Spruce:
		case DouglasFir:
			xVector[0] = 0d;
			xVector[1] = 0d;
			xVector[2] = 0d;
			xVector[3] = 1d;
			break;
		}
		
		if (getTreeVariable(AWSTreeImpl.TestTreeVariable.RelativeDbh) != null) {
			xVector[4] = (Double) getTreeVariable(AWSTreeImpl.TestTreeVariable.RelativeDbh);
		} else {
			xVector[4] = getRelativeTreeRankInDbh(tree);
		}
		
		if (getTreeVariable(AWSTreeImpl.TestTreeVariable.RelativeDbh) != null) {
			xVector[5] = (Double) getTreeVariable(AWSTreeImpl.TestTreeVariable.RelativeHDRatio);
		} else {
			double referenceH100D100;
			if (getStandVariable(TestStandVariable.hdRelRatio) != null) {
				referenceH100D100 = (Double) getStandVariable(TestStandVariable.hdRelRatio);
			} else {
				referenceH100D100 = getReferenceTables().getRelativeH100D100Ratio((AWSTreeSpecies) getStandVariable(StandVariable.DominantSpecies), 
					getStand().getDominantDiameterCm(),
					getStand().getDominantHeightM(),
					(Integer) getStandVariable(StandVariable.year));
			}
			xVector[5] = getTree().getHeightM() / getTree().getDbhCm() / referenceH100D100; // relative h/d - ratio  
		}

		double offset;
		if (getTreeVariable(AWSTreeImpl.TestTreeVariable.RegOffset) != null) {
			offset = (Double) getTreeVariable(AWSTreeImpl.TestTreeVariable.RegOffset);
		} else {
			offset = Math.log(getProportionOfStandDamageIfNotTotallyDamaged() / (1 - getProportionOfStandDamageIfNotTotallyDamaged()));
		}
		
		double xBeta = offset + CommonUtility.multiplyTwoArraysOfDouble(xVector, beta);
		double prob = Math.exp(xBeta) / (1d + Math.exp(xBeta));
		return prob;
	}

	
	/**
	 * This method computes the percentile rank of the tree with respect to the other trees of the stand. If several trees have the same dbh, it returns the maximum rank.
	 * @param tree a AlbrechtWindStormModelTree object
	 * @return the percentile rank (double)
	 */
	private double getRelativeTreeRankInDbh(AWSTree tree) {
		if (!treeListHasBeenSet) {
			setOrderedTreeArrays();
		}
		
		int i = treeList.indexOf(tree);
		if (i == -1) {
			throw new InvalidParameterException("This tree is not in the tree list!");
		}
				
		double numberOfTreesSmallerThanThisTree;
		if (i == 0) {
			numberOfTreesSmallerThanThisTree = 0d;
		} else {
			numberOfTreesSmallerThanThisTree = cumulativeFreq.get(i - 1);
		}
		
		return (double) numberOfTreesSmallerThanThisTree / cumulativeFreq.get(cumulativeFreq.size() - 1);
	}

	
	/**
	 * This method sets the array of AWSTree and the number of trees.
	 */
	@SuppressWarnings("unchecked")
	private void setOrderedTreeArrays() {
		Collection<AWSTree> trees = getStand().getAlbrechtWindStormModelTrees();
		for (AWSTree oneOfTheseTrees : trees) {
			treeList.add(oneOfTheseTrees);
		}
		Collections.sort(treeList, new AWSTreeComparator());
		for (AWSTree tree : treeList) {
			double number = tree.getNumber();
			if (!cumulativeFreq.isEmpty()) {
				number += cumulativeFreq.get(cumulativeFreq.size() - 1);
			}
			cumulativeFreq.add(number);
		}
		treeListHasBeenSet=true;
	}

	
	/**
	 * This method returns the GUI interface. If the interface has not been created so far, the method creates it and 
	 * sends it to the Event Dispatch Thread. The dialog pops up. 
	 * @return a AlbrechtWindStormMakerUI instance
	 */
	public AlbrechtWindStormModelUI getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new AlbrechtWindStormModelUI(this);
		}
		return guiInterface;
	}

	
	public void showInterface() {
		if (!getGuiInterface().isVisible()) {
			guiInterface.setVisible(true);
		}
	}

	
	public static void main(String[] args) {
		AlbrechtWindStormModel model = new AlbrechtWindStormModel();
		model.showInterface();
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (guiInterface != null && evt.getSource().equals(guiInterface.stochasticOptionCheckBox)) {
			setStochasticModeEnabled(guiInterface.stochasticOptionCheckBox.isSelected());			
		}
	}
	
}
