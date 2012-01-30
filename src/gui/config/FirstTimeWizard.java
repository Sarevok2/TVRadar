/**
 * TV Radar
 * Copyright 2009-2010 Louis Amstutz (louis34@gmail.com)
 * 
 *   This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gui.config;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.MatteBorder;

import core.TVRadar;
import gui.MainGui;
import data.Options;

/**
 * Dialog that opens the first time the user launch the program (When there
 * is no options file).  It prompts the user to choose and configure a grabber.
 * 
 * @author Louis Amstutz
 */
public class FirstTimeWizard extends JDialog implements XmltvConfigContainerDialog {
	private static final long serialVersionUID = -2466922833979996123L;

	private static final String WELCOME_MESSAGE = "Welcome to " + TVRadar.getProgramName() + "!";
	
	private static final String sep = System.getProperty("file.separator");
	
	private static final String LOGO_FILENAME = "resources" + sep + "icons" + sep + "tvradar_100.gif";
	
	private Options options;
	
	private BufferedImage logo;
	
	/** Set to true if user clicks the exit button, letting the calling class
	 * know to exit the program */
	private boolean exitProgram = false;
	
	private boolean xmltvFinished = false;
	
	private enum CurrentPanel  {WELCOME, GRABBER, XMLTV_CONFIG, SD_CONFIG};
	private CurrentPanel currentPanel = CurrentPanel.WELCOME;
	
	
	///////////////////UI Elements//////////////////////
	private JPanel mainPanel, bottomPanel, contentPanel, welcomePanel, grabberPanel;
	private XmltvConfigPanel xmltvPanel;
	private SDConfigPanel sdPanel;
	private JLabel grabberLabel, welcomeLabel, logoLabel;
	private JRadioButton xmltvButton, sdButton;
	private ButtonGroup grabberGroup;
	private JButton exitButton, backButton, nextButton;
	private CardLayout cardLayout;

	private FirstTimeActionListener faListener;

