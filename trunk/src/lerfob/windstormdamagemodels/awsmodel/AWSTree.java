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

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.ExpansionFactorProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;


/**
 * This interface provides the method required at the tree level to implement
 * Albrecht et al.'s wind storm model.
 * @author M. Fortin - August 2010
 */
@SuppressWarnings("rawtypes")
public interface AWSTree extends Comparable,
								DbhCmProvider,
								HeightMProvider, 
								ExpansionFactorProvider {
	
	/**
	 * The TreeSpecies enum defines the species considered in the model.
	 * @author M. Fortin - August 2010
	 */
	public enum AWSTreeSpecies {
		Spruce,
		SilverFir,
		DouglasFir,
		ScotsPine,
		EuropeanLarch,
		JapanLarch,
		Oak,
		Beech,
	}
	
	public enum TreeVariable {
		Species(AWSTreeSpecies.Beech),	// a AWSTreeSpecies object
//		Height(0d) ,					// a double
//		Dbh(0d),						// a double
//		Number(0d)						// a double
		;
		
		private Object sample;
		
		private TreeVariable(Object obj) {
			this.sample = obj;
		}
		
		public Object cast(Object obj) throws Exception {return sample.getClass().cast(obj);}
	}

	public Object getAWSTreeVariable(Enum treeVariable);
	
	
	
	
	public void registerProbability(double probability);
	public double getProbability();
	
	
}
