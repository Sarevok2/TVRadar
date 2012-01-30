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

package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gui.config.*;
import core.TVRadar;
import data.*;

/**
 * Main GUI
 * 
 * This is the central gui window of the program.
 * 
 * The guide pane gets inserted into this.  There are buttons to launch
 * the options and favorites dialogs and a button to download the listings.
 * There are also controls to change what listings are showing.
 * 
 * @author Louis Amstutz
 */
public class MainGui extends JFrame {
	private static final long serialVersionUID = 5989031860136080880L;

	/** The options for categoryChooser.  Includes the top level genres. */
	private static final String[] CATEGORIES = {"All", "Favorite Channels", "Favorite Shows", 
		"Movies", "Sports", "News", "Educational", "Sci-Fi/Fantasy", "Comedy", "Animation"};
	
	/** Most of the genres that can appear in the XML listings from the grabbers.  They are divided
	 * by the top level genres in CATEGORIES.  Some genres are not included if they don't fit into
	 * one of the top level genres.  These are treated as generic programs
	 */
	private static final String[][] SUB_CATEGORIES = {{"sports", "sports event", "Sports non-event", 
		"Action sports", "Pro wrestling", "Soccer", "Sports talk", "Hunting", "Basketball", "hockey", 
		"baseball", "Cricket", "skiing", "Mixed martial arts", "Golf", "Football", "fishing", 
		"Card games", "Poker", "motorsports", "Skateboarding", "outdoors"}, {"news", "newsmagazine", 
		"weather", "public affairs", "Bus./financial", "politics"}, {"documentary", "educational", 
		"history", "health", "how-to", "biography", "science", "travel", "Cooking", "Home improvement", 
		"Animals", "nature"}, {"science fiction", "paranormal", "fantasy"}, {"comedy", "sitcom", 
		"Standup"}, {"animated"}};

	/** The colors of program labels depending on which of the top level genres they fit into */
	private static final Color[] CATEGORY_COLORS = {Color.YELLOW, Color.ORANGE.darker(), Color.MAGENTA, 
		Color.RED, Color.GREEN, Color.WHITE};
	
	/** The default color of a program.  Programs that don't have genres or are of genres that aren't
	 * in SUB_CATEGORIES are this color.
	 */
	private static final Color DEFAULT_COLOR=Color.WHITE;

	/** The color of movies */
	private static final Color MOVIE_COLOR=Color.CYAN;
	
	private String searchString = null;
	
	/** Whether the categoryChooser action lister should redraw the program guide.  It is 
	 * temporarily set to false when the the category index is automatically reset so the 
	 * action listenerdoesn't redraw the guide in this case */
	private boolean catChooserRedrawGuide = true;
	
	/** Set to true if the program is in the process of storing listings.  If the user tries
	 * to close the app at this time a warning comes up. */
	private boolean storageInProgress = false;
	
	private TVRadar tvRadar;
	
	//*******UI Components************//
	private GuidePane guidePane;
	private JPanel mainPanel, controlsPanel;
	private JButton grabListingsButton, optionsButton, favoritesButton, searchButton;
	private JComboBox categoryChooser;
	private JTextField searchField;
	