	/**
	 * Constructor
	 * 
	 * @param tvgui - main gui and parent class
	 * @param _options - options
	 */
	public FirstTimeWizard(MainGui mg, Options _options) {
		super(mg, "First Time Setup Wizard", true);
		options = _options;
		
		faListener = new FirstTimeActionListener();
		
		createContentPanel();
		createBottomPanel();
		createMainPanel();
		getContentPane().add(mainPanel);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing ( WindowEvent w ) {
				exitProgram = true;
				dispose();
			}
		});
		this.pack();
		this.setLocation(200, 200);
		this.setVisible(true);
	}
	
	public boolean isExitProgram() {return exitProgram;}
	
	/*
	 * Create the grabber panel
	 */
	private void createGrabberPanel() {
		grabberPanel = new JPanel();
		grabberPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		grabberLabel = new JLabel("Download source:");
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,0,5);
		grabberPanel.add(grabberLabel, c);
		
		sdButton = new JRadioButton("Schedules Direct (North America)");
		sdButton.setSelected(true);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		grabberPanel.add(sdButton, c);
		
		xmltvButton = new JRadioButton("XMLTV (Everywhere else)");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		grabberPanel.add(xmltvButton, c);
		
		grabberGroup = new ButtonGroup();
		grabberGroup.add(sdButton);
		grabberGroup.add(xmltvButton);
	}
	
	/**
	 * Create the Welcome panel, which is the first page of the wizard
	 */
	private void createWelcomePanel() {
		welcomePanel = new JPanel();
		//welcomePanel.setBorder(new EmptyBorder(10,10,10,10));
		//ridLayout gLayout = new GridLayout(2, 2, 20, 20);
		welcomePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		loadLogo();
		if (logo != null) {
			logoLabel = new JLabel(new ImageIcon(logo));
			//logoLabel.setVerticalAlignment(SwingConstants.TOP);
			//logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
			logoLabel.setBorder(new MatteBorder(0,0,0,1,Color.GRAY));
			logoLabel.setPreferredSize(new Dimension(120,100));
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.insets = new Insets(0,10,0,10);
			welcomePanel.add(logoLabel, c);
		}
		
		Font welcomeFont = new Font("Arial", Font.PLAIN, 17);
		
		welcomeLabel = new JLabel(WELCOME_MESSAGE);
		welcomeLabel.setFont(welcomeFont);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.insets = new Insets(0,10,00,130);
		welcomePanel.add(welcomeLabel, c);
		
		JLabel emptyLabel = new JLabel();
		emptyLabel.setPreferredSize(new Dimension(50,100));
		emptyLabel.setBorder(new MatteBorder(0,0,0,1,Color.GRAY));
		emptyLabel.setPreferredSize(new Dimension(120,120));
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0,10,0,10);
		welcomePanel.add(emptyLabel, c);
	}
	
	/**
	 * Create the content panel, which has a CardLayout with all the pages of the wizard
	 */
	private void createContentPanel() {
		createWelcomePanel();
		createGrabberPanel();
		
		sdPanel = new SDConfigPanel(options);
		xmltvPanel = new XmltvConfigPanel(options, this, false);
		
		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		contentPanel.setLayout(new CardLayout());
		
		cardLayout = (CardLayout)contentPanel.getLayout();
		
		contentPanel.add(welcomePanel, "welcome");
		contentPanel.add(grabberPanel, "grabber");
		contentPanel.add(xmltvPanel, "xmltv");
		contentPanel.add(sdPanel, "sd");
	}
	
	/**
	 * Create the bottom panel with the OK and exit buttons
	 */
	private void createBottomPanel() {
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		nextButton = new JButton("Next");
		nextButton.setPreferredSize(new Dimension(75,26));
		nextButton.addActionListener(faListener);
		bottomPanel.add(nextButton);
		
		backButton = new JButton("Back");
		backButton.setPreferredSize(new Dimension(75,26));
		backButton.addActionListener(faListener);
		backButton.setEnabled(false);
		bottomPanel.add(backButton);
		
		exitButton = new JButton("Exit");
		exitButton.setPreferredSize(new Dimension(75,26));
		exitButton.addActionListener(faListener);
		bottomPanel.add(exitButton);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(bottomPanel);
	}
	
	/**
	 * Action Listener for FirstTimeDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class FirstTimeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == exitButton) {
				exitProgram = true;
				xmltvPanel.cancelConfig();
				dispose();
			}
			else if (e.getSource() == nextButton) {
				next();
			}
			else if (e.getSource() == backButton) {
				back();
			}
		}
	}
	
	/**
	 * Called when user clicks the next button
	 */
	private void next() {
		if (currentPanel == CurrentPanel.WELCOME) {
			currentPanel = CurrentPanel.GRABBER;

			backButton.setEnabled(true);
			cardLayout.next(contentPanel);
		}
		else if (currentPanel == CurrentPanel.GRABBER){
			if (sdButton.isSelected()) {
				currentPanel = CurrentPanel.SD_CONFIG;
				cardLayout.show(contentPanel, "sd");
				nextButton.setText("Finish");
			}
			else {
				currentPanel = CurrentPanel.XMLTV_CONFIG;
				cardLayout.show(contentPanel, "xmltv");
			}
		}
		else if (currentPanel == CurrentPanel.SD_CONFIG) {
			if (sdPanel.checkAndSetOptions()) {
				saveOptions();
				dispose();
			}
		}
		else if (currentPanel == CurrentPanel.XMLTV_CONFIG) {
			if (!xmltvFinished) {
				xmltvPanel.configXmltv();
				nextButton.setEnabled(false);
			}
			else {
				saveOptions();
				dispose();
			}
		}
	}
	
	/**
	 * Called when user clicks the back button
	 */
	private void back() {
		if (currentPanel == CurrentPanel.GRABBER) {
			currentPanel = CurrentPanel.WELCOME;
			backButton.setEnabled(false);
			cardLayout.previous(contentPanel);
		}
		else if (currentPanel == CurrentPanel.SD_CONFIG) {
			currentPanel = CurrentPanel.GRABBER;
			cardLayout.show(contentPanel, "grabber");
			nextButton.setText("Next");
		}
		else if (currentPanel == CurrentPanel.XMLTV_CONFIG) {
			xmltvPanel.cancelConfig();
			currentPanel = CurrentPanel.GRABBER;
			cardLayout.show(contentPanel, "grabber");
			xmltvPanel.reset();
			xmltvFinished = false;
			nextButton.setText("Next");
			nextButton.setEnabled(true);
		}
	}
	
	/**
	 * Loads the large TV Radar logo for the welcome screen
	 */
	private void loadLogo() {
		try {logo = ImageIO.read(new File(LOGO_FILENAME));} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Saves the options after user has chosen a grabber
	 */
	private void saveOptions() {
		try {
			options.initOptions();
			//TODO: Need to save more options
			if (sdButton.isSelected()) options.setGrabber(Options.GrabberType.SD);
			else options.setGrabber(Options.GrabberType.XMLTV);
			options.initOptions();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			exitProgram = true;
		}
	}

	@Override
	public void xmltvConfigFinished() {
		nextButton.setText("Finish");
		nextButton.setEnabled(true);
		xmltvFinished = true;
	}
}
