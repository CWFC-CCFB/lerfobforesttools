package lerfob.carbonbalancetool;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.Assert;
import org.junit.Test;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.gui.UIControlManager;
import repicea.io.REpiceaOSVGFileHandlerUI;
import repicea.simulation.processsystem.SystemManagerDialog;
import repicea.simulation.processsystem.SystemManagerDialog.MessageID;

public class CarbonAccountingToolGUITest {

	
	private final static int WAIT_TIME = 500;

	@Test
	public void testSVNExport() throws Exception {
		ProductionProcessorManager man = new ProductionProcessorManager();
		SystemManagerDialog dlg = man.getUI(null);
		Runnable toRun = new Runnable() {
			@Override
			public void run() {
				man.showUI(null);
			}
		};
		Thread t = new Thread(toRun);
		t.start();
		Thread.sleep(WAIT_TIME);
		JMenu menu = null;
		for (int i = 0; i < dlg.getJMenuBar().getMenuCount(); i++) {
			menu = dlg.getJMenuBar().getMenu(i);
			if (UIControlManager.CommonMenuTitle.File.name().equals(menu.getName())) {
				break;
			}
		}
		if (menu == null) {
			throw new Exception("Unable to find File menu in the menu bar!");
		}
		JMenuItem item = null;
		for (int i = 0; i < menu.getItemCount(); i++) {
			item = menu.getItem(i);
			if (item != null) {
				String itemName = item.getName();
				if (MessageID.ExportAsSVG.name().equals(item.getName())) {
					break;
				}
			}
		}
		if (item == null) {
			throw new Exception("Unable to find the ExportToSVG menu item in the File menu!");
		}
		ActionListener[] listeners = item.getListeners(ActionListener.class);
		REpiceaOSVGFileHandlerUI oSVGHandler = null;
		for (ActionListener l : listeners) {
			if (l instanceof REpiceaOSVGFileHandlerUI) {
				oSVGHandler = (REpiceaOSVGFileHandlerUI) l;
				break;
			}
		}
		if (oSVGHandler == null) {
			throw new Exception("Unable to find the REpiceaOSVGFileHandlerUI instance related to the SVG export!");
		}
		String filename = System.getProperty("java.io.tmpdir") + "exportFluxConfiguration.svg";
		File f = new File(filename);
		if (f.exists())
			f.delete();
		oSVGHandler.internalSaveAction(filename);
		Thread.sleep(WAIT_TIME);
		Assert.assertTrue("Testing if svg export file exists", f.exists());
		
		dlg.setVisible(false);
		dlg.dispose();
		t.join();
	}
	
	
	
}
