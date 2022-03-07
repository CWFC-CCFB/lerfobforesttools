package lerfob.carbonbalancetool;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import lerfob.carbonbalancetool.CATUtility.ProductionManagerName;
import lerfob.carbonbalancetool.io.CATSpeciesSelectionDialog;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManagerDialog;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaGUITestRobot;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.REpiceaMatchSelectorDialog;
import repicea.gui.genericwindows.REpiceaLicenseWindow;
import repicea.io.tools.ImportFieldManagerDialog;
import repicea.simulation.processsystem.SystemManagerDialog.MessageID;
import repicea.util.ObjectUtility;

public class CarbonAccountingToolGUITest {
	
	static CarbonAccountingTool CAT;
	static REpiceaGUITestRobot ROBOT;
	
	@BeforeClass
	public static void initTest() throws Exception {
		CAT = new CarbonAccountingTool();
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				try {
					CAT.initializeTool();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		ROBOT = new REpiceaGUITestRobot();
		Thread t = ROBOT.startGUI(toRun, REpiceaLicenseWindow.class);
		ROBOT.clickThisButton("acceptLicense");
		ROBOT.clickThisButton(UIControlManager.CommonControlID.Continue.name(), CAT.getUI().getClass());
	}
	
	@AfterClass
	public static void cleanUp() {
		CAT.getUI().dispose();
		ROBOT.shutdown();
	}

	@Test
	public void testSVNExportHappyPath() throws Exception {
		JComboBox b = (JComboBox) ROBOT.findComponentWithThisName(CATFrame.MessageID.HWP_Parameters.name() + "_ComboBox");
		Runnable toRun = new Runnable() {
			public void run() {
				b.getModel().setSelectedItem(CAT.getCarbonCompartmentManager().getCarbonToolSettings().productionManagerMap.get(ProductionManagerName.customized));
			}
		};
		SwingUtilities.invokeLater(toRun);
		ROBOT.letDispatchThreadProcess();
		
		toRun = new Runnable() {
			public void run() {
				try {
					ROBOT.clickThisButton(CATFrame.MessageID.HWP_Parameters.name());
				} catch (InvocationTargetException | InterruptedException e) {}
			}
		};
		ROBOT.startGUI(toRun, ProductionProcessorManagerDialog.class);
		
		ROBOT.clickThisButton(UIControlManager.CommonMenuTitle.File.name()); 
		
		toRun = new Runnable() {
			public void run() {
				try {
					ROBOT.clickThisButton(MessageID.ExportToSVG.name());
				} catch (InvocationTargetException | InterruptedException e) {} 
			}
		};
		ROBOT.startGUI(toRun, JDialog.class);

		String filename = System.getProperty("java.io.tmpdir") + "exportFluxConfiguration.svg";
		File f = new File(filename);
		if (f.exists())
			f.delete();
		ROBOT.fillThisTextField("Filename", filename);
		
		ROBOT.clickThisButton(CommonControlID.Save.name(), REpiceaAWTProperty.SVGFileSaved);
		
		Assert.assertTrue("Testing if svg export file exists", f.exists());

		ROBOT.clickThisButton(UIControlManager.CommonMenuTitle.File.name()); 
		ROBOT.clickThisButton(UIControlManager.CommonControlID.Close.name());
	}
	
	@Test
	public void testImportAndSimulateYieldTableHappyPath() throws Exception {
		ROBOT.clickThisButton(UIControlManager.CommonMenuTitle.File.name());
		ROBOT.clickThisButton(UIControlManager.CommonControlID.Import.name()); 
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				try {
					ROBOT.clickThisButton(CATFrame.MessageID.ImportYieldTable.name()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Thread t = ROBOT.startGUI(toRun, JDialog.class);
		String fileToLoad = ObjectUtility.getPackagePath(getClass()) + File.separator + "io" + File.separator + "ExampleYieldTable.csv";
		ROBOT.fillThisTextField("Filename", fileToLoad);
		ROBOT.clickThisButton("Open", CATSpeciesSelectionDialog.class);		
		ROBOT.clickThisButton("Ok", ImportFieldManagerDialog.class);
		ROBOT.clickThisButton("Ok", CATAWTProperty.StandListProperlySet);
		
		List<CATCompatibleStand> stands = CAT.getCarbonCompartmentManager().getStandList();
		System.out.println("Number of stands " + stands.size());
		Assert.assertEquals("Testing the number of CATCompatibleStand instances imported", 46, stands.size());
		
		int tabCountBefore = CAT.getUI().graphicPanel.tabbedPane.getTabCount();
		ROBOT.clickThisButton(CATFrame.MessageID.CalculateCarbonBalance.name(), CATAWTProperty.CarbonCalculationSuccessful); 
		int tabCountAfter = CAT.getUI().graphicPanel.tabbedPane.getTabCount();
		Assert.assertEquals("Testing if a tab has been craeted in the tabbedPane instance", tabCountBefore + 1, tabCountAfter);
	}
	
	@Test
	public void testImportAndSimulateGrowthSimulationHappyPath() throws Exception {
		ROBOT.clickThisButton(UIControlManager.CommonMenuTitle.File.name());
		ROBOT.clickThisButton(UIControlManager.CommonControlID.Import.name()); 
		
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				try {
					ROBOT.clickThisButton(CATFrame.MessageID.ImportGrowthSimulation.name()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = ROBOT.startGUI(toRun, JDialog.class);
		String fileToLoad = ObjectUtility.getPackagePath(getClass()) + File.separator + "io" + File.separator + "MathildeTreeExport.csv";
		ROBOT.fillThisTextField("Filename", fileToLoad);
		ROBOT.clickThisButton("Open", ImportFieldManagerDialog.class);		
		ROBOT.clickThisButton("Ok", REpiceaMatchSelectorDialog.class);
		ROBOT.clickThisButton("Ok", CATAWTProperty.StandListProperlySet);
		
		List<CATCompatibleStand> stands = CAT.getCarbonCompartmentManager().getStandList();
		Assert.assertEquals("Testing the number of CATCompatibleStand instances imported", 8, stands.size());
		
		int tabCountBefore = CAT.getUI().graphicPanel.tabbedPane.getTabCount();
		ROBOT.clickThisButton(CATFrame.MessageID.CalculateCarbonBalance.name(), CATAWTProperty.CarbonCalculationSuccessful); 
		int tabCountAfter = CAT.getUI().graphicPanel.tabbedPane.getTabCount();
		Assert.assertEquals("Testing if a tab has been craeted in the tabbedPane instance", tabCountBefore + 1, tabCountAfter);
	}
	
	
	
}
