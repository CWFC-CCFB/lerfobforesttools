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
package lerfob.fagacees;


/**
 * This interface ensures the tree instance can provide its species in the Fagacees model, i.e.
 * either oak or beech.
 * @author Mathieu Fortin - December 2012
 */
public interface FagaceesSpeciesProvider {

    public static enum FgSpecies {
        OAK,
        BEECH;
        
        /**
         * This method returns the name in lower case.
         * @return a String
         */
        public String getName() {
        	return name().toLowerCase();
        }
    }

    /**
     * This method returns the FgSpecies enum that corresponds to the tree or the stand.
     * @return a FgSpecies enum
     */
    public FgSpecies getFgSpecies();
	
	
}
