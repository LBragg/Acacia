/*
 * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
 * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
 * 
 * 	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pyromaniac.GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.BoxLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JCheckBox;

import pyromaniac.AcaciaConstants;
import pyromaniac.AcaciaMain;
import pyromaniac.AcaciaEngine;
import pyromaniac.ErrorCorrectionWorker;
import pyromaniac.DataStructures.MIDPrimerCombo;
import pyromaniac.IO.AcaciaLogger;
import pyromaniac.IO.MIDReader;
import pyromaniac.IO.MIDReader.MIDFormatException;


// TODO: Auto-generated Javadoc
/**
 * The Class TagInputPanel.
 */
public class TagInputPanel extends JPanel implements ActionListener,ListSelectionListener
{	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant NUM_VERTICAL_ELEMENTS. */
	public static final int NUM_VERTICAL_ELEMENTS = 6;
	
	/** The Constant WINDOW_BACKGROUND_COLOUR. */
	public static final Color WINDOW_BACKGROUND_COLOUR = Color.decode("#000000");
	
	/** The Constant FORM_BACKGROUND_COLOUR. */
	public static final Color FORM_BACKGROUND_COLOUR = Color.decode("#DFE567");
	
	/** The Constant BORDER_COLOUR. */
	public static final Color BORDER_COLOUR = Color.decode("#777F19");
	
	/** The Constant BUTTON_GRADIENT_COLOR2. */
	public static final Color BUTTON_GRADIENT_COLOR2 = Color.decode("#C3CC66");
	
	/** The Constant BUTTON_GRADIENT_COLOR1. */
	public static final Color BUTTON_GRADIENT_COLOR1 = Color.white;
	
	/** The Constant BUTTON_BORDER_COLOR. */
	public static final Color BUTTON_BORDER_COLOR = Color.decode("#616633");
	
	/** The Constant TABLE_SELECTION_COLOUR. */
	private static final Color TABLE_SELECTION_COLOUR = Color.decode("#E6E614");
	
	/** The Constant TABLE_BACKGROUND_COLOUR. */
	private static final Color TABLE_BACKGROUND_COLOUR = Color.decode("#DBE572");
	
	/** The Constant DEFAULT_BORDER. */
	public static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(TagInputPanel.BORDER_COLOUR, 2);

	/** The Constant COLUMN_IDENTIFIERS. */
	public static final Object [] COLUMN_IDENTIFIERS = {"MID", "Primer", "Description"};

	/** The worker. */
	private ErrorCorrectionWorker worker;
	
	/** The logger. */
	private AcaciaLogger logger;
	
	/** The Constant sigStringToDouble. */
	public static final HashMap <String, Double> sigStringToDouble = new HashMap <String, Double> ();
	

	
	/** The enclosing bg. */
	private JPanel enclosingBG;
	
	/** The bg panel left. */
	private JPanel bgPanelLeft;
	
	/** The bg panel right. */
	private JPanel bgPanelRight;
	
	/** The data format. */
	private JPanel dataFormat;
	
	/** The fasta panel. */
	private JPanel fastaPanel;
	
	/** The fastq panel. */
	private JPanel fastqPanel;
	
	/** The mids. */
	private JPanel mids;
	
	/** The load mids panel. */
	private JPanel loadMidsPanel;
	
	/** The quality assurance. */
	private JPanel qualityAssurance;
	
	/** The mismatches. */
	private JPanel mismatches;
	
	/** The last dir. */
	private String lastDir;
	
	
	/** The cancel. */
	private AcaciaGradientButton cancel;
	
	/** The submit. */
	private AcaciaGradientButton submit;

	//relating to the form creation 
	/** The file format. */
	private ButtonGroup fileFormat;
	
	/** The mid selection. */
	private ButtonGroup midSelection;
	
	/** The split on mid. */
	private JCheckBox splitOnMID;
	
	
	//action strings
	/** The Constant FASTQ. */
	private static final String FASTQ = "FASTQ";
	
	/** The Constant FASTA. */
	private static final String FASTA = "FASTA";
	
	
	/** The Constant FIND_FASTQ. */
	private static final String FIND_FASTQ = "Find FASTQ";
	
	/** The Constant FIND_FASTA. */
	private static final String FIND_FASTA = "Find FASTA";
	
	/** The Constant FIND_QUAL. */
	private static final String FIND_QUAL = "Find QUAL";
	
	/** The Constant FIND_MIDS. */
	private static final String FIND_MIDS = "Find MIDS";
	
	/** The Constant SELECT_ALL_MIDS. */
	private static final String SELECT_ALL_MIDS = "Select All";
	
	/** The Constant CLEAR_ALL_MIDS. */
	private static final String CLEAR_ALL_MIDS = "Clear All";
	
	/** The Constant OPTION_FIVEBASE. */
	private static final String OPTION_FIVEBASE = "Roche supplied 5-base MIDs";
	
	/** The Constant OPTION_TENBASE. */
	private static final String OPTION_TENBASE = "Roche supplied 10-base MIDs";
	
	/** The Constant OPTION_LOAD_MIDS. */
	private static final String OPTION_LOAD_MIDS = "Load user-supplied MID descriptor file";
	
	/** The Constant OPTION_NO_MIDS. */
	private static final String OPTION_NO_MIDS = "Ignore MIDs";
	
	/** The Constant FIND_PROJECT_DIR. */
	private static final String FIND_PROJECT_DIR = "Select directory to save project:";
	
	/** The Constant SUBMIT_FORM. */
	private static final String SUBMIT_FORM = "Submit form";

	
	/** The Constant DEFAULT_PREFERRED_FIELD_HEIGHT. */
	private static final int DEFAULT_PREFERRED_FIELD_HEIGHT = 28; //this is the default height for ALL the textfield and button components.
	
	/** The Constant DEFAULT_TEXTFIELD_MAX_WIDTH. */
	private static final int DEFAULT_TEXTFIELD_MAX_WIDTH = 120;
	
	/** The Constant DEFAULT_MINIMUM_LABEL_WIDTH. */
	private static final int DEFAULT_MINIMUM_LABEL_WIDTH = 80; //this may be useful? TODO
	
	/** The fastq file choice. */
	private JTextField fastqFileChoice;	
	
	/** The fasta file choice. */
	private JTextField fastaFileChoice;
	
	/** The qual file choice. */
	private JTextField qualFileChoice;
	
	/** The project name. */
	private JTextField projectName;
	
	/** The mids choice. */
	private JTextField midsChoice;
	
	/** The output dir. */
	private JTextField outputDir;
	
	/** The trim to length. */
	private JTextField trimToLength;
	
	/** The hamming distance. */
	private JTextField manhattanDistance;
	
	/** The hamming align distance. */
	private JTextField hammingAlignDistance;
	
	/** The parent. */
	private JFrame parent;
	
	
	/** The spinner significance. */
	private JSpinner spinnerSignificance;

	/** The name panel. */
	private JPanel namePanel;
	
	/** The screen mids. */
	private JPanel screenMIDS;
	
	/** The standard output. */
	private JPanel standardOutput;
	
	/** The standard output field. */
	private JTextPane standardOutputField;
	
	/** The mid table. */
	private JTable midTable;
	
	/** The MID select all. */
	private JButton MIDSelectAll;
	
	/** The MID clear all. */
	private JButton MIDClearAll;
	
	/** The num selected. */
	private JLabel numSelected;
	
	/** The file format inner panel. */
	private JPanel fileFormatInnerPanel;
	
	/** The format card panel. */
	private JPanel formatCardPanel;
	
	/** The quality threshold. */
	private JSpinner qualityThreshold;
	
	/** The mid card panel. */
	private JPanel midCardPanel;
	
	/** The spinner representative seq. */
	private JSpinner spinnerRepresentativeSeq;
	
	/** The no mi ds. */
	private JRadioButton noMIDs;
	
	/** The load mi ds. */
	private JRadioButton loadMIDs;
	
	/** The SD threshold. */
	private JSpinner SDThreshold;
	
	/** The error model spinner. */
	private JSpinner errorModelSpinner;
	
//	private static final String [] optionsSignificance = new String [] {"0", "0.001", "0.01", "0.025", "0.05", "0.1"};
	
	/** The Constant optionsSignificance. */
private static final String [] optionsSignificance = new String [] {"0", "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10", "-Inf"};
	
	/** The Constant significanceModel. */
	private static final javax.swing.SpinnerListModel significanceModel = new SpinnerListModel(optionsSignificance);

	
	/** The Constant DEFAULT_BUTTON_WIDTH. */
	private static final int DEFAULT_BUTTON_WIDTH = 100;
	
	/** The Constant MIN_QUALITY_SCORE. */
	private static final int MIN_QUALITY_SCORE = 1;
	
	/** The Constant MAX_QUALITY_SCORE. */
	private static final int MAX_QUALITY_SCORE = 40;
	
	//messages and defaults.
	
	/** The Constant DONT_LOAD_USER_SUPPLIED_MIDS. */
	private static final String DONT_LOAD_USER_SUPPLIED_MIDS = "DONT_LOAD_MIDS_PANEL";
	
	/** The Constant HELP_QUALITY_THRESHOLD_TEXT. */
	private static final String HELP_QUALITY_THRESHOLD_TEXT = "All reads with an average quality less than this threshold will be removed from dataset. This parameter is ignored when quality values are not supplied.";
	
	/** The Constant HELP_TRIM_READS_TO_LENGTH. */
	private static final String HELP_TRIM_READS_TO_LENGTH = "Reads will be trimmed to this length prior to error correction. Leave blank if no trimming desired.";
	
