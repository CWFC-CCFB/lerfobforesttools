package lerfob.carbonbalancetool.productionlines;

import java.awt.Container;

import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


@SuppressWarnings("serial")
public class WoodyDebrisProcessor extends LeftHandSideProcessor {

	
	protected static enum WoodyDebrisProcessorID implements TextableEnum {
		CoarseWoodyDebris("Coarse Woody Debris", "Gros d\u00E9bris ligneux"),
		FineWoodyDebris("Fine Woody Debris", "Petit d\u00E9bris ligneux");

		
		WoodyDebrisProcessorID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	
	/**
	 * The WoodyDebrisProcessorButton class is the GUI implementation for 
	 * WoodyDebrisProcessor. It has a specific icon for better identification in the GUI.
	 * @author Mathieu Fortin - May 2014
	 */
	public static class WoodyDebrisProcessorButton extends LeftHandSideProcessorButton {

		/**
		 * Constructor.
		 * @param panel	a SystemPanel instance
		 * @param processor the WoodyDebrisProcessor that owns this button
		 */
		protected WoodyDebrisProcessorButton(SystemPanel panel, WoodyDebrisProcessor processor) {
			super(panel, processor);
		}

	}

	
	protected WoodyDebrisProcessorID wdpID;
	
	protected WoodyDebrisProcessor(WoodyDebrisProcessorID wdpID) {
		setName(wdpID.toString());
		this.wdpID = wdpID;
	}
	
	@Override
	public ProcessorButton getGuiInterface(Container container) {
		if (guiInterface == null) {
			guiInterface = new WoodyDebrisProcessorButton((SystemPanel) container, this);
		}
		return guiInterface;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WoodyDebrisProcessor) {
			WoodyDebrisProcessor wdp = (WoodyDebrisProcessor) obj;
			if (wdp.wdpID.equals(wdpID)) {
				return true;
			}
		}
		return false;
	}
	
}
