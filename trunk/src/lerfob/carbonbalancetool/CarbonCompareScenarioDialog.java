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
		Scenario("Alternative scenario", "Sc\u00E9nario alternatif");
		
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

	private final JButton ok;
	private final JButton cancel;
	private final JButton help;
	
	private String selectionCompared;
	
	
	@SuppressWarnings("rawtypes")
	protected CarbonCompareScenarioDialog(CarbonAccountingToolDialog parent, CarbonAccountingToolPanelView panelView) {
		super(parent);
		this.panelView = panelView;
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		help = UIControlManager.createCommonButton(CommonControlID.Help);
		scenarioToCompareComboBox = new JComboBox();
		initUI();
		pack();
		setMinimumSize(getSize());
	}

	
	@SuppressWarnings("unchecked")
	private void setComboBoxValues() {
		scenarioToCompareComboBox.removeAllItems();
		for (int i = 0; i < panelView.tabbedPane.getTabCount(); i++) {
			if (i != panelView.tabbedPane.getSelectedIndex()) {
				CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) panelView.tabbedPane.getComponentAt(i);
				if (panel != null) {
					if (panel.getSummary() instanceof CarbonAssessmentToolSingleSimulationResult) { // to avoid comparing a difference with a scenario
						scenarioToCompareComboBox.addItem(panel);
					}
				}
			}
		}
		checkCorrespondanceWith(scenarioToCompareComboBox, selectionCompared);
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
		
		JPanel mainPanel = new JPanel(new GridLayout(1,1));
		add(mainPanel, BorderLayout.NORTH);
		
		Border etched = BorderFactory.createEtchedBorder();
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(etched, MessageID.Scenario.toString()));
		panel.add(scenarioToCompareComboBox, BorderLayout.CENTER);
		mainPanel.add(panel);
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
		CarbonAccountingToolSingleViewPanel baseline = (CarbonAccountingToolSingleViewPanel) panelView.tabbedPane.getSelectedComponent();
		String simulationName = scenToCompare.toString() + " - " + baseline.toString();

		CarbonAssessmentToolSingleSimulationResult scen = (CarbonAssessmentToolSingleSimulationResult) scenToCompare.getSummary();
		CarbonAssessmentToolSingleSimulationResult base = (CarbonAssessmentToolSingleSimulationResult) baseline.getSummary();
		
		panelView.addSimulationResult(new CarbonAssessmentToolSimulationDifference(simulationName, scen, base));
		selectionCompared = scenToCompare.toString();
		super.okAction();
	}

}