	/** The Constant HELP_MAX_HAMMING_TEXT. */
	private static final String HELP_MAX_HAMMING_TEXT = "Used in pre-clustering, this is the <i> maximum </i> hamming distance between the hexamer frequencies in two reads such that they will be clustered together.";
	
	/** The Constant HELP_SIGNIFICANCE_THRESHOLD_TEXT. */
	private static final String HELP_SIGNIFICANCE_THRESHOLD_TEXT = "Loci where there are variable homopolymer lengths observed across reads will be tested to see if the difference is due to under/over-call error, or 'real' differences. " +
			"	A hypothesis test is performed to determine whether differences are due to error alone. A lower value provides more sensitivity, at the cost of specificity. Vice versa for a higher value.";
	
	/** The Constant FIVEBASE_MID_FILE. */
//	private static final String FIVEBASE_MID_FILE = "/data/MID_fivebase.csv";
	
	/** The Constant TENBASE_MID_FILE. */
//	private static final String TENBASE_MID_FILE = "/data/MID_tenbase.csv";
	
	/** The Constant HELP_REPRESENTATIVE_TEXT. */
	private static final String HELP_REPRESENTATIVE_TEXT = "During the de-replication process, a representative read is selected for read 'cluster'. This representative sequence is selected based on its length relative to others in the cluster; the representative sequence may have the minimum, maximum, mode, or median length in the cluster.";

	/** The Constant CLEAR_FORM. */
private static final String CLEAR_FORM = "CLEAR_FORM";
	
	/** The Constant CANCEL_RUN. */
	private static final String CANCEL_RUN = "CANCEL_RUN";
	
	/** The Constant HELP_SPLIT_MID_TEXT. */
	private static final String HELP_SPLIT_MID_TEXT = "When switched on, reads will be split into groups based on their MID" +
		", ie. each MID-based group will be analysed separately. When switched off, sequences with any of the user-selected MIDs" +
		"will be processed altogether.";
	
	/** The Constant HELP_FILTER_BY_LENGTH_SD. */
	private static final String HELP_FILTER_BY_LENGTH_SD = "Reads which have a un-trimmed length this many standard deviations from the mean are removed from further analysis.";
	
	/** The Constant HELP_ERROR_MODEL_TEXT. */
	private static final String HELP_ERROR_MODEL_TEXT = 
		"Two models are currently available: the Balzer et al. (2010) models include flow position as an effect, however these " +
		"require that the sequences have had the key removed, but not the MID. If some kind of quality assurance has been applied to the reads, trimming the 5' end, the Quince (2009) models are preferable.";
	//private static final String HELP_MAX_HAMMING_ALIGN_TEXT = "In the second phase of clustering, sequences with less than this hamming distance will be ";
	

	/**
	 * Instantiates a new TagInputPanel.
	 *
	 * @param util the grid bag utility which assists the layout manager for this panel
	 * @param parent the parent frame
	 * @throws Exception any exception that occurs during the creation of this TagInputPanel
	 */
	public TagInputPanel(GridBagUtility util, JFrame parent) throws Exception
	{	
			this.setVisible(true);
			this.setBackground(WINDOW_BACKGROUND_COLOUR);
			this.worker = null;
			fastqFileChoice = null;
			fastaFileChoice = null;
			qualFileChoice = null;
			this.parent = parent;
			this.logger = new AcaciaLogger();
			this.lastDir = null;
			initPanel();
	}
	
	/**
	 * Initialises the tag input panel.
	 *
	 * @throws Exception the exception
	 */
	public void initPanel() throws Exception
	{		
		//changing main TagInputPanel to have Spring Layout too.
		
		SpringLayout tagInputLayout = new SpringLayout();
		this.setLayout(tagInputLayout);

		int parentWidth = this.parent.getWidth();
		int parentHeight = this.parent.getHeight();

		//setting up the logo on this tag input panel.
        JPanel logo = createImagePanel(AcaciaMain.ACACIA_LOGO, TagInputPanel.WINDOW_BACKGROUND_COLOUR, "Acacia logo");
        tagInputLayout.putConstraint(SpringLayout.NORTH, logo, 2, SpringLayout.NORTH, this);
        
        int westOffset = 5;
        int distFromEastEdge = parentWidth - logo.getPreferredSize().width - westOffset; 
        
        tagInputLayout.putConstraint(SpringLayout.EAST, logo, -1 * (distFromEastEdge) + 5 , SpringLayout.EAST, this);
        tagInputLayout.putConstraint(SpringLayout.WEST, logo, westOffset , SpringLayout.WEST, this);
        
        
		int logoHeight = logo.getPreferredSize().height;		
		int maxPanelHeight = parentHeight - logoHeight;
        this.add(logo);
        
        //reference sizes

		Border basic = BorderFactory.createLineBorder(TagInputPanel.BORDER_COLOUR, 2);
		Dimension enclosingBGDimensions = new Dimension(parentWidth - 5, maxPanelHeight - 15);
		
		System.out.println("The enclosing BG dimensions are: " + enclosingBGDimensions.width + " , " + enclosingBGDimensions.height);
		
		this.enclosingBG = new JPanel();
		this.enclosingBG.setMinimumSize(enclosingBGDimensions);
		this.enclosingBG.setPreferredSize(enclosingBGDimensions);
		this.enclosingBG.setSize(enclosingBGDimensions);
		this.enclosingBG.setBackground(TagInputPanel.WINDOW_BACKGROUND_COLOUR);
		
		//potentially use spring layout for the rest of the components
		SpringLayout enclosingBGLayoutManager = new SpringLayout();
		this.enclosingBG.setLayout(enclosingBGLayoutManager);
	
		tagInputLayout.putConstraint(SpringLayout.NORTH, this.enclosingBG, 0, SpringLayout.SOUTH,logo);
		tagInputLayout.putConstraint(SpringLayout.SOUTH, this.enclosingBG, -5, SpringLayout.SOUTH, this);
		tagInputLayout.putConstraint(SpringLayout.WEST, this.enclosingBG, 2, SpringLayout.WEST, this);
		tagInputLayout.putConstraint(SpringLayout.EAST, this.enclosingBG, -2, SpringLayout.EAST, this);
		this.add(enclosingBG);
		
		Dimension panelPreferredDimension = new Dimension ((this.parent.getWidth()/2) - 10, (int)((maxPanelHeight - 15) * 70));
		initBGPanelLeft(panelPreferredDimension);
		initBGPanelRight(panelPreferredDimension);
		
		JPanel submitPanel = this.createSubmitOrClearPanel(enclosingBG,  basic);
		Dimension submitPanelDimensions = submitPanel.getPreferredSize();
		submitPanelDimensions.width = enclosingBG.getPreferredSize().width - 10;
		submitPanelDimensions.height = this.DEFAULT_PREFERRED_FIELD_HEIGHT;
		submitPanel.setPreferredSize(submitPanelDimensions);
		submitPanel.setSize(submitPanelDimensions);
		initStandardOutputPanel(enclosingBG, basic);
		
		this.enclosingBG.add(bgPanelLeft);
		this.enclosingBG.add(bgPanelRight);
		this.enclosingBG.add(submitPanel);
		this.enclosingBG.add(this.standardOutput);
		
		//add the panels...
		enclosingBGLayoutManager.putConstraint(SpringLayout.WEST, bgPanelLeft, 5, SpringLayout.WEST, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.EAST, bgPanelRight, -5, SpringLayout.EAST, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.EAST, bgPanelLeft, -5, SpringLayout.WEST, bgPanelRight);
		enclosingBGLayoutManager.putConstraint(SpringLayout.NORTH, bgPanelLeft, 1, SpringLayout.NORTH, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.NORTH, bgPanelRight, 1, SpringLayout.NORTH, enclosingBG);
		
		//add the submit panel
		enclosingBGLayoutManager.putConstraint(SpringLayout.WEST, submitPanel, 5, SpringLayout.WEST, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.EAST, submitPanel, -5, SpringLayout.EAST, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.NORTH, submitPanel, 5, SpringLayout.SOUTH,bgPanelRight);
		//enclosingBGLayoutManager.putConstraint(SpringLayout.NORTH, submitPanel, 5, SpringLayout.SOUTH,bgPanelLeft);
		
		
		//add the standard output panel
		enclosingBGLayoutManager.putConstraint(SpringLayout.NORTH, this.standardOutput, 5, SpringLayout.SOUTH, submitPanel);
		enclosingBGLayoutManager.putConstraint(SpringLayout.SOUTH, this.standardOutput, -5, SpringLayout.SOUTH, enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.WEST, this.standardOutput, 5, SpringLayout.WEST,  enclosingBG);
		enclosingBGLayoutManager.putConstraint(SpringLayout.EAST, this.standardOutput, -5, SpringLayout.EAST,  enclosingBG);
	
		//this.am.getLogger().flushBuffers();
	}
	
	/**
	 * Inits the bg panel left.
	 *
	 * @param preferred the preferred
	 * @throws Exception the exception
	 */
	private void initBGPanelLeft(Dimension preferred) throws Exception
	{
		this.bgPanelLeft = new JPanel();
		this.bgPanelLeft.setLayout(new SpringLayout());
		this.bgPanelLeft.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		this.bgPanelLeft.setMinimumSize(preferred);
		
		//all initialised
		//assume that these panels are instance variables
		
		this.addFileFormatPanel(this.bgPanelLeft, DEFAULT_BORDER);
		this.addQualityAssurancePanel(bgPanelLeft, DEFAULT_BORDER);
		this.addTreeConstructionPanel(bgPanelLeft, DEFAULT_BORDER);
		SpringUtilities.makeCompactGrid(bgPanelLeft, 3, 1, 5, 5, 5, 5);
	}

	
	
