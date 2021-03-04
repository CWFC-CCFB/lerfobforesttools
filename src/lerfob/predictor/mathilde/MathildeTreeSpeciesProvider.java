package lerfob.predictor.mathilde;

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public abstract interface MathildeTreeSpeciesProvider {

	/**
	 * This enum variable defines the different species in the Mathilde simulator.
	 * @author Mathieu Fortin - June 2013
	 */
	public static enum MathildeTreeSpecies implements TextableEnum {
		CARPINUS("Hornbeam", "Charme"),   // code 17
		QUERCUS("Oak", "Ch\u00EAne"),  // code 22
		FAGUS("Beech", "H\u00EAtre"), // code 42
		OTHERS("Others", "Autres");
		
		private static Map<Integer, MathildeTreeSpecies> speciesMap;

		private Matrix shortDummyVariable;
		private Matrix longDummyVariable;
	
		/**
		 * This method returns the species enum according to the code.
		 * @param code an integer (17 for Carpinus, 22 for Quercus, 42 for Fagus, and 99 for others)
		 * @return a MathildeTreeSpecies enum
		 */
		public static MathildeTreeSpecies getSpecies(int code) {
			if (speciesMap == null) {
				speciesMap = new HashMap<Integer, MathildeTreeSpecies>();
				speciesMap.put(17, CARPINUS);
				speciesMap.put(22, QUERCUS);
				speciesMap.put(42, FAGUS);
				speciesMap.put(99, OTHERS);
			}
			return speciesMap.get(code);
		}

		MathildeTreeSpecies(String englishText, String frenchText) {
			setText(englishText, frenchText);
			shortDummyVariable = new Matrix(1,3);
			if (ordinal() > 0) {
				shortDummyVariable.setValueAt(0, ordinal() - 1, 1d);
			}
			longDummyVariable = new Matrix(1,4);
			longDummyVariable.setValueAt(0, ordinal(), 1d);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

		/**
		 * This method returns a 1x3 row vector of dummy variables which represent the species effect.
		 * @return a Matrix instance
		 */
		public Matrix getShortDummyVariable() {return shortDummyVariable;}
		
		/**
		 * This method returns a 1x4 row vector of dummy variables which represent the species effect.
		 * @return a Matrix instance
		 */
		public Matrix getLongDummyVariable() {return longDummyVariable;}
		
	}

	
	/**
	 * This method returns the enum instance that represents the species.
	 * @return a MathildeTreeSpecies instance
	 */
	public MathildeTreeSpecies getMathildeTreeSpecies();

}
