/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface ensures the ModelBasedSimulator class can emulate fertility
 * classes through plot random effects predictors.
 * @author Mathieu Fortin - December 2015
 */
public interface FertilityClassEmulator {

	public static enum FertilityClass implements TextableEnum {
		Unknown("Unknown", "Inconnu"),
		I("Class I", "Classe I"),
		II("Class II", "Classe II"),
		III("Class III", "Classe III");

		FertilityClass(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	/**
	 * This method allows to tweak the plot random effect in order to reproduce a sort of site index.
	 * @param fertilityClass a FertilityClass enum
	 */
	public void emulateFertilityClass(FertilityClass fertilityClass);

}
