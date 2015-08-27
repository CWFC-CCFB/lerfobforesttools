package lerfob.carbonbalancetool.productionlines;

import java.awt.Container;
import java.awt.Window;

import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.ProcessorInternalDialog;
import repicea.simulation.processsystem.SystemPanel;

@SuppressWarnings("serial")
public class AbstractProcessorButton extends ProcessorButton {

	/**
	 * Constructor.
	 * @param panel a SystemPanel instance
	 * @param process the Processor instance that owns this button
	 */
	protected AbstractProcessorButton(SystemPanel panel, Processor process) {
		super(panel, process);
	}
	
	/**
	 * Constructor.
	 * @param panel a SystemPanel instance
	 * @param process the Processor instance that owns this button
	 * @param permissions the REpiceaGUIPermission instance that enables or disables some controls
	 */
	protected AbstractProcessorButton(SystemPanel panel, Processor process, REpiceaGUIPermission permissions) {
		super(panel, process, permissions);
	}

	@Override
	public ProcessorInternalDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new EnhancedProcessorInternalDialog((Window) parent, this);
		}
		return guiInterface;
	}

}
