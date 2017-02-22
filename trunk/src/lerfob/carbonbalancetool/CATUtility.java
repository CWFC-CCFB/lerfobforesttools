/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.Vector;

import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CATUtility {

	protected static interface ParameterWrapper<P> {
		public Enum<?> getName();
		public P getWrappedInstance();
	}
	
	
	
	protected static class ProductionProcessorManagerWrapper implements REpiceaShowableUIWithParent, ParameterWrapper<ProductionProcessorManager> {
		
		protected final ProductionProcessorManager manager;
		protected final ProductionManagerName name;
		
		protected ProductionProcessorManagerWrapper(ProductionManagerName name, ProductionProcessorManager manager) {
			this.name = name;
			this.manager = manager;
		}

		/*
		 * Useless here. 
		 */
		@Override
		public Component getUI(Container parent) {return null;}

		@Override
		public void showUI(Window parent) {
			manager.showUI(parent);
		}

		@Override
		public String toString() {
			return name.toString();
		}

		@Override
		public Enum<?> getName() {
			return name;
		}

		@Override
		public boolean isVisible() {
			return manager.isVisible();
		}

		@Override
		public ProductionProcessorManager getWrappedInstance() {return manager;}
	}

	static class BiomassParametersWrapper implements REpiceaShowableUIWithParent, ParameterWrapper<BiomassParameters> {
		
		protected final BiomassParameters manager;
		protected final BiomassParametersName name;
		
		protected BiomassParametersWrapper(BiomassParametersName name, BiomassParameters manager) {
			this.name = name;
			this.manager = manager;
		}

		@Override
		public boolean isVisible() {
			return manager.isVisible();
		}
		
		/*
		 * Useless here. 
		 */
		@Override
		public Component getUI(Container parent) {return null;}

		@Override
		public void showUI(Window parent) {
			manager.showUI(parent);
		}

		@Override
		public String toString() {
			return name.toString();
		}

		@Override
		public Enum<?> getName() {
			return name;
		}

		@Override
		public BiomassParameters getWrappedInstance() {return manager;}
	}

	public static enum ProductionManagerName implements TextableEnum {

		hardwood_simple("Hardwood - Simple", "Feuillus - Simple"),
		hardwood_recycling("Hardwood - Recycling", "Feuillus - Recyclage"),
		customized("Costumized", "Sur mesure");

		ProductionManagerName(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	
	public static enum BiomassParametersName implements TextableEnum {

		ipcc_2003_oakbeechsprucefir("IPCC - Oak, Beech, Spruce, Fir", "GIEC - Ch\u00EAne, H\u00EAtre, Epic\u00E9a, Sapin"),
		citepa_france("CITEPA - France", "CITEPA - France"),
		customized("Costumized", "Sur mesure");

		BiomassParametersName(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	
	@SuppressWarnings("serial")
	protected static class CarbonToolSettingsVector<E extends ParameterWrapper<?>> extends Vector<E> {

		protected Object getFirstInstanceWithThisName(Enum<?> enumVar) {
			for (ParameterWrapper<?> wrapper : this) {
				if (wrapper.getName().equals(enumVar)) {
					return wrapper.getWrappedInstance();
				}
			}
			return null;
		}
		
		
	}

}