	/**
	 * Inits the bg panel right.
	 *
	 * @param preferred the preferred
	 * @throws Exception the exception
	 */
	private void initBGPanelRight(Dimension preferred) throws Exception
	{
		this.bgPanelRight = new JPanel();		
		this.bgPanelRight.setLayout(new SpringLayout());
		this.bgPanelRight.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		this.addMIDOptionsPanel(this.bgPanelRight, DEFAULT_BORDER);
		this.addScreenMIDsPanel(this.bgPanelRight, DEFAULT_BORDER);
		this.addProjectNamePanel(this.bgPanelRight, DEFAULT_BORDER);
	
		int preferredHeight = this.screenMIDS.getPreferredSize().height  + this.namePanel.getPreferredSize().height + this.midCardPanel.getPreferredSize().height + 120; //why 120, dunno

		Dimension minD = new Dimension(preferred.width, preferredHeight);	
		this.bgPanelRight.setMinimumSize(minD);
		this.bgPanelRight.setPreferredSize(minD);
		
		SpringUtilities.makeCompactGrid(bgPanelRight, 3, 1, 5, 5, 5, 5);
		
	}
	
	/**
	 * Locks the interface, preventing user input.
	 */
	public void lockInterface()
	{
		this.enableInputMethods(false);
	}
	
