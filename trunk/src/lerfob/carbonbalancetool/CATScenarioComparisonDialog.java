package lerfob.carbonbalancetool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
public class CATScenarioComparisonDialog extends REpiceaDialog implements ActionListener, ItemListener {

	static {
		UIControlManager.setTitle(CATScenarioComparisonDialog.class, "Scenario comparison", "Comparaison de sc\u00E9narios");
	}
	
	private static enum MessageID implements TextableEnum {
		Baseline("Baseline", "Sc\u00E9nario de r\u00E9f\u00E9rence"),
		AlternativeScenario("Alternative scenario", "Sc\u00E9nario alternatif"),
		Scenario("Scenario", "Sc\u00E9nario"),
		ComparisonMode("Comparison mode", "Mode de comparaison"),
		Date("Date", "Date");
		
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
	
	private static enum ComparisonMode implements TextableEnum {
		PointEstimate("Between two point estimates", "Entre deux estimations finies"),
		InfiniteSequence("Infinite sequence", "En s\u00E9quence infinie");

		ComparisonMode(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	private final CATPanelView panelView;
	
	private final JComboBox<CATSingleViewPanel> alternativeScenarioComboBox;
	private final JComboBox<CATSingleViewPanel> baselineComboBox;
	private final JComboBox<ComparisonMode> comparisonModeComboBox;
	private final JComboBox<Integer> dateBaselineScenarioComboBox;
	private final JComboBox<Integer> dateAlternativeScenarioComboBox;
	
	private final JButton ok;
	private final JButton cancel;
	private final JButton help;
	
	private String selectionCompared;
	
	private final CATSingleViewPanel baselinePanel;
	
	
	protected CATScenarioComparisonDialog(CATFrame parent, CATPanelView panelView) {
		super(parent);
		this.panelView = panelView;
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		help = UIControlManager.createCommonButton(CommonControlID.Help);
		comparisonModeComboBox = new JComboBox<ComparisonMode>();
		alternativeScenarioComboBox = new JComboBox<CATSingleViewPanel>();
		dateBaselineScenarioComboBox = new JComboBox<Integer>();
		dateAlternativeScenarioComboBox = new JComboBox<Integer>();
		baselinePanel = (CATSingleViewPanel) panelView.tabbedPane.getSelectedComponent();
		baselineComboBox = new JComboBox<CATSingleViewPanel>();
		baselineComboBox.addItem(baselinePanel);
		baselineComboBox.setEnabled(false);
		
		initUI();
		pack();
		setMinimumSize(getSize());
		setVisible(true);
	}


	private void setComparisonModeComboBox() {
		comparisonModeComboBox.removeAllItems();
		comparisonModeComboBox.addItem(ComparisonMode.PointEstimate);
		CATSingleViewPanel panel = (CATSingleViewPanel) panelView.tabbedPane.getSelectedComponent();
		if (panel.getSummary().isEvenAged()) {
			comparisonModeComboBox.addItem(ComparisonMode.InfiniteSequence);
		}
		comparisonModeComboBox.setSelectedIndex(0);
	}
	
	
	
	private void setDateComboBoxForBaseline() {
		dateBaselineScenarioComboBox.removeAllItems();
		if (!isComparisonModeInfiniteSequence()) {
			for (Integer date : baselinePanel.getSummary().getTimeTable().getListOfDatesUntilLastStandDate()) {
				dateBaselineScenarioComboBox.addItem(date);
			}
		}
	}
	
	private void setDateComboBoxForAlternativeScenario() {
		dateAlternativeScenarioComboBox.removeAllItems();
		if (!isComparisonModeInfiniteSequence()) {
			CATSingleViewPanel altPanel = (CATSingleViewPanel) alternativeScenarioComboBox.getSelectedItem();
			for (Integer date : altPanel.getSummary().getTimeTable().getListOfDatesUntilLastStandDate()) {
				dateAlternativeScenarioComboBox.addItem(date);
			}
		}
	}
	
	private void setComboBoxValues() {
		alternativeScenarioComboBox.removeAllItems();
		for (int i = 0; i < panelView.tabbedPane.getTabCount(); i++) {
			CATSingleViewPanel panel = (CATSingleViewPanel) panelView.tabbedPane.getComponentAt(i);
			if (panel != null) {
				if (panel.getSummary() instanceof CATSingleSimulationResult) { // to avoid comparing a difference with a scenario
					if (isComparisonModeInfiniteSequence()) {
						if (panel.getSummary().isEvenAged()) {
							alternativeScenarioComboBox.addItem(panel);
						}
					} else {
						alternativeScenarioComboBox.addItem(panel);
					}
				}
			}
		}
		checkCorrespondanceWith(alternativeScenarioComboBox, selectionCompared);
	}

	private boolean isComparisonModeInfiniteSequence() {
		return comparisonModeComboBox.getSelectedItem().equals(ComparisonMode.InfiniteSequence);
	}

	@Override
	public void listenTo() {
		ok.addActionListener(this);
		cancel.addActionListener(this);
		help.addActionListener(this);
		comparisonModeComboBox.addItemListener(this);
		alternativeScenarioComboBox.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
		help.removeActionListener(this);
		comparisonModeComboBox.removeItemListener(this);
		alternativeScenarioComboBox.removeItemListener(this);
	}

	
	@Override
	public void refreshInterface() {
		setComparisonModeComboBox();
		setComboBoxValues();
		setDateComboBoxForBaseline();
		setDateComboBoxForAlternativeScenario();
		super.refreshInterface();
	}
 	
	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			refreshInterface();
		}
		super.setVisible(bool);
	}
	
