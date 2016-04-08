/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.carbonbalancetool.productionlines;

import java.io.Serializable;

import lerfob.carbonbalancetool.CarbonAccountingToolSettings;

/**
 * This class handles the life cycle analysis. The lca are embedded in an enum variable 
 * which controls the availability of the lca. 
 * @author Mathieu Fortin - April 2010
 */
public class LifeCycleAnalysis implements Serializable {

	private static final long serialVersionUID = 20100914L;

	public static enum ReferenceLCA {
			FCBA_OAKHOUSING(new LifeCycleAnalysis("CharpenteChene",
					0.0403574144,
					6.509505703E-5,
					3.4410646387E-6)),
			FCBA_OSB(new LifeCycleAnalysis("OSB", 
					0.19588679,
					6.5030188E-4,
					5.1396556E-6)),
			FCBA_MDF_MEDIUM(new LifeCycleAnalysis("MDF_Medium",
					0.20625,
					9.21428571E-4,
					7.17857142E-6)),
			FCBA_PANNEAUP4(new LifeCycleAnalysis("PanneauP4", 
					0.3758045, 
					0.001876162,
					1.297973778E-05)),
			KCL_LWCPAPER(new LifeCycleAnalysis("LWCPaper",			// in Hischier (2007) ecoinvent
					0.15719177,
					0d,				// unavailable
					0d)),
			ECOINVENT_LOGGINGHW(new LifeCycleAnalysis("SimpleLoggingHardwood",
					0.010989,
					0d,
					0d)),			// unavailable
			CORRIM_LVL(new LifeCycleAnalysis("CorrimLaminatedVeenerLumber",		// in Puettmann and Wilson (2005) (LVL) with assumed yield of 0.4
					0.068,
					1.64E-04,
					4.44E-04)),
			CORRIM_VENEER(new LifeCycleAnalysis("CorrimVeener",		// in Puettmann and Wilson (2005) (Plywood) with assumed yield of 0.56
					0.07168,
					1.68E-04,
					5.32E-04)),
			KCL_UNCOATEDFINEPAPER(new LifeCycleAnalysis("UncoatedFinePaper",			// in Hischier (2007) ecoinvent
					0.15555,
					0d,				// unavailable
					0d))
			;
			
		private LifeCycleAnalysis lca;
	
		ReferenceLCA(LifeCycleAnalysis lca) {
			this.lca = lca;
		}
		
		public LifeCycleAnalysis getLCA() {return lca;}
		
		@Override
		public String toString() {
			return getLCA().strActivity;
		}
	}
	

	private String strActivity;
	private double dCO2EmissionPerUnit;
	private double dCH4EmissionPerUnit;
	private double dN2OEmissionPerUnit;

	
	/**
	 * A life cycle analysis for production and transportation only. ALL EMISSION
	 * ARE GIVEN IN Mg per M3 OF RAW TIMBER. Serves to build the ReferenceLCA enum.
	 * @param lcaName the lifecycle analysis name
	 * @param CO2emission the amount of CO2 emissions (Mg)
	 * @param CH4emission the amount of CH4 emissions (Mg)
	 * @param N2Oemission the amount of NO2 emissions (Mg)
	 */
	private LifeCycleAnalysis(String lcaName, 
			double CO2emission,
			double CH4emission,
			double N2Oemission) {
		strActivity = lcaName;
		dCO2EmissionPerUnit = CO2emission;
		dCH4EmissionPerUnit = CH4emission;
		dN2OEmissionPerUnit = N2Oemission;
	}
	
	/**
	 * This constructor builds independent life cycle analysis from the ReferenceLCA enum. So, two independent
	 * life cycles analysis could originate from the same reference life cycle.
	 */
	public LifeCycleAnalysis(ReferenceLCA referenceLCA) {
		this(	referenceLCA.lca.strActivity,
				referenceLCA.lca.dCO2EmissionPerUnit, 
				referenceLCA.lca.dCH4EmissionPerUnit,
				referenceLCA.lca.dN2OEmissionPerUnit);
	}

	/**
	 * This method returns the carbon equivalent emissions per m3 of before-process material.
	 * @return the carbon equivalent emissions per m3 (double)
	 */
	private double getCarbonEquivalentEmissionPerM3() {
		double CO2 = dCO2EmissionPerUnit 
		 		+ (dCH4EmissionPerUnit * CarbonAccountingToolSettings.CH4_CO2_EQUIVALENT)
				+ (dN2OEmissionPerUnit * CarbonAccountingToolSettings.N2O_CO2_EQUIVALENT)
				;
		return CO2 * CarbonAccountingToolSettings.CO2_C_FACTOR;
	}

	public double getCarbonEmissionPerM3() {
		return getCarbonEquivalentEmissionPerM3();
	}
	
	/**
	 * This method enables the comparison with another life cycle analysis.
	 * @param lca = a LifeCycleAnalysis instance
	 * @return true if the lca equals this or false otherwise
	 */
	public boolean compare(LifeCycleAnalysis lca) {
		double verySmall = 10E-8;
		if (!this.strActivity.equals(lca.strActivity)) {
			return false;
		}
		if (Math.abs(this.dCO2EmissionPerUnit - lca.dCO2EmissionPerUnit) > verySmall) {
			return false;
		}
		if (Math.abs(this.dCH4EmissionPerUnit - lca.dCH4EmissionPerUnit) > verySmall) {
			return false;
		}
		if (Math.abs(this.dN2OEmissionPerUnit - lca.dN2OEmissionPerUnit) > verySmall) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LifeCycleAnalysis)) {
			return false; 
		} else {
			LifeCycleAnalysis lca = (LifeCycleAnalysis) obj;
			if (!lca.strActivity.equals(strActivity)) {
				return false;
			}
			if (lca.dCO2EmissionPerUnit != dCO2EmissionPerUnit) {
				return false;
			}
			if (lca.dCH4EmissionPerUnit != dCH4EmissionPerUnit) {
				return false;
			}
			if (lca.dN2OEmissionPerUnit != dN2OEmissionPerUnit) {
				return false;
			}
			return true;
		}
	}
//	public static void main (String[] args) {
//		for (ReferenceLCA lca : ReferenceLCA.values()) {
//			System.out.println(lca.toString() + " : " + lca.getLCA().getCarbonEmissionPerM3().getMean());
//		}
//	}
	
}