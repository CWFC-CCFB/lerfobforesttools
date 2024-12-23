/*
 * This file is part of the lerfob-foresttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package lerfob.treelogger.basictreelogger;

import java.io.IOException;
import java.util.List;

import javax.swing.JButton;

import org.junit.Assert;
import org.junit.Test;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaGUITestRobot;
import repicea.gui.components.REpiceaSlider;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

public class BasicTreeLoggerTest {

	
	/*
	 * Log categories could be saved in French and then we read in English, the categories would no longer be recognized.
	 * This resulted in null pointer exception being thrown.
	 */
	@Test
	public void testLanguageCompatibilityAllInFrench() throws Exception {
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.out.println("Test will be skipped because Mac does not support Times font.");
			return;
		}
		Language languageAtBeginning = REpiceaTranslator.getCurrentLanguage();
		REpiceaTranslator.setCurrentLanguage(Language.French); // make sure we read it in English because those in the file .prl are in 
		String filename = ObjectUtility.getPackagePath(getClass()) + "basicTreeLoggerParmsInFrench.tlp";
		BasicTreeLoggerParameters parms = new BasicTreeLoggerParameters();
		parms.load(filename);
		BasicTreeLoggerParametersDialog dlg = parms.getUI(null);
		Runnable doRun = new Runnable() {
			@Override
			public void run() {
				parms.showUI(null);
			}
		};
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		robot.startGUI(doRun, BasicTreeLoggerParametersDialog.class);
		
		List<REpiceaSlider> sliders = CommonGuiUtility.mapComponents(dlg, REpiceaSlider.class);
		List<JButton> buttons = CommonGuiUtility.mapComponents(dlg, JButton.class);
		REpiceaSlider slider1 = sliders.get(0);
		REpiceaSlider slider2 = sliders.get(1);
		slider1.setValue(52);
		
		robot.letDispatchThreadProcess();

		Assert.assertEquals("Testing if other slider is synchronized", 48, slider2.getValue());
		BasicLogCategory shortLived = parms.findLogCategoryRegardlessOfTheLanguage(BasicTreeLoggerParameters.MessageID.ShortLived);
		Assert.assertEquals("Testing if volume proportion has been recorded in short-lived category", 0.52, shortLived.getVolumeProportion(), 1E-8);
		BasicLogCategory longLived = parms.findLogCategoryRegardlessOfTheLanguage(BasicTreeLoggerParameters.MessageID.LongLived);
		Assert.assertEquals("Testing if volume proportion has been recorded in long-lived category", 0.48, longLived.getVolumeProportion(), 1E-8);
		
		robot.clickThisButton("Cancel");
		
		robot.letDispatchThreadProcess();
		
		robot.shutdown();
		
		REpiceaTranslator.setCurrentLanguage(languageAtBeginning);
	}

	/*
	 * Log categories could be saved in French and then we read in English, the categories would no longer be recognized.
	 * This resulted in null pointer exception being thrown.
	 */
	@Test
	public void testLanguageCompatibilityWhenFileRecordedInFrenchButReadInEnglish() throws Exception {
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.out.println("Test will be skipped because Mac does not support Times font.");
			return;
		}
		Language languageAtBeginning = REpiceaTranslator.getCurrentLanguage();
		REpiceaTranslator.setCurrentLanguage(Language.English); // make sure we read it in English because those in the file .prl are in 
		String filename = ObjectUtility.getPackagePath(getClass()) + "basicTreeLoggerParmsInFrench.tlp";
		BasicTreeLoggerParameters parms = new BasicTreeLoggerParameters();
		parms.load(filename);
		
		REpiceaGUITestRobot robot = new REpiceaGUITestRobot();
		
		BasicTreeLoggerParametersDialog dlg = parms.getUI(null);
		
		Runnable doRun = new Runnable() {
			@Override
			public void run() {
				parms.showUI(null);
			}
		};
		
		robot.startGUI(doRun, BasicTreeLoggerParametersDialog.class);
		
		List<REpiceaSlider> sliders = CommonGuiUtility.mapComponents(dlg, REpiceaSlider.class);
		List<JButton> buttons = CommonGuiUtility.mapComponents(dlg, JButton.class);
		REpiceaSlider slider1 = sliders.get(0);
		REpiceaSlider slider2 = sliders.get(1);
		slider1.setValue(52);
		robot.letDispatchThreadProcess();
		
		Assert.assertEquals("Testing if other slider is synchronized", 48, slider2.getValue());
		
		BasicLogCategory shortLived = parms.findLogCategoryRegardlessOfTheLanguage(BasicTreeLoggerParameters.MessageID.ShortLived);
		BasicLogCategory longLived = parms.findLogCategoryRegardlessOfTheLanguage(BasicTreeLoggerParameters.MessageID.LongLived);
//		System.out.println("ShortLived volume proportion = " + shortLived.getVolumeProportion());
//		System.out.println("LongLived volume proportion = " + longLived.getVolumeProportion());
		Assert.assertEquals("Testing if volume proportion has been recorded in short-lived category", 0.52, shortLived.getVolumeProportion(), 1E-8);
		Assert.assertEquals("Testing if volume proportion has been recorded in long-lived category", 0.48, longLived.getVolumeProportion(), 1E-8);
		
		robot.clickThisButton("Cancel");
		robot.letDispatchThreadProcess();
		robot.shutdown();
		REpiceaTranslator.setCurrentLanguage(languageAtBeginning);
	}
	

}