	@SuppressWarnings("rawtypes")
	private void checkCorrespondanceWith(JComboBox comboBox, String formerSelection) {
		int selectedIndex = 0;
		if (formerSelection != null) {
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				CATSingleViewPanel panel = (CATSingleViewPanel) comboBox.getItemAt(i);
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

		JPanel compPane = new JPanel();
		compPane.setLayout(new BoxLayout(compPane, BoxLayout.Y_AXIS));
		JPanel comparisonModePanel = UIControlManager.createSimpleHorizontalPanel(MessageID.ComparisonMode, comparisonModeComboBox, 5, true);
		compPane.add(Box.createVerticalStrut(5));
		compPane.add(comparisonModePanel);
		compPane.add(Box.createVerticalStrut(5));
		add(compPane, BorderLayout.NORTH);	
		
		JPanel mainPanel = new JPanel(new GridLayout(1,2));
		add(mainPanel, BorderLayout.CENTER);

		GridLayout gridLayout = new GridLayout(2,1);
		
		Border etched = BorderFactory.createEtchedBorder();
		JPanel baselinePane = new JPanel(gridLayout);
		baselinePane.setBorder(BorderFactory.createTitledBorder(etched, MessageID.Baseline.toString()));
		JPanel pane = UIControlManager.createSimpleHorizontalPanel(MessageID.Scenario, baselineComboBox, 5, false);
		baselinePane.add(pane);
		pane = UIControlManager.createSimpleHorizontalPanel(MessageID.Date,	dateBaselineScenarioComboBox, 5, false);
		baselinePane.add(pane);
		mainPanel.add(baselinePane);
		
		JPanel comparisonPane = new JPanel(gridLayout);
		comparisonPane.setBorder(BorderFactory.createTitledBorder(etched, MessageID.AlternativeScenario.toString()));
		pane = UIControlManager.createSimpleHorizontalPanel(MessageID.Scenario,	alternativeScenarioComboBox, 5, false);
		comparisonPane.add(pane);
		pane = UIControlManager.createSimpleHorizontalPanel(MessageID.Date, dateAlternativeScenarioComboBox, 5, false);
		comparisonPane.add(pane);
		
		mainPanel.add(comparisonPane);
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
		CATSingleViewPanel scenToCompare = ((CATSingleViewPanel) alternativeScenarioComboBox.getSelectedItem());
		String simulationName = scenToCompare.toString() + " - " + baselinePanel.toString();

		CATSingleSimulationResult base = (CATSingleSimulationResult) baselinePanel.getSummary();
		Integer baseDate = (Integer) dateBaselineScenarioComboBox.getSelectedItem();

		CATSingleSimulationResult altScen = (CATSingleSimulationResult) scenToCompare.getSummary();
		Integer altScenDate = (Integer) dateAlternativeScenarioComboBox.getSelectedItem();
				
		panelView.addSimulationResult(new CATSimulationDifference(simulationName, base, baseDate, altScen, altScenDate));
		selectionCompared = scenToCompare.toString();
		super.okAction();
	}


	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(alternativeScenarioComboBox)) {
			System.out.println("scenarioToCompareComboBox changed");
			setDateComboBoxForAlternativeScenario();
		} else if (arg0.getSource().equals(comparisonModeComboBox)) {
			doNotListenToAnymore();
			System.out.println("comparisonModeComboBox changed");
			setComboBoxValues();
			setDateComboBoxForBaseline();
			setDateComboBoxForAlternativeScenario();
			listenTo();
		}
	}

}
