package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategoryPanel;

@SuppressWarnings("serial")
public class CATDiameterBasedTreeLogCategoryPanel extends DiameterBasedTreeLogCategoryPanel {

	protected CATDiameterBasedTreeLogCategoryPanel(CATDiameterBasedTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setEditable(false);	// override here the log names cannot be changed
	}

}
