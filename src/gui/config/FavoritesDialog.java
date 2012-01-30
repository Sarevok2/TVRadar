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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import data.Favorites;
import data.guide.Mapping;

/**
 * Dialog to manage favorites
 * 
 * @author Louis Amstutz
 */
public class FavoritesDialog extends JDialog {
	private static final long serialVersionUID = -5391150759577860145L;

	private Favorites favorites;
	
	////////////////// UI Elements /////////////////////
	private JPanel mainPanel, contentPanel, bottomPanel;
	private JButton okButton, cancelButton, dropChannelsButton, dropProgramsButton;
	private JList channelList, programList;
	private DefaultListModel channelModel, programModel;
	private JScrollPane channelPane, programPane;
	private JLabel channelsLabel, programsLabel, infoLabel;
	
	private FavoritesActionListener faListener;
	
	
	/**
	 * Constructor
	 * 
	 * @param favs - favorites object that this dialog is managing
	 * @param _parent - Parent frame
	 */
	public FavoritesDialog(Favorites favs, JFrame _parent) {
		super(_parent, "Favorites", true);
		
		favorites = favs;
		
		faListener = new FavoritesActionListener();
		
		createContentPanel();
		createBottomPanel();
		createMainPanel();
		
		loadFavorites();
		getContentPane().add(mainPanel);
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Create the content panel
	 */
	private void createContentPanel() {
		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		contentPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		infoLabel = new JLabel("<html>To add programs and channels to favorites, right " +
				"click them in the <br>guide view</html>");
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,20,10);
		contentPanel.add(infoLabel, c);
		
		channelsLabel = new JLabel("Favorite Channels");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		contentPanel.add(channelsLabel, c);
		
		programsLabel = new JLabel("Favorite Programs");
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		contentPanel.add(programsLabel, c);
		
		channelModel = new DefaultListModel();
		channelList = new JList(channelModel);
		channelPane = new JScrollPane(channelList);
		channelPane.setPreferredSize(new Dimension(200,200));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(5,10,0,10);
		contentPanel.add(channelPane,c);
		
		programModel = new DefaultListModel();
		programList = new JList(programModel);
		programPane = new JScrollPane(programList);
		programPane.setPreferredSize(new Dimension(200,200));
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(5,10,0,10);
		contentPanel.add(programPane,c);
		
		dropChannelsButton = new JButton("Drop Channels");
		dropChannelsButton.addActionListener(faListener);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,10,10,10);
		contentPanel.add(dropChannelsButton, c);
		
		dropProgramsButton = new JButton("Drop Programs");
		dropProgramsButton.addActionListener(faListener);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,10,10,10);
		contentPanel.add(dropProgramsButton, c);
	}
	
	/**
	 * Create the bottom panel with the ok and cancel buttons
	 */
	private void createBottomPanel() {
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(faListener);
		cancelButton.setPreferredSize(new Dimension(75,26));
		bottomPanel.add(cancelButton);
		
		okButton = new JButton("Ok");
		okButton.addActionListener(faListener);
		okButton.setPreferredSize(new Dimension(75,26));
		bottomPanel.add(okButton);
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
	 * Action Listener for FavoritesDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class FavoritesActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == dropChannelsButton) {
				dropSelectedChannels();
			}
			else if (e.getSource() == dropProgramsButton) {
				dropSelectedPrograms();
			}
			else if (e.getSource() == cancelButton) {
				dispose();
			}
			else if (e.getSource() == okButton) {
				saveFavorites();
				dispose();
			}
		}
	}
			
	/**
	 * Remove the select channels from favorites
	 */
	private void dropSelectedChannels() {
		int selectedChans [] = channelList.getSelectedIndices();
		for (int i=selectedChans.length-1; i>=0; i--) {
			channelModel.removeElementAt(selectedChans[i]);
		}
	}
	
	/**
	 * Remove the selected programs from favorites
	 */
	private void dropSelectedPrograms() {
		int selectedProgs [] = programList.getSelectedIndices();
		for (int i=selectedProgs.length-1; i>=0; i--) {
			programModel.removeElementAt(selectedProgs[i]);
		}
	}
	
	/**
	 * Load favorites from disk
	 */
	private void loadFavorites() {
		for (int i=0; i<favorites.getFavoritePrograms().size(); i++) {
			programModel.addElement(favorites.getFavoritePrograms().get(i));
		}
		for (int i=0; i<favorites.getFavoriteChannels().size(); i++) {
			channelModel.addElement(favorites.getFavoriteChannels().get(i));
		}
	}
	
	/**
	 * Save favorites to disk
	 */
	private void saveFavorites() {
		favorites.getFavoriteChannels().clear();
		for (int i=0; i<channelModel.getSize(); i++) {
			favorites.getFavoriteChannels().add((Mapping)channelModel.elementAt(i));
		}
		
		favorites.getFavoritePrograms().clear();
		for (int i=0; i<programModel.getSize(); i++) {
			favorites.getFavoritePrograms().add((String)programModel.elementAt(i));
		}
		
		Favorites.saveFavorites(favorites);
	}
}