	/**
	 * Adds the tree construction panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 * @throws Exception the exception
	 */
	private void addTreeConstructionPanel(JPanel panelToAddTo, Border b) throws Exception
	{
		this.mismatches = new JPanel();
		this.mismatches.setLayout(new SpringLayout());
		this.mismatches.setBorder(BorderFactory.createTitledBorder(b, "Analysis",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		this.mismatches.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);

		
		JLabel maxHammingDistLabel = new JLabel("Maximum k-mer dist between reads:");
		adjustLabel(maxHammingDistLabel);
		setDimensionsToDefault(maxHammingDistLabel);
		
		this.manhattanDistance = new JTextField();
		this.manhattanDistance.setText(AcaciaConstants.DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST);
		this.setDimensionsToDefault(manhattanDistance);
		this.setTextFieldSize(manhattanDistance);
		manhattanDistance.setHorizontalAlignment(JTextField.RIGHT);
		
		String [] options = new String [] {AcaciaConstants.OPT_FLOWSIM_ERROR_MODEL, AcaciaConstants.OPT_PYRONOISE_ERROR_MODEL, 
				AcaciaConstants.OPT_ACACIA_IT_OT_100bp_314_MODEL,AcaciaConstants.OPT_ACACIA_IT_OT_100bp_316_MODEL, 
				AcaciaConstants.OPT_ACACIA_IT_MAN_200bp_314_MODEL, AcaciaConstants.OPT_ACACIA_IT_MAN_200bp_316_MODEL, 
				AcaciaConstants.OPT_ACACIA_IT_OT_200bp_314, AcaciaConstants.OPT_ACACIA_IT_OT_200bp_316};
		
		
		javax.swing.SpinnerListModel errorModel = new SpinnerListModel(options);
		errorModel.setValue(AcaciaConstants.OPT_FLOWSIM_ERROR_MODEL);	
		
		
		JLabel errorModelLabel = new JLabel("Error model to use:");		
		this.errorModelSpinner = new JSpinner(errorModel);
		Dimension dError = errorModelSpinner.getPreferredSize();
		dError.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH / 2;
		errorModelSpinner.setPreferredSize(dError);
		errorModelSpinner.setMaximumSize(dError);
		errorModelSpinner.setSize(dError);
		
		JLabel significanceThreshold = new JLabel("Statistical significance threshold: ");
		adjustLabel(significanceThreshold);
		setDimensionsToDefault(significanceThreshold);
		significanceModel.setValue(AcaciaConstants.DEFAULT_OPT_SIGNIFICANCE_LEVEL);
		
		spinnerSignificance = new JSpinner(significanceModel);
		
		Dimension d = spinnerSignificance.getPreferredSize();
		d.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH / 2;
		spinnerSignificance.setPreferredSize(d);
		spinnerSignificance.setMaximumSize(d);
		spinnerSignificance.setSize(d);
		setDimensionsToDefault(spinnerSignificance);
		
		String [] optionsRepresentative = {AcaciaConstants.OPT_MODE_REPRESENTATIVE, AcaciaConstants.OPT_MEDIAN_REPRESENTATIVE, AcaciaConstants.OPT_MAX_REPRESENTATIVE, AcaciaConstants.OPT_MIN_REPRESENTATIVE};
		
		javax.swing.SpinnerListModel repModel = new SpinnerListModel(optionsRepresentative);
		repModel.setValue(AcaciaConstants.DEFAULT_OPT_REPRESENTATIVE_SEQ); 
		this.spinnerRepresentativeSeq =  new JSpinner(repModel);

		
		JLabel representativeLabel = new JLabel("Representative sequence:");
		adjustLabel(representativeLabel);
		setDimensionsToDefault(representativeLabel);
		
		d = spinnerRepresentativeSeq.getPreferredSize();
		d.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH / 2;
		spinnerRepresentativeSeq.setPreferredSize(d);
		spinnerRepresentativeSeq.setMaximumSize(d);
		spinnerRepresentativeSeq.setSize(d);
		setDimensionsToDefault(spinnerRepresentativeSeq);
		
		JPanel helpPanelMaxHamming = new JPanel();
		helpPanelMaxHamming.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelMaxHamming.setLayout(new BoxLayout(helpPanelMaxHamming, BoxLayout.LINE_AXIS));
		helpPanelMaxHamming.add(new HelpButton(HELP_MAX_HAMMING_TEXT, this.parent));
		this.setJPanelToButtonSize(helpPanelMaxHamming);
		
		JPanel helpPanelSignificance = new JPanel();
		helpPanelSignificance.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelSignificance.setLayout(new BoxLayout(helpPanelSignificance, BoxLayout.LINE_AXIS));
		helpPanelSignificance.add(new HelpButton(HELP_SIGNIFICANCE_THRESHOLD_TEXT, this.parent));
		this.setJPanelToButtonSize(helpPanelSignificance);
		
		JPanel helpPanelRepresentative = new JPanel();
		helpPanelRepresentative.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelRepresentative.setLayout(new BoxLayout(helpPanelRepresentative, BoxLayout.LINE_AXIS));
		helpPanelRepresentative.add(new HelpButton(HELP_REPRESENTATIVE_TEXT, this.parent));

		JPanel helpPanelModels = new JPanel();
		helpPanelModels.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelModels.setLayout(new BoxLayout(helpPanelModels, BoxLayout.LINE_AXIS));
		helpPanelModels.add(new HelpButton(HELP_ERROR_MODEL_TEXT, this.parent));
		
		
		
		mismatches.add(maxHammingDistLabel);
		mismatches.add(manhattanDistance);
		mismatches.add(helpPanelMaxHamming);

		mismatches.add(errorModelLabel);
		mismatches.add(errorModelSpinner);
		mismatches.add(helpPanelModels);
		
		mismatches.add(significanceThreshold);
		mismatches.add(spinnerSignificance);
		mismatches.add(helpPanelSignificance);
		mismatches.add(representativeLabel);
		mismatches.add(spinnerRepresentativeSeq);
		mismatches.add(helpPanelRepresentative);
		
		SpringUtilities.makeCompactGrid(mismatches, 4, 3, 5, 5, 5, 5);
		panelToAddTo.add(mismatches);
	}

	/**
	 * Creates the submit or clear panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 * @return the j panel
	 */
	private JPanel createSubmitOrClearPanel(JPanel panelToAddTo, Border b)
	{
		JPanel submitOrClearPanel = new JPanel();
		submitOrClearPanel.setBackground(TagInputPanel.WINDOW_BACKGROUND_COLOUR);
		
		SpringLayout submitLayout = new SpringLayout();
		submitOrClearPanel.setLayout(submitLayout);	
		
		this.submit = new AcaciaGradientButton("Submit", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
		submit.setActionCommand(SUBMIT_FORM);
		submit.addActionListener(this);
	
		JButton clear = new AcaciaGradientButton("Clear form", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED); 	
		clear.setActionCommand(CLEAR_FORM);
		clear.addActionListener(this);
		
		this.cancel = new AcaciaGradientButton("Cancel Run", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
		cancel.setActionCommand(CANCEL_RUN);
		cancel.addActionListener(this);
		cancel.setEnabled(false);
		
		this.setDimensionsToDefault(submit);
		this.setDimensionsToDefault(clear);
		this.setDimensionsToDefault(cancel);
		
		submitOrClearPanel.add(submit);
		submitOrClearPanel.add(clear);
		submitOrClearPanel.add(cancel);
		
		submitLayout.putConstraint(SpringLayout.WEST, clear, 0 , SpringLayout.WEST, submitOrClearPanel);
		submitLayout.putConstraint(SpringLayout.EAST, cancel, -5,SpringLayout.WEST, submit);
		
		submitLayout.putConstraint(SpringLayout.EAST, submit,
                0,
                SpringLayout.EAST, submitOrClearPanel);
		submitLayout.putConstraint(SpringLayout.SOUTH, submitOrClearPanel,
                5,
                SpringLayout.SOUTH, submit);
		
		return submitOrClearPanel;
	}
	
	/**
	 * Adds the project name panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 */
	private void addProjectNamePanel(JPanel panelToAddTo, Border b)
	{
		this.namePanel = new JPanel();
		SpringLayout sl = new SpringLayout();
		
		this.namePanel.setLayout(sl);
		this.namePanel.setBorder(BorderFactory.createTitledBorder(b, "Output requirements",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		this.namePanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		this.projectName = new JTextField("");	

		this.setTextFieldSize(this.projectName);
		this.setDimensionsToDefault(projectName);
		
		//add to namePanel
		JLabel outputFilePrefix = new JLabel("Output file prefix:");
		this.adjustLabel(outputFilePrefix);
		this.setDimensionsToDefault(outputFilePrefix);
		
		JButton openFile = new AcaciaGradientButton("Browse...", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2,TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
		openFile.setActionCommand(FIND_PROJECT_DIR);
		openFile.addActionListener(this);
		this.setDimensionsToDefault(openFile);

		this.outputDir = new JTextField("");
		this.setTextFieldSize(this.outputDir);
					
		Dimension actualSizeTextField = outputDir.getPreferredSize();
		actualSizeTextField.width = DEFAULT_TEXTFIELD_MAX_WIDTH;
		
		outputDir.setPreferredSize(actualSizeTextField);
		
		JLabel selectProjectDir = new JLabel(TagInputPanel.FIND_PROJECT_DIR);
		this.setDimensionsToDefault(selectProjectDir);
		this.adjustLabel(selectProjectDir);
		
		JLabel [] labels = {outputFilePrefix, new JLabel(""), selectProjectDir};
		JComponent [] middle = {projectName,new JLabel(""),outputDir};
		JComponent [] last = {new JLabel(""),new JLabel(""),openFile}; //empty anchor used to be 'openFile'
		prepareFormPanel(namePanel,labels,middle, last);
		
		panelToAddTo.add(namePanel);
	}
	
	/**
	 * Adjust label.
	 *
	 * @param label the label
	 */
	private void adjustLabel(JLabel label)
	{
		Dimension d = label.getPreferredSize();
		d.width = DEFAULT_MINIMUM_LABEL_WIDTH;
		label.setMinimumSize(d);
		label.setPreferredSize(d);
	}
	
	/**
	 * Prepare form panel.
	 *
	 * @param parent the parent
	 * @param first the first
	 * @param middle the middle
	 * @param last the last
	 */
	private void prepareFormPanel(JPanel parent, JLabel [] first, JComponent [] middle, JComponent[] last)
	{
		int numRows = first.length;
        for (int i = 0; i < numRows; i++) 
        {
            parent.add(first[i]);
            parent.add(middle[i]);
            parent.add(last[i]);
        }
		SpringUtilities.makeCompactGrid(parent, numRows, 3, 5, 5, 5, 5);
	}
	
	/**
	 * Adds the file format panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 */
	private void addFileFormatPanel(JPanel panelToAddTo, Border b)
	{
		//creation of the buttons JPanel
		this.dataFormat = new JPanel();
		this.dataFormat.setBorder(BorderFactory.createTitledBorder(b, "Pyrotag data format",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		
		SpringLayout sp = new SpringLayout();
		this.dataFormat.setLayout(sp);
		this.dataFormat.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		//prepare buttons!
		this.fileFormat = new ButtonGroup();
		JRadioButton fastqButton = new JRadioButton("FASTQ");
		fastqButton.setActionCommand(FASTQ);
		fastqButton.addActionListener(this);
		fastqButton.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		this.setDimensionsToDefault(fastqButton);
		
		JRadioButton separateButton = new JRadioButton("FASTA (with separate quality file)");
		separateButton.setActionCommand(FASTA);
		separateButton.addActionListener(this);
		separateButton.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		this.setDimensionsToDefault(separateButton);

		this.fileFormat.add(fastqButton);
		this.fileFormat.add(separateButton);
		this.fileFormat.setSelected(separateButton.getModel(),true);
		
		this.fileFormatInnerPanel = new JPanel();
		fileFormatInnerPanel.setLayout(new SpringLayout());
		fileFormatInnerPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		fileFormatInnerPanel.add(fastqButton);
		fileFormatInnerPanel.add(separateButton);
		
		SpringUtilities.makeCompactGrid(fileFormatInnerPanel, 1, 2, 0, 0, 50, 5);
		
		dataFormat.add(fileFormatInnerPanel);		

		this.formatCardPanel = new JPanel();
		this.formatCardPanel.setLayout(new CardLayout());
	
		this.initFormatCards();		

		Dimension ffd = fileFormatInnerPanel.getPreferredSize();
		dataFormat.add(this.formatCardPanel);
		SpringUtilities.makeCompactGrid(dataFormat, 2, 1, 0, 0, 5, 5);	
		SpringUtilities.printSizes(dataFormat);
		
		Dimension minSize = dataFormat.getMinimumSize();
		dataFormat.setPreferredSize(minSize);
		panelToAddTo.add(dataFormat);
	}
	
	/**
	 * Inits the format cards.
	 */
	private void initFormatCards() 
	{
		createFASTAPanel();
		createFASTQPanel();
		
		this.formatCardPanel.add(this.fastaPanel, TagInputPanel.FASTA); //assuming that is fasta selected??
		this.formatCardPanel.add(this.fastqPanel, TagInputPanel.FASTQ);
	
		Dimension preferredSizeFASTQ = this.fastqPanel.getPreferredSize();
		Dimension cardPanelMax = new Dimension(Integer.MAX_VALUE,preferredSizeFASTQ.height);
		this.formatCardPanel.setMaximumSize(cardPanelMax);
		CardLayout cl = (CardLayout) this.formatCardPanel.getLayout();
		
		//default FASTA is selected
		cl.show(this.formatCardPanel, TagInputPanel.FASTA); //make sure the radiobutton for fasta is selected.
	}

	/**
	 * Inits the standard output panel.
	 *
	 * @param parent the parent
	 * @param b the b
	 */
	private void initStandardOutputPanel(JPanel parent, Border b)
	{
		this.standardOutput = new JPanel();
		this.standardOutput.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		standardOutput.setBorder(BorderFactory.createTitledBorder(b, "Acacia Standard Output",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		
		Dimension stdOutDimensions = new Dimension(parent.getPreferredSize().width - 5, 50);
		this.standardOutput.setPreferredSize(stdOutDimensions);
		this.standardOutput.setMaximumSize(stdOutDimensions);
		this.standardOutput.setSize(stdOutDimensions);
		SpringLayout soLayoutManager = new SpringLayout();
		
		this.standardOutput.setLayout(soLayoutManager);
		this.standardOutputField = new JTextPane();	
		this.standardOutputField.setEditable(false);
		this.standardOutputField.setBackground(Color.white);
		this.standardOutputField.setContentType("text/html");

		
		Dimension textFieldSize = new Dimension(parent.getPreferredSize().width - 100, 10);
		standardOutputField.setPreferredSize(textFieldSize);
		standardOutputField.setMaximumSize(textFieldSize);
		standardOutputField.setSize(textFieldSize);
		
		JScrollPane textFieldViewer = new JScrollPane(this.standardOutputField);

		LogTextPane lta = new LogTextPane(this.standardOutputField, textFieldViewer);
		
		//this.standardOutputField.
		this.logger.addOutput(lta, AcaciaLogger.LOG_PROGRESS);
		this.logger.addOutput(lta, AcaciaLogger.LOG_ERROR);
		
		soLayoutManager.putConstraint(SpringLayout.EAST, textFieldViewer, -5, SpringLayout.EAST, this.standardOutput);
		soLayoutManager.putConstraint(SpringLayout.WEST, textFieldViewer, 5, SpringLayout.WEST, this.standardOutput);
		soLayoutManager.putConstraint(SpringLayout.NORTH, textFieldViewer, 5, SpringLayout.NORTH, this.standardOutput);
		soLayoutManager.putConstraint(SpringLayout.SOUTH, textFieldViewer, -5, SpringLayout.SOUTH, this.standardOutput);
		this.standardOutput.add(textFieldViewer);
	}
	
	/**
	 * Clear standard output panel.
	 */
	private void clearStandardOutputPanel()
	{
		this.standardOutputField.setText("");
	}
	
	/**
	 * Adds the quality assurance panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 * @throws Exception the exception
	 */
	private void addQualityAssurancePanel(JPanel panelToAddTo, Border b) throws Exception
	{
		this.qualityAssurance = new JPanel();
		this.qualityAssurance.setBorder(BorderFactory.createTitledBorder(b, "Tag Quality Assurance",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		this.qualityAssurance.setLayout(new SpringLayout());
		this.qualityAssurance.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		this.qualityThreshold = new JSpinner();		

		String [] optionsQualities = new String [MAX_QUALITY_SCORE - MIN_QUALITY_SCORE + 2];
		
		for(int i = MIN_QUALITY_SCORE; i <= MAX_QUALITY_SCORE; i++)
		{
				optionsQualities[i] = i + "";
		}
		
		
		javax.swing.SpinnerListModel qualityThreshModel = new SpinnerListModel(optionsQualities);
		qualityThreshModel.setValue(AcaciaConstants.DEFAULT_OPT_MIN_AVG_QUALITY);
		
		this.qualityThreshold.setModel(qualityThreshModel);
		this.setDimensionsToDefault(qualityThreshold);
		
		Dimension d = this.qualityThreshold.getPreferredSize();
		d.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH;
		
		this.qualityThreshold.setPreferredSize(d);
		this.qualityThreshold.setMaximumSize(d);
		this.qualityThreshold.setMinimumSize(d);
		
		JLabel qualityLabel = new JLabel("Minimum avg. quality threshold:");
		qualityLabel.setBackground(FORM_BACKGROUND_COLOUR);
		this.setDimensionsToDefault(qualityLabel);
		this.adjustLabel(qualityLabel);

		JPanel helpPanelQualityThreshold = new JPanel();
		helpPanelQualityThreshold.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelQualityThreshold.setLayout(new BoxLayout(helpPanelQualityThreshold, BoxLayout.LINE_AXIS));
		helpPanelQualityThreshold.add(new HelpButton(HELP_QUALITY_THRESHOLD_TEXT, this.parent));
		this.setJPanelToButtonSize(helpPanelQualityThreshold);
		//create a action listener?
		
		JPanel helpPanelTrimLength = new JPanel();
		helpPanelTrimLength.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelTrimLength.setLayout(new BoxLayout(helpPanelTrimLength, BoxLayout.LINE_AXIS));
		helpPanelTrimLength.add(new HelpButton(HELP_TRIM_READS_TO_LENGTH, this.parent));
		this.setJPanelToButtonSize(helpPanelTrimLength);
		
		this.trimToLength = new JTextField();
		this.setDimensionsToDefault(trimToLength);
		this.setTextFieldSize(trimToLength);
		this.trimToLength.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel trimLabel = new JLabel("Trim all reads to length:");
		this.adjustLabel(trimLabel);
		this.setDimensionsToDefault(trimLabel);	
		
		JLabel removeReadsSDLabel = new JLabel("Max. no. of SD's from mean length:");
		this.adjustLabel(removeReadsSDLabel);
		this.setDimensionsToDefault(removeReadsSDLabel);
		
		
		
		
		JPanel helpPanelSD = new JPanel();
		helpPanelSD.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelSD.setLayout(new BoxLayout(helpPanelSD, BoxLayout.LINE_AXIS));
		helpPanelSD.add(new HelpButton(HELP_FILTER_BY_LENGTH_SD, this.parent));
		this.setJPanelToButtonSize(helpPanelSD);
		
		
		String [] sdOptions = {"0.5", "1", "1.5", "2", "2.5", "3","4","5"};
		
		
		this.SDThreshold = new JSpinner();
		
		javax.swing.SpinnerListModel SDModel = new SpinnerListModel(sdOptions);
		SDModel.setValue(AcaciaConstants.DEFAULT_OPT_MAX_STD_DEV_LENGTH);
		this.SDThreshold.setModel(SDModel);
		
		this.setDimensionsToDefault(this.SDThreshold);
		Dimension d2 = this.SDThreshold.getPreferredSize();
		d2.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH;
		
		this.SDThreshold.setPreferredSize(d2);
		this.SDThreshold.setMaximumSize(d2);
		this.SDThreshold.setMinimumSize(d2);
		
		
		//add all the components
		qualityAssurance.add(qualityLabel);
		qualityAssurance.add(qualityThreshold);
		qualityAssurance.add(helpPanelQualityThreshold);
		qualityAssurance.add(trimLabel);
		qualityAssurance.add(trimToLength);
		qualityAssurance.add(helpPanelTrimLength);
		qualityAssurance.add(removeReadsSDLabel);
		qualityAssurance.add(SDThreshold);
		qualityAssurance.add(helpPanelSD);
		
		
		
		SpringUtilities.makeCompactGrid(qualityAssurance, 3,3,5,5,5,5);
		qualityAssurance.setVisible(true);
	
		//add to this panel
		panelToAddTo.add(qualityAssurance);
	}
	
	/**
	 * Creates the blank button sized label.
	 *
	 * @return the j label
	 */
	private JLabel createBlankButtonSizedLabel()
	{
		JLabel blankLabel = new JLabel("");
		this.setDimensionsToDefault(blankLabel);
		Dimension d = blankLabel.getPreferredSize();
		d.width = DEFAULT_BUTTON_WIDTH;
		blankLabel.setPreferredSize(d);
        blankLabel.setSize(d);
        blankLabel.setMaximumSize(d);
		return blankLabel;
	}
	
	/**
	 * Sets the j panel to button size.
	 *
	 * @param panel the new j panel to button size
	 */
	private void setJPanelToButtonSize(JPanel panel)
	{
		this.setDimensionsToDefault(panel);
		Dimension d = panel.getPreferredSize();
		d.width = DEFAULT_BUTTON_WIDTH;
		panel.setPreferredSize(d);
		panel.setSize(d);
        panel.setMaximumSize(d);
	}
	
	/**
	 * Adds the screen mi ds panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the b
	 * @throws Exception the exception
	 */
	private void addScreenMIDsPanel(JPanel panelToAddTo, Border b) throws Exception
	{
		this.screenMIDS = new JPanel();
		this.screenMIDS.setBackground(FORM_BACKGROUND_COLOUR);
		this.screenMIDS.setLayout(new SpringLayout());
		
		DefaultTableModel tableModel = new DefaultTableModel(0, TagInputPanel.COLUMN_IDENTIFIERS.length);
		tableModel.setColumnIdentifiers(TagInputPanel.COLUMN_IDENTIFIERS);
		
		this.midTable = new JTable(tableModel);
		this.midTable.setBackground(TABLE_BACKGROUND_COLOUR);

		midTable.setSelectionBackground(TABLE_SELECTION_COLOUR);
		midTable.setRowSelectionAllowed(true);
		midTable.getSelectionModel().addListSelectionListener(this); 
		midTable.getColumnModel().getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollPane = new JScrollPane(midTable);
		Dimension spPreferred = new Dimension(midTable.getPreferredSize().width, 120);
		scrollPane.setPreferredSize(spPreferred);
		scrollPane.setMaximumSize(scrollPane.getPreferredSize());
		scrollPane.setMinimumSize(scrollPane.getPreferredSize());
		
		midTable.setPreferredScrollableViewportSize(new Dimension(midTable.getPreferredSize().width, 150));
		
		this.screenMIDS.setBorder(BorderFactory.createTitledBorder(b, "MID Selection",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		
		JPanel selectMIDSButtonsPanel = new JPanel();
		selectMIDSButtonsPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		selectMIDSButtonsPanel.setLayout(new SpringLayout());
		
		this.MIDSelectAll = new AcaciaGradientButton(SELECT_ALL_MIDS,TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
		this.MIDSelectAll.setBackground(BUTTON_GRADIENT_COLOR2);
		this.MIDSelectAll.addActionListener(this);
		this.MIDSelectAll.setEnabled(false);
		
		this.MIDClearAll = new AcaciaGradientButton(CLEAR_ALL_MIDS,TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);	
		this.MIDClearAll.setBackground(BUTTON_GRADIENT_COLOR2);
		this.MIDClearAll.addActionListener(this);
		this.MIDClearAll.setEnabled(false);
		
		this.setDimensionsToDefault(MIDSelectAll);
		this.setDimensionsToDefault(MIDClearAll);
		
		this.numSelected = new JLabel("Number selected: 0");
		this.setDimensionsToDefault(numSelected);
		this.adjustLabel(numSelected);
		this.numSelected.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		selectMIDSButtonsPanel.add(this.numSelected);
		selectMIDSButtonsPanel.add(this.MIDSelectAll);
		selectMIDSButtonsPanel.add(this.MIDClearAll);
		
		
		SpringUtilities.makeCompactGrid(selectMIDSButtonsPanel, 1, 3, 0, 0, 5, 5);
		this.screenMIDS.add(scrollPane);
		this.screenMIDS.add(selectMIDSButtonsPanel);
		
		JPanel aggregateMIDSPanel = new JPanel();
		aggregateMIDSPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
	
		this.splitOnMID = new JCheckBox("Process MIDs separately?");
		this.splitOnMID.setBackground(FORM_BACKGROUND_COLOUR);
		this.splitOnMID.setEnabled(false);
		
		JPanel helpPanelSplitMID = new JPanel();
		helpPanelSplitMID.setBackground(FORM_BACKGROUND_COLOUR);
		helpPanelSplitMID.setLayout(new BoxLayout(helpPanelSplitMID, BoxLayout.LINE_AXIS));
		helpPanelSplitMID.add(new HelpButton(HELP_SPLIT_MID_TEXT, this.parent));
		this.setJPanelToButtonSize(helpPanelSplitMID);
		//create a action listener?
		
		
		
		SpringLayout sl = new SpringLayout();
		aggregateMIDSPanel.setLayout(sl);
		//this panel should have a label and radio button?
		aggregateMIDSPanel.add(splitOnMID);
		aggregateMIDSPanel.add(helpPanelSplitMID);
		
		SpringUtilities.makeCompactGrid(aggregateMIDSPanel, 1, 2, 0, 0, 5, 5);
		
		this.screenMIDS.add(aggregateMIDSPanel);
		
		SpringUtilities.makeCompactGrid(this.screenMIDS, 3, 1, 5, 5, 5, 5);
		
		panelToAddTo.add(screenMIDS);
	}
	
	/**
	 * Clears the MIDs panel.
	 */
	private void clearMIDSPanel()
	{
		DefaultTableModel tm = (DefaultTableModel) this.midTable.getModel();
		tm.setDataVector(null, TagInputPanel.COLUMN_IDENTIFIERS);
		this.splitOnMID.setEnabled(false);
		this.MIDClearAll.setEnabled(false);
		this.MIDSelectAll.setEnabled(false);
	}
		
	/**
	 * Populate the MIDS panel.
	 *
	 * @param filename the file containing the MID descriptions and sequences
	 */
	private void populateMIDSPanel(String filename)
	{
		DefaultTableModel tm = (DefaultTableModel) this.midTable.getModel();
		
		if(filename == null || filename.length() == 0)
		{
			return;
		}

		System.out.println("The file path is <" + filename + ">");
		
		MIDReader mReader = new MIDReader(filename);
		
		
		LinkedList <MIDPrimerCombo> mids = null;
		
		try
		{
			mids = mReader.loadMIDS();
		}
		catch(MIDFormatException mfe)
		{
			try
			{
				this.logger.writeLog(mfe.getMessage(), AcaciaLogger.LOG_ERROR);
			}
			catch(Exception e)
			{
				this.cleanExit("", e); //error writing to logger, not the MIDformat exception
			}
			return;
		}
		
		Object [][] data = new Object [mids.size()][3];
		
		int i = 0;
		
		HashSet <String> seenBefore = new HashSet <String>();
		
		for(MIDPrimerCombo mid : mids)
		{
			data[i][0]  = mid.getMID();
			data[i][1] = mid.getPrimer();
			data[i][2] = mid.getDescriptor();
			i++;
		}
		tm.setDataVector(data, TagInputPanel.COLUMN_IDENTIFIERS);
		
		this.splitOnMID.setEnabled(true);
		this.splitOnMID.setSelected(false);
		this.MIDClearAll.setEnabled(true);
		this.MIDSelectAll.setEnabled(true);
	}
	
	/**
	 * Clean exit. Probably should implement this.
	 *
	 * @param string the string
	 * @param e the e
	 */
	private void cleanExit(String string, Exception e) 
	{
		System.out.println("An exception occurred: " + e.getMessage());
		e.printStackTrace();
		System.exit(1); //an error occurred
		
	}

	/**
	 * Removes the screen mids panel.
	 *
	 * @param panelToRemoveFrom the panel to remove from
	 */
	private void removeScreenMIDsPanel(JPanel panelToRemoveFrom)
	{
		panelToRemoveFrom.remove(screenMIDS);
	}
	
	/**
	 * Sets the dimensions to default.
	 *
	 * @param component the new dimensions to default
	 */
	private void setDimensionsToDefault (Component component)
	{
		Dimension preferred = component.getPreferredSize();
		preferred.height = DEFAULT_PREFERRED_FIELD_HEIGHT;
		component.setPreferredSize(preferred);
		component.setMinimumSize(preferred);
		component.setSize(preferred);
	}
	
	/**
	 * Adds the mid options panel.
	 *
	 * @param panelToAddTo the panel to add to
	 * @param b the border to use
	 */
	private void addMIDOptionsPanel(JPanel panelToAddTo, Border b)
	{
		/* 
		 * Create MID Panel 
		 */
		
		this.mids = new JPanel();
		this.mids.setLayout(new SpringLayout());
		this.mids.setBorder(BorderFactory.createTitledBorder(b, "Multiplex ID (MID) Status",TitledBorder.LEFT,TitledBorder.TOP, this.getFont()));
		this.mids.setBackground(FORM_BACKGROUND_COLOUR);
		
		//what options do we want the user to have...
		//MID file, no MIDS, 
		midSelection = new ButtonGroup();

		
		this.loadMIDs = new JRadioButton(OPTION_LOAD_MIDS);
		loadMIDs.setActionCommand(OPTION_LOAD_MIDS);
		loadMIDs.addActionListener(this);
		loadMIDs.setBackground(FORM_BACKGROUND_COLOUR);
		
		this.noMIDs = new JRadioButton(OPTION_NO_MIDS);
		noMIDs.setActionCommand(OPTION_NO_MIDS);
		noMIDs.addActionListener(this);
		noMIDs.setBackground(FORM_BACKGROUND_COLOUR);
		
		//midSelection.add(fiveBase);
	//	midSelection.add(tenBase);
		midSelection.add(loadMIDs);
		midSelection.add(noMIDs);
		
		JPanel midsButtonPanel = new JPanel();
		midsButtonPanel.setLayout(new SpringLayout());
		midsButtonPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		//midsButtonPanel.add(fiveBase);
	//	midsButtonPanel.add(tenBase);
		midsButtonPanel.add(loadMIDs);
		midsButtonPanel.add(noMIDs);
		
		//changed rows to 1 from 2
		SpringUtilities.makeCompactGrid(midsButtonPanel, 1, 2, 0, 0, 5, 5);
		
		this.midCardPanel = new JPanel();
		this.midCardPanel.setLayout(new CardLayout());
	
		this.initMIDCards();		
		
		this.mids.add(midsButtonPanel);
		this.mids.add(this.midCardPanel);
		
		SpringUtilities.makeCompactGrid(this.mids, 2, 1, 5, 5, 5, 5);
		panelToAddTo.add(mids);
	}
	
	/**
	 * Initialises the MID card containers.
	 */
	private void initMIDCards()
	{
		this.initLoadMIDSPanel();
		JPanel blank = new JPanel();
		blank.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
		this.midCardPanel.add(this.loadMidsPanel, TagInputPanel.OPTION_LOAD_MIDS); //assuming that is fasta selected??
		this.midCardPanel.add(blank, TagInputPanel.DONT_LOAD_USER_SUPPLIED_MIDS);
	
		Dimension preferredSizeFASTQ = this.loadMidsPanel.getPreferredSize();
		Dimension cardPanelMax = new Dimension(Integer.MAX_VALUE,preferredSizeFASTQ.height);
		
		this.midCardPanel.setMaximumSize(cardPanelMax);
		CardLayout cl = (CardLayout) this.midCardPanel.getLayout();
		
		//default FASTA is selected
		cl.show(this.midCardPanel, TagInputPanel.DONT_LOAD_USER_SUPPLIED_MIDS);
		
		//make sure the radiobutton for fasta is selected.
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		//these actions are to do with the choice of radio button
		if(e.getActionCommand().equals(FASTQ))
		{
			CardLayout cl = (CardLayout) this.formatCardPanel.getLayout();
			cl.show(this.formatCardPanel, e.getActionCommand());
		}
		else if(e.getActionCommand().equals(FASTA))
		{
			CardLayout cl = (CardLayout) this.formatCardPanel.getLayout();
			cl.show(this.formatCardPanel, e.getActionCommand());
		}
		else if(e.getActionCommand().equals(FIND_FASTQ))
		{
			//spawn a dialogue box, get the file string, and set it for the respective text field...
		    this.fastqFileChoice.setText(this.getFileChoice(fastqPanel, fastqFileChoice, JFileChooser.FILES_ONLY));
		}
		else if(e.getActionCommand().equals(FIND_FASTA))
		{
			this.fastaFileChoice.setText(this.getFileChoice(fastaPanel, fastaFileChoice, JFileChooser.FILES_ONLY));
		}
		else if(e.getActionCommand().equals(FIND_QUAL))
		{
			this.qualFileChoice.setText(this.getFileChoice(fastaPanel, qualFileChoice, JFileChooser.FILES_ONLY));	
		}
		else if(e.getActionCommand().equals(OPTION_LOAD_MIDS))
		{
			//create load mids panel
			this.midsChoice.setText("");
			this.clearMIDSPanel();
			
			CardLayout cl = (CardLayout)this.midCardPanel.getLayout();
			cl.show(this.midCardPanel, TagInputPanel.OPTION_LOAD_MIDS);
		}
		else if(e.getActionCommand().equals(OPTION_NO_MIDS))
		{
			
			CardLayout cl = (CardLayout)this.midCardPanel.getLayout();
			cl.show(this.midCardPanel, TagInputPanel.DONT_LOAD_USER_SUPPLIED_MIDS);
			
			this.clearMIDSPanel();
			
			if(e.getActionCommand().equals(OPTION_NO_MIDS))
			{
				this.populateMIDSPanel(null);
				this.midsChoice.setText("");
			}
		}
		else if(e.getActionCommand().equals(FIND_MIDS))
		{
			this.midsChoice.setText(this.getFileChoice(this.mids, midsChoice, JFileChooser.FILES_ONLY));
			this.populateMIDSPanel(this.midsChoice.getText());
			
		}
		else if(e.getActionCommand().equals(FIND_PROJECT_DIR))
		{
			if(this.outputDir == null)
			{
				System.out.println("Project dir is null");
			}
			this.outputDir.setText(this.getFileChoice(this.namePanel, outputDir, JFileChooser.DIRECTORIES_ONLY));
		}
		else if(e.getActionCommand().equals(SELECT_ALL_MIDS))
		{
			this.midTable.selectAll();
		}
		else if(e.getActionCommand().equals(CLEAR_ALL_MIDS))
		{
			this.midTable.clearSelection();
		}
		else if(e.getActionCommand().equals(SUBMIT_FORM))
		{
			processFormContents();
		}
		else if(e.getActionCommand().equals(CLEAR_FORM))
		{
			this.clearInterface();
		}
		else if(e.getActionCommand().equals(CANCEL_RUN))
		{
			this.cancelRun();
		}
	}
	
	/**
	 * Cancel run.
	 */
	private void cancelRun() 
	{
		if(this.worker != null)
		{
			this.worker.cancel(true);
		}
	}
	
	/**
	 * Worker cancelled.
	 */
	public void workerCancelled()
	{
		try
		{
			this.logger.writeLog("Job cancelled!", AcaciaLogger.LOG_PROGRESS);
			//truly finished.
			this.worker = null;
			this.submit.setEnabled(true);
			this.cancel.setEnabled(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Process form contents.
	 */
	private void processFormContents()
	{		
		StringBuilder errors = new StringBuilder();
		String projectName = this.projectName.getText();
		
		if(projectName.trim().length() == 0)
		{
			errors.append("Error: Output prefix not specified" + System.getProperty("line.separator"));
		}
		
		String outputDir = this.outputDir.getText();
		File output = new File(outputDir);
		
		if(!output.exists())
		{
			errors.append("Error: No output directory has been selected" + System.getProperty("line.separator"));
		}
		
		if(this.midTable.getRowCount() > 0 && this.midTable.getSelectedRowCount() == 0)
		{
			errors.append("Error: No MIDS have been selected " + System.getProperty("line.separator"));
		}
		
		if(fileFormat.getSelection().getActionCommand().equals(FASTA))
		{
			if(this.fastaFileChoice.getText().length() == 0)
			{
				errors.append("Error: Input FASTA file not specified. " + System.getProperty("line.separator"));
			}
			else
			{
				File fasta = new File (this.fastaFileChoice.getText());
				
				if(!fasta.exists())
				{
					errors.append("Error: file " + this.fastaFileChoice.getText() + " does not exist" + System.getProperty("line.separator"));
				}
			}
		}
		
		if(fileFormat.getSelection().getActionCommand().equals(FASTA) &&  (this.qualFileChoice.getText()).length() > 0)
		{
			File quality = new File(this.qualFileChoice.getText());
			
			if(!quality.exists())
			{
				errors.append("Error: file " + this.qualFileChoice.getText() + " does not exist" + System.getProperty("line.separator")); 
			}				
		}
		
		int trimToLength = 0;
	
		if(this.trimToLength.getText().length() > 0)
		{
			try
			{
				trimToLength = Integer.parseInt(this.trimToLength.getText());
			}
			catch(NumberFormatException nfe)
			{
				errors.append("Error: trim length is not a number. " + System.getProperty("line.separator"));
			}
		}
		
		int manDist = 0;
		if(this.manhattanDistance.getText().length() > 0)
		{
			boolean error = false;
			try
			{
				manDist = Integer.parseInt(this.manhattanDistance.getText());
			}
			catch(NumberFormatException ife)
			{
				errors.append("Error: manhattan distance is not a number " + System.getProperty("line.separator"));
				error =true;
			}	
			
			if(!error && manDist < 0)
			{
				errors.append("Error: manhattan distance was less than zero " + System.getProperty("line.separator"));
			}
		}
		
	
		if(errors.length() > 0)
		{
			errors.insert(0,"The following error/s occurred when processing your input:" + System.getProperty("line.separator"));
			JOptionPane.showMessageDialog(this,
			    errors.toString(),
			    "Input Error",
			    JOptionPane.ERROR_MESSAGE);
			return;
		}

		//if all required settings are valid, then proceed to storing these options...
		HashMap <String, String> settings = new HashMap <String, String>();
		settings.put(AcaciaConstants.OPT_OUTPUT_PREFIX, projectName);
		
		
		if(fileFormat.getSelection().getActionCommand().equals(FASTA))
		{
			settings.put(AcaciaConstants.OPT_FASTA, "TRUE");
			settings.put(AcaciaConstants.OPT_FASTA_LOC, this.fastaFileChoice.getText());
			
			
			String qualFile = (this.qualFileChoice.getText().trim().length() > 0)? qualFileChoice.getText(): null;
			
			settings.put(AcaciaConstants.OPT_QUAL_LOC, qualFile);
		}
		else
		{
			settings.put(AcaciaConstants.OPT_FASTQ, "TRUE");
			settings.put(AcaciaConstants.OPT_FASTA, "FALSE");
			settings.put(AcaciaConstants.OPT_FASTQ_LOC, this.fastqFileChoice.getText());
		}
					
		if(trimToLength != 0)
		{
			settings.put(AcaciaConstants.OPT_TRIM_TO_LENGTH, this.trimToLength.getText());
		}
		
		
		
		settings.put(AcaciaConstants.OPT_MAX_STD_DEV_LENGTH, (String) this.SDThreshold.getValue());
		settings.put(AcaciaConstants.OPT_MIN_AVG_QUALITY, (String) this.qualityThreshold.getValue());
		settings.put(AcaciaConstants.OPT_MAXIMUM_MANHATTAN_DIST, (String) this.manhattanDistance.getText());
		settings.put(AcaciaConstants.OPT_ERROR_MODEL, (String) this.errorModelSpinner.getValue());
		
		//settings.put(Acacia, arg1) for align hamming distance.
		settings.put(AcaciaConstants.OPT_SIGNIFICANCE_LEVEL, (String)this.spinnerSignificance.getValue());
		settings.put(AcaciaConstants.OPT_OUTPUT_DIR, this.outputDir.getText());
		settings.put(AcaciaConstants.OPT_REPRESENTATIVE_SEQ, (String)this.spinnerRepresentativeSeq.getValue());
		settings.put(AcaciaConstants.OPT_SPLIT_ON_MID, (this.splitOnMID.isSelected())? "TRUE" : "FALSE");
		
		if(this.loadMIDs.isSelected())
		{
			settings.put(AcaciaConstants.OPT_MID, AcaciaConstants.OPT_LOAD_MIDS);
		}
		else if(this.noMIDs.isSelected())
		{
			settings.put(AcaciaConstants.OPT_MID, AcaciaConstants.OPT_NO_MID);
		}
		
		LinkedList <MIDPrimerCombo> validTags = this.getSelectedTags();
				
		//set up objects required by worker thread.
				// in case a value is not specified, we use the default
		
		HashMap <String, String> defaults = AcaciaEngine.getEngine().getDefaultSettings();
		
		for (String key : settings.keySet()) 
		{
			defaults.put(key, settings.get(key));

			System.out.println("User-specified settings: " + key + ": "+ settings.get(key));
		}
		
		this.worker = new ErrorCorrectionWorker(defaults, logger,validTags, this);
		this.cancel.setEnabled(true);
		this.submit.setEnabled(false);
		this.worker.execute(); //now running, but GUI should still be relatively responsive?
	}

	/**
	 * Gets the selected tags.
	 *
	 * @return the selected tags
	 */
	private LinkedList <MIDPrimerCombo> getSelectedTags()
	{
		LinkedList <MIDPrimerCombo> selectedTags = new LinkedList <MIDPrimerCombo>();
		
		int [] selectedRows = this.midTable.getSelectedRows();
		DefaultTableModel dft = (DefaultTableModel) this.midTable.getModel();
		
		for(int i = 0; i < selectedRows.length; i++)
		{
			String midValue = (String) dft.getValueAt(selectedRows[i], 0);
			String primerValue = (String) dft.getValueAt(selectedRows[i], 1);
			String descriptor = (String) dft.getValueAt(selectedRows[i], 2); 

			midValue = midValue.toUpperCase();
			primerValue = primerValue.toUpperCase();
			
			MIDPrimerCombo m = new MIDPrimerCombo(midValue, primerValue, descriptor);
			selectedTags.add(m); //assumption
		}
		
		return selectedTags;
	}
	

	/**
	 * Inits the load mids panel.
	 */
	private void initLoadMIDSPanel() 
	{
		if(this.loadMidsPanel == null)
		{
			this.loadMidsPanel = new JPanel();
			this.loadMidsPanel.setLayout(new SpringLayout());
			this.loadMidsPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			
			JButton openFile = new AcaciaGradientButton("Browse...",TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2,TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
			openFile.setActionCommand(FIND_MIDS);
			openFile.addActionListener(this);
			this.midsChoice = new JTextField("");
	
			this.setTextFieldSize(midsChoice);
			
			JLabel selectMIDS = new JLabel("Select MID descriptor file:");
			selectMIDS.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			this.adjustLabel(selectMIDS);
			this.setDimensionsToDefault(selectMIDS);
			
			loadMidsPanel.add(selectMIDS);
			loadMidsPanel.add(midsChoice);
			loadMidsPanel.add(openFile);
			SpringUtilities.makeCompactGrid(loadMidsPanel, 1, 3, 0, 0, 5, 5);
			loadMidsPanel.setVisible(true);
		}
	}

	/**
	 * Creates the fastq panel.
	 */
	private void createFASTQPanel()
	{
			fastqPanel = new JPanel();
			fastqPanel.setLayout(new SpringLayout());
			fastqPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
		
			JButton openFile = new AcaciaGradientButton("Browse...",TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
			openFile.setActionCommand(FIND_FASTQ);
			openFile.addActionListener(this);
			this.setDimensionsToDefault(openFile);
			
			this.fastqFileChoice = new JTextField("");
			setTextFieldSize(this.fastqFileChoice);
			
			JLabel selectFASTQ = new JLabel("Select FASTQ file:");
			selectFASTQ.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			
			this.adjustLabel(selectFASTQ);
			this.setDimensionsToDefault(selectFASTQ);
			
			fastqPanel.add(selectFASTQ);
			fastqPanel.add(fastqFileChoice);
			fastqPanel.add(openFile);

			SpringUtilities.makeCompactGrid(fastqPanel, 1,3, 0, 0, 5, 5);
	}
	
	/**
	 * Creates the fasta panel.
	 */
	private void createFASTAPanel()
	{
			this.fastaPanel = new JPanel();
			this.fastaPanel.setLayout(new SpringLayout());
			this.fastaPanel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			
			//FASTA FILE
			JButton openFileFasta = new AcaciaGradientButton("Browse...",TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2,TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
			openFileFasta.setActionCommand(FIND_FASTA);
			openFileFasta.addActionListener(this);
			
			this.fastaFileChoice = new JTextField("");
			setTextFieldSize(this.fastaFileChoice);
			
			JLabel selectFASTA = new JLabel("Select FASTA file:");
			selectFASTA.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			this.adjustLabel(selectFASTA);
			this.setDimensionsToDefault(selectFASTA);
			
			JButton openFileQual = new AcaciaGradientButton("Browse...", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
			openFileQual.setActionCommand(FIND_QUAL);
			openFileQual.addActionListener(this);
			
			this.qualFileChoice = new JTextField("");
			this.setTextFieldSize(this.qualFileChoice);

			JLabel selectQUAL = new JLabel("Select Quality file:");
			selectQUAL.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
			this.adjustLabel(selectQUAL);
			this.setDimensionsToDefault(selectQUAL);
		
			fastaPanel.add(selectFASTA);
			fastaPanel.add(fastaFileChoice);
			fastaPanel.add(openFileFasta);
			fastaPanel.add(selectQUAL);
			fastaPanel.add(qualFileChoice);
			fastaPanel.add(openFileQual);
			SpringUtilities.makeCompactGrid(fastaPanel, 2, 3, 5, 5, 5, 5);				
	}
	
	/**
	 * Sets the text field size.
	 *
	 * @param component the new text field size
	 */
	public void setTextFieldSize(JTextField component)
	{
		Dimension preferredSize = component.getPreferredSize();
		preferredSize.width = TagInputPanel.DEFAULT_TEXTFIELD_MAX_WIDTH;;
		preferredSize.height = TagInputPanel.DEFAULT_PREFERRED_FIELD_HEIGHT;
		
		component.setSize(preferredSize);
		component.setPreferredSize(preferredSize);
		component.setMaximumSize(preferredSize);
	}
	
	/**
	 * Gets the file choice.
	 *
	 * @param parent the parent
	 * @param linkedField the linked field
	 * @param selectionMode the selection mode
	 * @return the file choice
	 */
	private String getFileChoice(JPanel parent, JTextField linkedField, int selectionMode)
	{
		try
		{
			File f = null;
			
			if(this.lastDir != null)
				f = new File(new File(this.lastDir).getCanonicalPath());
			
			final JFileChooser fc = new JFileChooser(f);
			fc.setFileSelectionMode(selectionMode);
			
		    int returnVal = fc.showOpenDialog(parent);
	
		    String retVal = null;
		    
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
		    {
		                File file = fc.getSelectedFile();
		                retVal = file.getAbsolutePath();		
		    
		    
		                if(retVal.equals(""))
		                {
		                	return linkedField.getText();
		                }
		    
		                this.lastDir = retVal; //store it for next time, to pre-empt stuff;
		                return retVal;
		    }
		}
		catch(IOException ie)
		{
			
		}
		return linkedField.getText(); //an error occurs, return the previous text.
	}
	
	
	/**
	 * The Class BlankSpaceFilter.
	 */
	public class BlankSpaceFilter extends DocumentFilter
	{
		
		/** The error label. */
		JLabel errorLabel;
		
		/** The parent. */
		TagInputPanel parent;
		
		/**
		 * Instantiates a new blank space filter.
		 *
		 * @param errorLabel the error label
		 * @param parent the parent
		 */
		public BlankSpaceFilter (JLabel errorLabel, TagInputPanel parent)
		{
			super();
			this.errorLabel = errorLabel;
			this.parent = parent;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException 
		{
			boolean containsWhiteSpace = false;
			
			for(int i = 0; i < string.length(); i++)
			{
				if(Character.isWhitespace(string.charAt(i)))
				{
					containsWhiteSpace = true;
					break;
				}
			}
			
			if(containsWhiteSpace)
				errorLabel.setText("No white-space characters are allowed");
			else
				errorLabel.setText("");
			
			parent.revalidate();
			fb.insertString(offset, string.trim(), attr);  
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length,  
		String string, AttributeSet attr) throws BadLocationException 
		{
			boolean containsWhiteSpace = false;
			for(int i = 0; i < string.length(); i++)
			{
				if(Character.isWhitespace(string.charAt(i)))
				{
					containsWhiteSpace = true;
					break;
				}
			}
			
			if(containsWhiteSpace)
				errorLabel.setText("No white-space characters are allowed");
			else
				errorLabel.setText("");
			
			this.parent.revalidate();
			
			fb.remove(offset, length);
			fb.insertString(offset, string.trim(), attr);
		}
	}
	
	/**
	 * The Class NonNumericFilter.
	 */
	public class NonNumericFilter extends DocumentFilter
	{
		
		/** The error label. */
		JLabel errorLabel;
		
		/** The parent. */
		TagInputPanel parent;
		
		/**
		 * Instantiates a new non numeric filter.
		 *
		 * @param errorLabel the error label
		 * @param parent the parent
		 */
		public NonNumericFilter (JLabel errorLabel, TagInputPanel parent)
		{
			super();
			this.errorLabel = errorLabel;
			this.parent = parent;
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException 
		{
			boolean containsNonNumeric = false;
			
			int pos = 0;
			
			for(;pos < string.length(); pos++)
			{
				if(! Character.isDigit(string.charAt(pos)))
				{
					containsNonNumeric = true;
					break;
				}
			}
			String modifiedValue = null;
			
			if(containsNonNumeric)
			{
				errorLabel.setText("Trim length should be a number.");
				modifiedValue = string.substring(0, pos); //does not include pos
				this.parent.revalidate();
			}
			else
			{
				errorLabel.setText("");
				modifiedValue = string;
			}

			fb.insertString(offset, modifiedValue, attr);  
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
		 */
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length,  
		String string, AttributeSet attr) throws BadLocationException 
		{
			boolean containsNonNumeric = false;
			int pos = 0;
			
			for(;pos < string.length(); pos++)
			{
				if(! Character.isDigit(string.charAt(pos)))
				{
					containsNonNumeric = true;
					break;
				}
			}
			String modifiedValue = null;
			
			if(containsNonNumeric)
			{
				errorLabel.setText("Trim length should be a number.");
				modifiedValue = string.substring(0, pos); //does not include pos
				this.parent.revalidate();
			}
			else
			{
				errorLabel.setText("");
				
				//this is different, someone has selected stuff, and then started typing...
				
				modifiedValue = string;
			}
	
			fb.remove(offset, length);				
			fb.insertString(offset, modifiedValue, attr);  
		}
	}
	
	/**
	 * Creates the image panel.
	 *
	 * @param path the path
	 * @param background the background
	 * @param description the description
	 * @return the j panel
	 */
	private JPanel createImagePanel(String path, Color background, String description) 
	{
		URL imgURL = getClass().getResource(path);
		
		System.out.println(imgURL.getPath());
	
		if (imgURL != null) 
		{
				ImageIcon image = new ImageIcon(imgURL, description);
				JLabel label = new JLabel("", image, JLabel.CENTER);
				Dimension sizeOfIcon = new Dimension(image.getIconWidth(), image.getIconHeight());
				
				System.out.println("Size of icon " + sizeOfIcon.width + ", " + sizeOfIcon.height);
				
				label.setMinimumSize(sizeOfIcon);
				label.setPreferredSize(sizeOfIcon);
				label.setMaximumSize(sizeOfIcon);
				label.setSize(sizeOfIcon);

				JPanel panel = new JPanel(new BorderLayout());
				panel.add(label, BorderLayout.WEST);
				panel.setBackground(background);
		
				panel.setPreferredSize(label.getPreferredSize());
		//		panel.setMinimumSize(label.getPreferredSize());
		//		panel.setMinimumSize(label.getPreferredSize());
				panel.setSize(label.getPreferredSize());
				
				System.out.println("Size of panel is " + panel.getPreferredSize().width + ", " + panel.getPreferredSize().height);
				
				return panel; 
		} 
		else 
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	/**
	 * Creates the image button.
	 *
	 * @param path the path
	 * @param background the background
	 * @param description the description
	 * @return the j panel
	 */
	private JPanel createImageButton(String path, Color background, String description)
	{

		
		File f = new File(System.getProperty("user.dir"));
		
		URL imgURL = getClass().getResource(path);
		
	
		if (imgURL != null) 
		{
				ImageIcon image = new ImageIcon(imgURL, description);
				JButton imageButton = new JButton("", image);
				Dimension sizeOfIcon = new Dimension(image.getIconWidth(), image.getIconHeight());
				
				System.out.println("Size of icon " + sizeOfIcon.width + ", " + sizeOfIcon.height);
				
				imageButton.setMinimumSize(sizeOfIcon);
				imageButton.setPreferredSize(sizeOfIcon);
				imageButton.setMaximumSize(sizeOfIcon);
				imageButton.setSize(sizeOfIcon);

				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
				panel.add(imageButton, BorderLayout.CENTER);
				panel.setBackground(background);
				panel.setMaximumSize(imageButton.getPreferredSize());
				panel.setPreferredSize(imageButton.getPreferredSize());
				panel.setMinimumSize(imageButton.getPreferredSize());
				panel.setSize(imageButton.getPreferredSize());
				return panel; 
		} 
		else 
		{
			try
			{
				this.logger.writeLog("Couldn't find file: " + path, AcaciaLogger.LOG_ERROR);
			}
			catch(Exception e)
			{
				this.cleanExit("Error writing to log", e);
			}
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent arg0) 
	{
		this.numSelected.setText("Number selected: " + this.midTable.getSelectedRowCount());
		this.numSelected.repaint();
	}
	
	/**
	 * Clear interface.
	 */
	public void clearInterface()
	{
		this.enableInputMethods(true);

		this.projectName.setText("");
		this.outputDir.setText("");
		this.fastaFileChoice.setText("");
		this.fastqFileChoice.setText("");
		this.qualFileChoice.setText("");
		this.midsChoice.setText("");
		this.trimToLength.setText("");
		this.qualityThreshold.getModel().setValue(AcaciaConstants.DEFAULT_OPT_MIN_AVG_QUALITY);
		this.spinnerSignificance.getModel().setValue(AcaciaConstants.DEFAULT_OPT_SIGNIFICANCE_LEVEL);
		this.manhattanDistance.setText(AcaciaConstants.DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST);
		this.spinnerRepresentativeSeq.getModel().setValue(AcaciaConstants.DEFAULT_OPT_REPRESENTATIVE_SEQ);
		this.midSelection.setSelected(this.noMIDs.getModel(), true);
		
		CardLayout cl = (CardLayout)this.midCardPanel.getLayout();
		cl.show(this.midCardPanel, TagInputPanel.DONT_LOAD_USER_SUPPLIED_MIDS);
		this.clearMIDSPanel();
		
	}

	/**
	 * Worker finished.
	 */
	public void workerFinished()  
	{
		this.submit.setEnabled(true);
		this.cancel.setEnabled(false);
	}
}
