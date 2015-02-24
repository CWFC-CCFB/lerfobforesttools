package lerfob.carbonbalancetool;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.Vector;

import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.gui.ShowableObjectWithParent;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CarbonAccountingToolUtility {

	protected static interface ParameterWrapper<P> {
		public Enum<?> getName();
		public P getWrappedInstance();
	}
	
	
	
	protected static class ProductionProcessorManagerWrapper implements ShowableObjectWithParent, ParameterWrapper<ProductionProcessorManager> {
		
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
		public Component getGuiInterface(Container parent) {return null;}

		@Override
		public void showInterface(Window parent) {
			manager.showInterface(parent);
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
		public ProductionProcessorManager getWrappedInstance() {return manager;}
	}

	static class BiomassParametersWrapper implements ShowableObjectWithParent, ParameterWrapper<BiomassParameters> {
		
		protected final BiomassParameters manager;
		protected final BiomassParametersName name;
		
		protected BiomassParametersWrapper(BiomassParametersName name, BiomassParameters manager) {
			this.name = name;
			this.manager = manager;
		}

		/*
		 * Useless here. 
		 */
		@Override
		public Component getGuiInterface(Container parent) {return null;}

		@Override
		public void showInterface(Window parent) {
			manager.showInterface(parent);
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

	protected static enum ProductionManagerName implements TextableEnum {

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

	
	protected static enum BiomassParametersName implements TextableEnum {

		ipcc_2003_oakbeechsprucefir("IPCC - Oak, Beech, Spruce, Fir", "GIEC - Ch\u00E8ne, H\u00E8tre, Epic\u00E9a, Sapin"),
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
