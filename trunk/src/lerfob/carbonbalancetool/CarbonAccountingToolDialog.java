package lerfob.carbonbalancetool;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import lerfob.carbonbalancetool.CarbonAccountingToolTask.Task;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.BiomassParametersWrapper;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.ProductionProcessorManagerWrapper;
import repicea.gui.AutomatedHelper;
import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.REpiceaFrame;
import repicea.gui.Refreshable;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.components.REpiceaComboBoxOpenButton;
import repicea.gui.dnd.AcceptableDropComponent;
import repicea.gui.dnd.DropTargetImpl;
import repicea.net.BrowserCaller;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CarbonAccountingToolDialog is the UI interface of the 
 * CarbonAccountingTool class. 
 * @author M. Fortin - June 2010
 */
@SuppressWarnings("serial")
public class CarbonAccountingToolDialog extends REpiceaFrame implements PropertyChangeListener, ItemListener,
																		Refreshable, 
																		AcceptableDropComponent<ArrayList<CarbonToolCompatibleStand>> {


	static {
		UIControlManager.setTitle(CarbonAccountingToolDialog.class, LERFoBCarbonAccountingTool.englishTitle, LERFoBCarbonAccountingTool.frenchTitle);
		
		try {
			Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"/capsis/extension/modeltool/carbonaccountingtool";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(CarbonAccountingToolDialog.class, helper);
		} catch (Exception e) {}
	}

	public static enum MessageID implements TextableEnum {
		EstimateCarbonBiomass("Carbon estimation in biomass", "Estimation du carbone de la biomasse"),
		HarvestedVolumeLabel("Harvested volume management", "Gestion des volumes r\u00E9colt\u00E9s"),
		ProductionLinesLabel("Production lines", "Lignes de production"),
		WoodDispatcherLabel("Production line supply", "Approvisionnement des lignes de production"),
		Default("<Default>","<D\u00E9faut>"),
		CalculateCarbonBalance("Calculate carbon balance", "Calculer bilan carbone"),
		ShowSomethingOptions("Show compartment(s)", "Afficher le(s) compartiment(s)"),
		Component("Component", "Composante"),
		DefaultParameters("Default parameters", "Param\u00E8tres par d\u00e9faut"),
		Name("Name", "Nom"),
		CompareScenario("Compare scenarios", "Comparaison de sc\u00E9narios"),
		Status("Status", "Etat"),
		HWP_Parameters("Harvested Wood products", "Produits bois"),
		Biomass("Biomass parameters", "Param\u00E8tres biomasse"),
		ManageWoodSupply("Manage wood supply", "G\u00E9rer l'approvisionnement"),
		WaitingTask("Waiting", "En attente d'une t\u00E2che"),
		Compartments("Compartments", "Compartiments"),
		Settings("Parameters", "Param\u00E8tres"),
		AvailabbleTreeLoggers("Select a tree logger", "Choisir un module de billonnage"),
		JobDone("Done", "Termin\u00E9"),
		ErrorWhileComputing("An error occured while performing the task: ", 
				"Une erreur est survenue pendant le d\u00E9roulement de la t\u00E2che : "),
		IPCCBiomassParameters("IPCC parameters", "Param\u00E8tres du GIEC"),
		CITEPABiomassParameters("French reporting parameters", "Param\u00E8tres du CITEPA"),
		ModelBasedBiomassParameters("Model-based parameters","Param\u00E8tres du mod\u00E8le"),
		LoggingJob("Logging trees...", "R\u00E9colte des arbres..."),
		WoodPieceJob("Bucking trees...", "Billonnage des arbres..."),
		CarbonCompartmentJob("Calculating carbon...", "Calcul du carbone..."),
		ActualizingCarbon("Actualizing carbon units...", "Actualisation des unit\u00E9s de carbone..."),
		Bucking("Sawing", "Billonnage"),
		Edit("Edit", "Editer"),
		YouAboutToExport("Please select the simulation you want to export: ",
				"Veuillez choisir la simulation que vous d\u00E9sirez exporter ? "),
		ParametersAreInvalid("The HWP parameters are invalid. Please carefully check!",
				"Les param\u00E8tres des produits bois sont incorrects. Veuillez les v\u00E9rifier!"),
		ReadyToCompute("Ready to perform the assessment.",
				"Pr\u00EAt \u00E0 r\u00E9aliser l'\u00E9valuation."),
		Forest("Forest", "For\u00EAt"),
		SpeciesGroup("Species group", "Group d'esp\u00E8ces"),
		BroadLeaved("Broadleaved species", "Esp\u00E8ces de feuillus"),
		Evergreen("Coniferous species", "Esp\u00E8ces de conif\u00E8res"),
		AboveGroundExpansionFactor("Aboveground biomass expansion factor", "Facteur d'expansion biomasse a\u00E9rienne"),
		BelowGroundExpansionFactor("Belowground biomass expansion factor", "Facteur d'expansion biomasse souterraine"),
		BasicDensityFactors("Basic density (ton dry matter/m3 of greenwood)", "Infradensit\u00E9 (tonne mati\u00E8re anhydre/m3 bois vert)"),
				
		WoodProducts("Wood products", "Produits bois"),
		ProductType("Product type", "Type de produit"),
		ShortLived("Short lived", "Courte dur\u00E9e de vie"),
		LongLived("Long lived", "Longue dur\u00E9e de vie"),
		Proportion("Splitting", "R\u00E9partition"),
		AverageLifeTime("Average lifetime (yrs)", "Dur\u00E9e de vie moyenne (ann\u00E9es)"),
		BurnedForEnergy("Burn wood products for energy when disposed", "Incin\u00E9ration \u00E0 des fins \u00E9nerg\u00E9tiques"),

		Landfill("Landfill site", "D\u00E9charge"),
		DOCf("Degredable Organic Carbon fraction (DOCf)", "Proportion d\u00E9composable du carbone organique (DOCf)"),
		AverageDecompositionTime("Average degradation time (yrs)", "Dur\u00E9e de d\u00E9composition moyenne (ann\u00E9es)"),
		ImportStandList("You are about to import this new stand list. Do you want to proceed?", "Vous \u00EAtes sur le point d'importer cette nouvelle liste de placettes. Voulez-vous continuer ?"),
		NumberOfRunsToDo("Analysing the realizations", "Analyse des r\u00E9alisations"),
//		CarboneBalance("Carbon balance", "Bilan de carbone")
		;
		
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
	

	private static BufferedImage iconImage;

	protected final LERFoBCarbonAccountingTool caller;

	private CarbonAccountingToolPanelView graphicPanel;

	protected final REpiceaComboBoxOpenButton<ProductionProcessorManagerWrapper> hwpComboBox;
	protected final REpiceaComboBoxOpenButton<BiomassParametersWrapper> biomassComboBox;
	
	private final JMenuItem calculateCarbonMenuItem;
	private final JMenuItem close; // after confirmation
	private final JMenuItem help;
	

	private final JButton calculateCarbonButton;
	
	protected JProgressBar majorProgressBar;
	private final JLabel majorProgressBarMessage;

	private JProgressBar minorProgressBar;
	private final JLabel minorProgressBarMessage;

	protected final CarbonCompareScenarioDialog compareDialog;
	
	
	/**
	 * General constructor of this class.
	 * @param caller = a CarbonStorageCalculator object
	 * @throws Exception
	 */
	protected CarbonAccountingToolDialog(final LERFoBCarbonAccountingTool caller, Window owner) {
		super(owner);
		this.setCancelOnClose(false);		// no possibility for cancelling here
		this.askUserBeforeExit = true;
		this.caller = caller;
		addPropertyChangeListener(this);
		
		new DropTargetImpl<ArrayList<CarbonToolCompatibleStand>>(this, ArrayList.class, DnDConstants.ACTION_LINK);
		
		minorProgressBarMessage = UIControlManager.getLabel(MessageID.WaitingTask.toString()); // default operation for now
		
		majorProgressBarMessage = UIControlManager.getLabel(MessageID.NumberOfRunsToDo.toString()); // default operation for now
		minorProgressBar = new JProgressBar();
		majorProgressBar = new JProgressBar();
		if (caller.getCarbonCompartmentManager().nRealizations > 0) {
			majorProgressBar.setMaximum(caller.getCarbonCompartmentManager().nRealizations);
		}
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu file = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		menuBar.add(file);
		
		close = UIControlManager.createCommonMenuItem(CommonControlID.Quit);
		file.add(close);

		JMenu actionMenu = new JMenu("Actions");
		menuBar.add(actionMenu);

		calculateCarbonMenuItem = new JMenuItem(MessageID.CalculateCarbonBalance.toString());
		ImageIcon carbonIcon = CommonGuiUtility.retrieveIcon(getClass(), "carbonIcon.png");
		calculateCarbonMenuItem.setIcon(carbonIcon);
		calculateCarbonMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
		actionMenu.add(calculateCarbonMenuItem);
		
		calculateCarbonButton = new JButton();
		calculateCarbonButton.setIcon(carbonIcon);
		calculateCarbonButton.setMargin(new Insets(2,2,2,2));
		calculateCarbonButton.setToolTipText(MessageID.CalculateCarbonBalance.toString());
		
		JMenu about = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(about);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		about.add(help);

		
		hwpComboBox = new REpiceaComboBoxOpenButton<ProductionProcessorManagerWrapper>(MessageID.HWP_Parameters.toString()); 
		hwpComboBox.getComboBox().setModel(new DefaultComboBoxModel<ProductionProcessorManagerWrapper>(caller.getCarbonToolSettings().processorManagers));
		hwpComboBox.getComboBox().setSelectedIndex(caller.getCarbonToolSettings().currentProcessorManagerIndex);

		biomassComboBox  = new REpiceaComboBoxOpenButton<BiomassParametersWrapper>(MessageID.Biomass.toString()); 
		biomassComboBox .getComboBox().setModel(new DefaultComboBoxModel<BiomassParametersWrapper>(caller.getCarbonToolSettings().biomassParametersVector));
		biomassComboBox .getComboBox().setSelectedIndex(caller.getCarbonToolSettings().currentBiomassParametersIndex);
		
		refreshInterface();
		createUI();
		compareDialog = new CarbonCompareScenarioDialog(this, graphicPanel);
		pack();
		setMinimumSize(getSize());
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.gui.REpiceaFrame#anchorLocation(java.awt.Point)
	 */
	@Override
	public void anchorLocation(Point location) {
		super.anchorLocation(location);
	}
	

	@Override
	protected BufferedImage getBufferedImage() {
		if (iconImage == null) {
			String path = ObjectUtility.getRelativePackagePath(CarbonAccountingToolDialog.class);
			String iconFilename = path + "LogoLerfob.jpg";
			InputStream in = ClassLoader.getSystemResourceAsStream(iconFilename);
			try {
				iconImage = ImageIO.read(in);
			}
			catch (IOException e) {
			}
		}
		return iconImage;
	}

	
	private JPanel createStatusAndProgressBarPanel() {

		JPanel wrapperPanel = new JPanel(new BorderLayout());
		wrapperPanel.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
		wrapperPanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
		JPanel progressBarPanel = new JPanel();
		wrapperPanel.add(progressBarPanel, BorderLayout.CENTER);
		progressBarPanel.setLayout(new BoxLayout(progressBarPanel, BoxLayout.Y_AXIS));
		progressBarPanel.add(Box.createVerticalStrut(10));
		
		JPanel progressBarTaskLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		progressBarTaskLabelPanel.add(minorProgressBarMessage);
		progressBarPanel.add(progressBarTaskLabelPanel);
		minorProgressBar.setStringPainted(true);
		progressBarPanel.add(minorProgressBar);
		progressBarPanel.add(Box.createVerticalStrut(10));
		
		JPanel progressRealizationPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		progressRealizationPanel.add(majorProgressBarMessage);
		progressBarPanel.add(progressRealizationPanel);
		majorProgressBar.setStringPainted(true);
		majorProgressBar.setString("");
		progressBarPanel.add(majorProgressBar);
		progressBarPanel.add(Box.createVerticalStrut(10));
		
		
		return wrapperPanel;
	}

	/**	
	 * From ActionListener interface.
	 * Buttons management.
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(close)) {
			okAction();
		} else if (evt.getSource().equals(help)) {
			UIControlManager.getHelper(getClass()).callHelp();
		} else if (evt.getSource().equals(calculateCarbonMenuItem) || evt.getSource().equals(calculateCarbonButton)) {
			try {
				caller.calculateCarbon();
			} catch (Exception e) {
				if (e instanceof InvalidParameterException) {
					JOptionPane.showMessageDialog(this, 
							MessageID.ParametersAreInvalid.toString(), 
							REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
							JOptionPane.ERROR_MESSAGE);
				}
			} 
		} 
	}
	

	/*	
	 * User interface definition
	 */
	private void createUI() {
		graphicPanel = new CarbonAccountingToolPanelView(new CarbonAccountingToolOptionPanel());
		JPanel parameterPanel  = new JPanel(new FlowLayout(FlowLayout.LEFT));
		parameterPanel.add(Box.createHorizontalStrut(5));
		parameterPanel.add(calculateCarbonButton);
		parameterPanel.add(Box.createHorizontalStrut(10));
		parameterPanel.add(biomassComboBox);
		parameterPanel.add(Box.createHorizontalStrut(10));
		parameterPanel.add(new JSeparator());
		parameterPanel.add(hwpComboBox);
		parameterPanel.add(Box.createHorizontalStrut(5));
		
		getContentPane().add(parameterPanel, BorderLayout.NORTH);
		getContentPane().add(graphicPanel, BorderLayout.CENTER);
		getContentPane().add(createStatusAndProgressBarPanel(), BorderLayout.SOUTH);
	}

	protected void displayResult() {
		graphicPanel.addSimulationResult(caller.getCarbonCompartmentManager().getSimulationSummary());
		refreshInterface();
	}
	
	
	private void updateMajorProgressBarValue(int i) {
		majorProgressBar.setValue(i);
		majorProgressBar.setString(majorProgressBar.getValue() + " / " + majorProgressBar.getMaximum());
	}
	
	/**
	 * Listens to the progress of the TreeLogger, the WoodPieceProcessorWorker and the CarbonCompartmentManager. Also
	 * refreshes the Graphic panel when the carbon compartment manager is done.
	 */
	@SuppressWarnings("incomplete-switch")
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("state")) {
			if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
				CarbonAccountingToolTask task = (CarbonAccountingToolTask) evt.getSource();
				if (task.getFailureReason () == null) {
					String taskName = task.getName();
					if (taskName.equals(CarbonAccountingToolTask.Task.COMPILE_CARBON.name())) {
						minorProgressBarMessage.setText(REpiceaTranslator.getString(MessageID.JobDone));
						minorProgressBar.setValue(100);
					} else if (taskName.equals(CarbonAccountingToolTask.Task.DISPLAY_RESULT.name())) {
						updateMajorProgressBarValue(majorProgressBar.getMaximum());
					}
				}
				setEnabled(true);
			} else if (evt.getNewValue() == SwingWorker.StateValue.STARTED) {
				setEnabled(false);
			}
		} else if (evt.getPropertyName().equals("progress")) {
			minorProgressBar.setValue((Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals("OngoingTask")) {
			Task task = (Task) evt.getNewValue();
			switch (task) {
				case LOG_AND_BUCK_TREES:
					minorProgressBarMessage.setText(REpiceaTranslator.getString(MessageID.LoggingJob));
					updateMajorProgressBarValue(caller.getCarbonCompartmentManager().currentRealization);
					break;
				case GENERATE_WOODPRODUCTS:
					minorProgressBarMessage.setText(REpiceaTranslator.getString(MessageID.WoodPieceJob));
					break;
				case ACTUALIZE_CARBON:
					minorProgressBarMessage.setText(REpiceaTranslator.getString(MessageID.ActualizingCarbon));
					break;
				case COMPILE_CARBON:
					minorProgressBarMessage.setText(REpiceaTranslator.getString(MessageID.CarbonCompartmentJob));
					break;
			}
		} else if (evt.getPropertyName().equals(REpiceaAWTProperty.WindowAcceptedConfirmed.name()) || evt.getPropertyName().equals(REpiceaAWTProperty.WindowCancelledConfirmed.name())) {
			caller.respondToWindowClosing();
		}
	}


	@Override
	public void listenTo() {
		close.addActionListener(this);
		help.addActionListener(this);
		calculateCarbonMenuItem.addActionListener(this);
		calculateCarbonButton.addActionListener(this);
		biomassComboBox.getComboBox().addItemListener(this);
		hwpComboBox.getComboBox().addItemListener(this);
	}


	@Override
	public void doNotListenToAnymore() {
		close.removeActionListener(this);
		help.removeActionListener(this);
		calculateCarbonMenuItem.removeActionListener(this);
		calculateCarbonButton.removeActionListener(this);
		biomassComboBox.getComboBox().removeItemListener(this);
		hwpComboBox.getComboBox().removeItemListener(this);
	}

	@Override
	public void refreshInterface() {
		String suffix = "";
		if (caller.getCarbonCompartmentManager().getLastStand() != null) {
			suffix = " - " + caller.getCarbonCompartmentManager().getLastStand().getStandIdentification();
		}
		setTitle(UIControlManager.getTitle(getClass()) + suffix);
	}

	@Override
	public void acceptThisObject(ArrayList<CarbonToolCompatibleStand> stands, DropTargetDropEvent arg0) {
		if (stands != null && !stands.isEmpty()) {
			CarbonToolCompatibleStand lastStand = stands.get(stands.size() - 1);
			String lastStandID = lastStand.getStandIdentification();
			if (JOptionPane.showConfirmDialog(this, 
					MessageID.ImportStandList.toString() + " " + lastStandID, 
					UIControlManager.InformationMessageTitle.Warning.toString(), 
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE) == 0) {
				caller.setStandList(stands);
			}
		}
	}



	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(biomassComboBox.getComboBox())) {
			caller.getCarbonToolSettings().currentBiomassParametersIndex = biomassComboBox.getComboBox().getSelectedIndex();
		} else if (arg0.getSource().equals(hwpComboBox.getComboBox())) {
			caller.getCarbonToolSettings().currentProcessorManagerIndex = hwpComboBox.getComboBox().getSelectedIndex();
		}
	}
}

