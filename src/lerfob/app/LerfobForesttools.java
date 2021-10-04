/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.app;

import java.util.Arrays;
import java.util.List;

import lerfob.carbonbalancetool.CarbonAccountingTool;
import lerfob.carbonbalancetool.pythonaccess.PythonAccessPoint;
import repicea.app.REpiceaAppVersion;
import repicea.lang.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

/**
 * This class is the main entry point for the application.
 * @author Mathieu Fortin - December 2018
 *
 */
public class LerfobForesttools {

	private static final String APPLICATION = "-app";

	public static void main(String[] args) throws Exception {
		System.out.println("Running on repicea " + REpiceaAppVersion.getInstance().getRevision());
		System.out.println("Running on lerfobforesttools " + LerfobForesttoolsAppVersion.getInstance().getRevision());
//		String repiceaRevision = "repicea; " + REpiceaAppVersion.getInstance().getRevision();
//		System.out.println(repiceaRevision);
//		String lerfobRevision = "lerfobforesttools; " + LerfobForesttoolsAppVersion.getInstance().getRevision();
//		System.out.println(lerfobRevision);
		String inputString = "";
		for (String str : args) {
			inputString = inputString + str + "; ";
		}
		System.out.println("Parameters received:" + inputString);
		REpiceaSystem.setLanguageFromMain(args, Language.English);
		System.out.println("Language set to: " + REpiceaTranslator.getCurrentLanguage().name());

		List<String> argumentList = Arrays.asList(args);

		if (argumentList.contains(APPLICATION) && (argumentList.indexOf(APPLICATION) + 1 < argumentList.size())) {
			String application = argumentList.get(argumentList.indexOf(APPLICATION) + 1);
			if (application.toLowerCase().equals("python")) {
				PythonAccessPoint.main(args);
			} 
		} else {
			CarbonAccountingTool.main(args);
		}
	}
}
