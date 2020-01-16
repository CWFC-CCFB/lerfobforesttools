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

import lerfob.windstormdamagemodels.awsmodel.AWSTree;

/**
 * Simple implementation of AWSTree for testing.
 * @author M. Fortin - August 2010
 */
public abstract class AWSTreeImpl implements AWSTree {

	protected static enum TestTreeVariable {RelativeDbh, RelativeHDRatio, RegOffset}
	
	private AWSTreeSpecies species;
	private double relativeDbh;
	private double relativeHDRatio;
	private double regOffset;
	
	protected AWSTreeImpl (AWSTreeSpecies species,
			double relativeDbh,
			double relativeHDRatio,
			double regOffset) {
		this.species = species;
		this.relativeDbh = relativeDbh;
		this.relativeHDRatio = relativeHDRatio;
		this.regOffset = regOffset;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAWSTreeVariable(Enum variable) {
		if (variable instanceof AWSTree.TreeVariable) {
			AWSTree.TreeVariable treeVariable = (AWSTree.TreeVariable) variable;
			switch (treeVariable) {
			case Species:
				return species;
			default:
				return null;
			}
		} else if (variable instanceof AWSTreeImpl.TestTreeVariable) {
			AWSTreeImpl.TestTreeVariable testTreeVariable = (AWSTreeImpl.TestTreeVariable) variable;
			switch (testTreeVariable) {
			case RelativeDbh:
				return relativeDbh;
			case RelativeHDRatio:
				return relativeHDRatio;
			case RegOffset:
				return regOffset;
			default:
				return null;
			} 
		} else {
			return null;
		}
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}


	@Override
	public double getHeightM() {return -1d;}
	
	@Override 
	public double getDbhCm() {return -1d;}
	
}
