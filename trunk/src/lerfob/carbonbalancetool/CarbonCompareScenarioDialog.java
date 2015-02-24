package lerfob.carbonbalancetool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CarbonCompareScenarioDialog extends REpiceaDialog implements ActionListener {

	static {
		UIControlManager.setTitle(CarbonCompareScenarioDialog.class, "Scenario comparison", "Comparaison de sc\u00E9narios");
	}
	
	private static enum MessageID implements TextableEnum {
		Scenario("Scenario to be compared", "Sc\u00E9nario \u00E0 comparer"),
		Baseline("Baseline", "Sc\u00E9nario de r\u00E9f\u00E9rence");

		
		MessageID(String englishText, String frenchText) {
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
	
	
	
	private final CarbonAccountingToolPanelView panelView;
	
	@SuppressWarnings("rawtypes")
	private final JComboBox scenarioToCompareComboBox;
	@SuppressWarnings("rawtypes")
	private final JComboBox baselineComboBox;

	private final JButton ok;
	private final JButton cancel;
	private final JButton help;
	
	private String selectionCompared;
	private String selectionBaseline;
	
	
	@SuppressWarnings("rawtypes")
	protected CarbonCompareScenarioDialog(CarbonAccountingToolDialog parent, CarbonAccountingToolPanelView panelView) {
		super(parent);
		this.panelView = panelView;
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		help = UIControlManager.createCommonButton(CommonControlID.Help);
		scenarioToCompareComboBox = new JComboBox();
		baselineComboBox = new JComboBox();
		initUI();
		pack();
		setMinimumSize(getSize());
	}

	
	@SuppressWarnings("unchecked")
	private void setComboBoxValues() {
		scenarioToCompareComboBox.removeAllItems();
		baselineComboBox.removeAllItems();
		for (int i = 0; i < panelView.tabbedPane.getTabCount(); i++) {
			CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) panelView.tabbedPane.getComponentAt(i);
			if (panel != null) {
				if (panel.getSummary() instanceof CarbonAssessmentToolSingleSimulationResult) { // to avoid comparing a difference with a scenario
					scenarioToCompareComboBox.addItem(panel);
					baselineComboBox.addItem(panel);
				}
			}
		}
		checkCorrespondanceWith(scenarioToCompareComboBox, selectionCompared);
		checkCorrespondanceWith(baselineComboBox, selectionBaseline);
	}



	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
		help.removeActionListener(this);
	}

	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			setComboBoxValues();
		}
		super.setVisible(bool);
	}
	
	@SuppressWarnings("rawtypes")
	private void checkCorrespondanceWith(JComboBox comboBox, String formerSelection) {
		int selectedIndex = 0;
		if (formerSelection != null) {
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) comboBox.getItemAt(i);
				if (panel.toString().equals(formerSelection)) {
					selectedIndex = i;
					break;
				}
			}
		}
		comboBox.setSelectedIndex(selectedIndex);
	}


	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		setLayout(new BorderLayout());
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(ok);
		controlPanel.add(cancel);
		controlPanel.add(help);
		add(controlPanel, BorderLayout.SOUTH);
		
		JPanel mainPanel = new JPanel(new GridLayout(2,1));
		add(mainPanel, BorderLayout.NORTH);
		
		
		Border etched = BorderFactory.createEtchedBorder();
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(etched, MessageID.Scenario.toString()));
		panel.add(scenarioToCompareComboBox, BorderLayout.CENTER);
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(UIControlManager.getLabel(MessageID.Scenario));
//		panel.add(Box.createGlue());
		mainPanel.add(panel);
		
//		JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		subPanel.add(Box.createHorizontalStrut(5));
//		subPanel.add(scenarioToCompareComboBox);
//		subPanel.add(Box.createGlue());
//		mainPanel.add(subPanel);
		
		panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(etched, MessageID.Baseline.toString()));
		panel.add(baselineComboBox, BorderLayout.CENTER);
//		panel.add(Box.createHorizontalStrut(5));
//		panel.add(UIControlManager.getLabel(MessageID.Baseline));
//		panel.add(Box.createGlue());
		mainPanel.add(panel);
		
//		subPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		subPanel.add(Box.createHorizontalStrut(5));
//		subPanel.add(baselineComboBox);
//		panel.add(subPanel);
//		subPanel.add(Box.createGlue());
//		mainPanel.add(subPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(cancel)) {
			cancelAction();
		} else if (e.getSource().equals(ok)) {
			okAction();
		} else if (e.getSource().equals(help)) {
			helpAction();
		}
		
	}


	@Override
	public void okAction() {
		CarbonAccountingToolSingleViewPanel scenToCompare = ((CarbonAccountingToolSingleViewPanel) scenarioToCompareComboBox.getSelectedItem());
		CarbonAccountingToolSingleViewPanel baseline = ((CarbonAccountingToolSingleViewPanel) baselineComboBox.getSelectedItem());
		String simulationName = scenToCompare.toString() + " - " + baseline.toString();

		CarbonAssessmentToolSingleSimulationResult scen = (CarbonAssessmentToolSingleSimulationResult) scenToCompare.getSummary();
		CarbonAssessmentToolSingleSimulationResult base = (CarbonAssessmentToolSingleSimulationResult) baseline.getSummary();
		
		panelView.addSimulationResult(new CarbonAssessmentToolSimulationDifference(scen, base), simulationName);
		selectionCompared = scenToCompare.toString();
		selectionBaseline = baseline.toString();
		super.okAction();
	}

}
