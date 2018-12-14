/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.thinners.frenchnfithinner2018;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;

/**
 * This package interface is used only for the validation of the thinner in the JUnit tests.
 * @author Mathieu Fortin - July 2018
 */
interface InnerValidationPlot {

	FrenchNFIThinnerSpecies getTargetSpecies();
	
}