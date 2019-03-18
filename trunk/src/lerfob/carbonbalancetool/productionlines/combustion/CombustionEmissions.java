/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2019 Mathieu Fortin for Canadian Forest Service, 
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
package lerfob.carbonbalancetool.productionlines.combustion;

import java.util.HashMap;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CombustionEmissions class handles the combustion factors provided by the 
 * LRGP in the SATAN research project (2015-2019).
 * @author Mathieu Fortin - March 2019
 */
public class CombustionEmissions {

	public static enum CombustionProcess implements TextableEnum {
		None("None", "Aucun"),
		Stove_log("Stove with logs", "Po\u00EAle \u00E0 b\u00FBches"),
		Boiler_log("Boiler with logs", "Chaudi\u00E8re \u00E0 b\\u00FBches"),
		Stove_pellet("Stove with pellets", "Po\u00EAle \u00E0 granul\u00E9s"),
		Boiler_pellet("Boiler with pellets", "Chaudi\u00E8re \u00E0 granul\u00E9s"),
		Boiler_chips("Boiler with chips", "Chaudi\u00E8re \u00E0 plaquettes");

		
		CombustionProcess(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);			
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	public static final Map<CombustionProcess, CombustionEmissions> CombustionEmissionsMap = new HashMap<CombustionProcess, CombustionEmissions>();
	static {
		init();
	}
	
	private final double co2EmissionMg_MgDryBiomassFactor;
	private final double ch4EmissionMg_MgDryBiomassFactor;
	private final double coEmissionMg_MgDryBiomassFactor;
	private final double covEmissionMg_MgDryBiomassFactor;
	private final double heatEmissionKWh_MgDryBiomassFactor;
	
	
	protected CombustionEmissions(Object[] record) {
		co2EmissionMg_MgDryBiomassFactor = Double.parseDouble(record[1].toString());
		ch4EmissionMg_MgDryBiomassFactor = Double.parseDouble(record[2].toString());
		coEmissionMg_MgDryBiomassFactor = Double.parseDouble(record[3].toString());
		covEmissionMg_MgDryBiomassFactor = Double.parseDouble(record[4].toString());
		heatEmissionKWh_MgDryBiomassFactor = Double.parseDouble(record[5].toString());
	}

	protected static void init() {
		try {
			String filename = ObjectUtility.getPackagePath(CombustionEmissions.class) + "CombustionFactors.csv";
			
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while((record = reader.nextRecord()) != null) {
				CombustionProcess process = CombustionProcess.valueOf(record[0].toString());
				CombustionEmissionsMap.put(process, new CombustionEmissions(record));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load combustion emission factors!");
		}
	}

	public static void main(String[] args) {
		
	}

}
