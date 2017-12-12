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

import java.util.Collection;
import java.util.Comparator;


/**
 * This class implements static method that are used in Albrecht et al.'s wind storm model.
 * @author M. Fortin - August 2010
 */
public class CommonUtility {
	
	@SuppressWarnings("rawtypes")
	public static class AWSTreeComparator implements Comparator {

		@Override
		public int compare(Object tree1, Object tree2) {
			double d1 = ((AWSTree) tree1).getDbhCm();
			double d2 = ((AWSTree) tree2).getDbhCm();
			
			if (d1 < d2) {
				return -1;							// t1 < t2
			} else if  (d1 > d2) {
				return 1;							// t1 > t2
			} else {
				return 0;							// t1 == t2
			}
		}
		
	}
	
	
	/**
	 * This static method finds the maximum value in a one-dimension array
	 * @param oArray an array of double values
	 * @return the index of the maximum value
	 */
	private static int findMaxInAnArrayOfDouble(double[] oArray) {
		double max = 0d;
		int pointer = -1;
		for (int i = 0; i < oArray.length; i++) {
			if (i == 0) {
				max = oArray[i];
				pointer = i;
			} else if (oArray[i] > max) {
				max = oArray[i];
				pointer = i;
			}
		}
		return pointer;
	}

	private static boolean isThereAnyElementDifferentFrom(double[] arrayDouble, double d) {
		final double VERY_SMALL = 1E-8;
		boolean diff = false;
		for (int i = 0; i < arrayDouble.length; i++) {
			if (Math.abs(arrayDouble[i] - d) > VERY_SMALL)
				diff = true;
		}
		return diff;
	}

	
	protected static double multiplyTwoArraysOfDouble(double[] array1, double[] array2) throws Exception {
		if (array1 == null || array2 == null) {
			throw new Exception("Error in method AlbrechtWindStormModel.multiplyTwoArraysOfDouble: One of the arrays is null!");
		} else if (array1.length == 0 || array2.length == 0) {
			throw new Exception("Error in method AlbrechtWindStormModel.multiplyTwoArraysOfDouble: One of the arrays has length of 0!");
		} else if (array1.length != array2.length) {
			throw new Exception("Error in method AlbrechtWindStormModel.multiplyTwoArraysOfDouble: Arrays are not the same length!");
		} else {
			double result = 0d;
			for (int i = 0; i < array1.length; i++) {
				result += array1[i] * array2[i];
			}
			return result;
		}
	}
	

	/**
	 * This static method returns either the dominant height or the dominant diameter
	 * @param trees a collection of Tree objects
	 * @param areaFactor the area factor
	 * @param heightRequested a boolean, true means dominant height is requested while false requests the dominant diameter
	 * @return either the dominant height or the dominant diameter
	 * @throws Exception
	 */
	protected static Double getDominantHeightOrDiameter(Collection<AWSTree> trees, double areaFactor, boolean heightRequested) throws Exception {		// true means the plot are weighted
		
		double dominantValue = 0;
		double numberForDominantPerHa = 100;
		
		double numberAdd;
		double numberTreesSoFar = 0;
	
		double[][] values = new double[2][trees.size()];
		
		int iter = 0;
		for (AWSTree tree : trees) {
			double number = tree.getNumber();
			if (number > 0) {
				if (heightRequested) {
					values[0][iter] = tree.getHeightM();
				}
				else {
					values[0][iter] = tree.getDbhCm();
				}
				values[1][iter] = number * areaFactor;
				iter++;
			}
		}
		
		double denum = (double) 1 / numberForDominantPerHa;
		int pointer = -1;
		
		do {
			pointer = CommonUtility.findMaxInAnArrayOfDouble(values[0]);
			
			if (numberTreesSoFar + values[1][pointer] <= numberForDominantPerHa) {
				numberAdd = values[1][pointer];
			} else {
				numberAdd = numberForDominantPerHa - numberTreesSoFar;
			}

			numberTreesSoFar += numberAdd;

			dominantValue += values[0][pointer] * numberAdd * denum;

			values[0][pointer] = -1d;
		} while (CommonUtility.isThereAnyElementDifferentFrom(values[0], -1d) && numberTreesSoFar < numberForDominantPerHa);

		return dominantValue;	
	}

}