	private TVGuiActionListener tvgListener;
	
	
	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to the TVRadar object
	 */
	public MainGui(TVRadar _tvr) {
		super(TVRadar.getProgramName() + " " + TVRadar.getProgramVersion());
		
		tvRadar = _tvr;
		
		tvgListener = new TVGuiActionListener();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		getContentPane().add(mainPanel);
		
		createControlsPanel();
		mainPanel.add(controlsPanel);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing ( WindowEvent w ) {
				exitProgramIfOK();
			}
		});
		
		setIconImage(tvRadar.getIcon());
		setSize(1200,750);
		setExtendedState(MAXIMIZED_BOTH);	
		setVisible(true);
	}
	
	public static String[] getCategories() {return CATEGORIES;}
	public static String[][] getSubCategories() {return SUB_CATEGORIES;}
	public static Color[] getCategoryColors() {return CATEGORY_COLORS;}
	public static Color getDefaultColor() {return DEFAULT_COLOR;}
	public static Color getMovieColor() {return MOVIE_COLOR;}
	
	/**
	 * Create the controls panel (the top panel)
	 */
	private void createControlsPanel() {
		controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.LINE_AXIS));
		
		//ensure that this panel will take all available horizontal space
		controlsPanel.setMinimumSize(new Dimension(5000, 30));
		controlsPanel.setMaximumSize(new Dimension(5000, 30));
		
		optionsButton = new JButton("Options");
		optionsButton.addActionListener(tvgListener);
		controlsPanel.add(optionsButton);
		optionsButton.setAlignmentX(Component.BOTTOM_ALIGNMENT);

		favoritesButton = new JButton("Favorites");
		favoritesButton.addActionListener(tvgListener);
		controlsPanel.add(favoritesButton);
		
		controlsPanel.add(Box.createRigidArea(new Dimension(20,0)));
		
		grabListingsButton = new JButton("Download Listings");
		grabListingsButton.addActionListener(tvgListener);
		controlsPanel.add(grabListingsButton);
		
		controlsPanel.add(Box.createHorizontalGlue());
		
		searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(150,28));
		searchField.setMaximumSize(new Dimension(200,28));
		searchField.setMinimumSize(new Dimension(60,28));
		searchField.addActionListener(tvgListener);
		controlsPanel.add(searchField);
		
		searchButton = new JButton("Search Programs");
		searchButton.addActionListener(tvgListener);
		controlsPanel.add(searchButton);
		
		controlsPanel.add(Box.createRigidArea(new Dimension(20,0)));
		
		categoryChooser = new JComboBox(CATEGORIES);
		categoryChooser.setMaximumSize(new Dimension(80,25));
		categoryChooser.setMinimumSize(new Dimension(80,25));
		categoryChooser.setSelectedIndex(0);
		categoryChooser.addActionListener(tvgListener);
		controlsPanel.add(categoryChooser);
	}
	
	/**
	 * Action Listener for the TVGui class
	 * 
	 * @author Louis Amstutz
	 */
	private class TVGuiActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == grabListingsButton) {
				openGrabListingsDialog();
				mainPanel.validate();
			}
			else if (e.getSource() == optionsButton) {
				openOptionsDialog();
			}
			else if (e.getSource() == favoritesButton) {
				openFavoritesDialog();
			}
			else if (e.getSource() == searchField) {
				if (searchField.getText() != "") 
					searchPrograms(searchField.getText());
			}
			else if (e.getSource() == searchButton) {
				if (searchField.getText() != "") 
					searchPrograms(searchField.getText());
			}
			else if (e.getSource() == categoryChooser) {
				searchString = null;
				if (tvRadar.getData() != null && catChooserRedrawGuide) addGuidePane();
			}
		}
	}
	
	public void setStorageInProgress(boolean value) {
		storageInProgress = value;
	}
	
	private void exitProgramIfOK() {
		if (storageInProgress) {
			int response = JOptionPane.showConfirmDialog(this, "Listings are being stored to disk.  " +
					"Are you sure you want to interrupt this?", "Warning", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) dispose();
		}
		else dispose();
	}
	
	/**
	 * Open options dialog
	 */
	private void openOptionsDialog() {
		new OptionsDialog(tvRadar, this);
	}
	
	/**
	 * Open favorites dialog
	 */
	private void openFavoritesDialog() {
		new FavoritesDialog(tvRadar.getFavorites(), this);
	}
	
	/**
	 * Open grab listings dialog and start grabbing listings
	 * Prompts the user for a password if necessary
	 */
	public void openGrabListingsDialog() {
		StringBuilder username = new StringBuilder(), password = new StringBuilder();
		
		if (tvRadar.getOptions().getGrabber() == Options.GrabberType.SD) tvRadar.getSDLogin(username, password);
		
		int response = JOptionPane.showConfirmDialog(this, "This will delete all current listings " +
				"and download new ones.  Are you sure?", "", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION) {
			resetCategoryChooser();
			new GrabDialog(tvRadar, this, username.toString(), password.toString());
		}
	}
	
	/**
	 * Adds the GuidePane, which shows the program grid
	 * 
	 * The GuidePane is removed and recreated every time something changes, such as new listings are
	 * downloaded or the user filters by a genre
	 */
	public void addGuidePane() {
		if (tvRadar.getData() != null && tvRadar.getData().getChannelMappings().size() > 0) {
			if (guidePane != null) mainPanel.remove(guidePane);
			guidePane = new GuidePane(tvRadar, this);
			mainPanel.add(guidePane);
			mainPanel.validate();
			guidePane.calculateDayIntervals();
			guidePane.scrollToCurrentTime();
		}
	}
	
	public String getSearchString() {return searchString;}
	
	/**
	 * Searches for a program
	 * The GuidePane is recreated only showing this program and only the channels that are showing it.
	 * 
	 * @param programTitle - Title of program to search for
	 */
	public void searchPrograms(String programTitle) {
		resetCategoryChooser();
		searchString = programTitle;
		if (tvRadar.getData() != null) addGuidePane();
	}
	
	/**
	 * Get the current selected index of category chooser
	 * @return The selected index
	 */
	public int getCategoryIndex() {return categoryChooser.getSelectedIndex();}
	
	/**
	 * reset the categoryChooser, temporarily setting catChooserRedrawGuide to false so the 
	 * GuidePane doesn't get redrawn when the action listener fires
	 */
	public void resetCategoryChooser() {
		catChooserRedrawGuide = false;
		categoryChooser.setSelectedIndex(0);
		catChooserRedrawGuide = true;
	}
}
