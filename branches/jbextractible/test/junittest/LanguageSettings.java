package junittest;

import org.junit.Test;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;


public class LanguageSettings {

	@Test
	public void settingLanguageTest() {
		REpiceaTranslator.setCurrentLanguage(Language.English);
	}

}
