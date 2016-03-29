package lerfob.treelogger.mathilde;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.mathilde.MathildeTree.MathildeTreeSpecies;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MathildeTreeLoggerParameters extends TreeLoggerParameters<MathildeTreeLogCategory> {

	public static enum Grade implements TextableEnum {
		EnergyWood("Industry and energy wood", "Bois d'industrie et bois \u00E9nergie (BIBE)"),
		EnergyWoodExplanation("If the dbh is larger than the limit, all the commercial volume that is not eligible as lumber wood is processed as industry and energy wood", 
				"Si le d130 est plus grand que la limite, tout le volume commercial qui n'est pas \u00E9ligible en tant que bois d'oeuvre (BO) est transform\u00E9 en bois d'industrie et bois \u00E9nergie (BIBE)"),
		LumberWood("Lumber wood", "Bois d'oeuvre (BO)"),
		LumberWoodExplanations("If the dbh is larger than the limit, 84% of the commercial volume is considered as lumber wood. The rest is processed as industry and energy wood.",
				"Si le d130 est plus grand que la limite, 84% du volume commercial est transform\u00E9 en bois d'oeuvre (BO). Le reste est trait\u00E9 en bois d'industrie et bois \u00E9nergie (BIBE)")
		;

		Grade(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	private transient MathildeTreeLoggerParametersDialog guiInterface;
	
	protected MathildeTreeLoggerParameters() {
		super(MathildeTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<MathildeTreeLogCategory> categories;
		getLogCategories().clear();
		for (MathildeTreeSpecies species : MathildeTreeSpecies.values()) {
			categories = new ArrayList<MathildeTreeLogCategory>();
			getLogCategories().put(species.name(), categories);
			categories.add(new MathildeTreeLogCategory(species, Grade.LumberWood.name(), 27.5));
			categories.add(new MathildeTreeLogCategory(species, Grade.EnergyWood.name(), 7d));
		}
	}

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public MathildeTreeLoggerParametersDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new MathildeTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}


}
