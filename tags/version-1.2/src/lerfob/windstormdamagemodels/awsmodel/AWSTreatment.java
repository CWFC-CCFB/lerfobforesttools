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


public interface AWSTreatment {
	
	/**
	 * This Enum variable defines all the treatment-related variables required by this model.
	 * @author M. Fortin and A. Albrecht - August 2010
	 */
	public enum TreatmentVariable {
		/**
		 * The cumulative merchantable volume of all thinnings to date as a ratio of total volume production (current volume + all thinnings to date)
		 */
		cumulatedRemovals(0d),	
		/**
		 * 	The ratio between removed volume and volume prior to thinning (values between 0 to 1)
		 */
		relativeRemovedVolume(0d),
		/**
		 * The ratio between mean quadratic diameter (mqd) of the removed trees and mqd of the stand prior to thinning 
		 */
		thinningQuotient(0d),									
		/**
		 * The ratio between removed volume and volume prior to the last thinning (values between 0 to 1), which is on average 5 years (3-7 yrs) before the current date
		 */
		relativeRemovedVolumeOfPreviousIntervention(0d),
		/**
		 * The number of years since the last thinning
		 */
		nbYrsSincePreviousIntervention((int) 0),						
		/**
		 * The average ratio between removed volume and volume prior to thinning within the past 10 years.
		 */
		relativeRemovedVolumeInPast10Yrs(0d),					
		/**
		 * The average ratio between mean quadratic diameter (mqd) of the removed trees and mqd of the stand prior to thinning within the past 10 years
		 */
		thinningQuotientOfPast10Yrs(0d)							
		;
		
		private Object sample;
		
		private TreatmentVariable(Object obj) {
			this.sample = obj;
		}
		
		public Object cast(Object obj) throws Exception {return sample.getClass().cast(obj);}
	}
	

	public Object getAWSTreatmentVariable(TreatmentVariable treatmentVariable);
	
}
