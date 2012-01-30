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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import core.TVRadar;

import data.*;
import data.guide.Genre;
import data.guide.Mapping;
import data.guide.Schedule;

/**
 * Shows the program grid where all program listings and channels are displayed
 * 
 * This object gets inserted into a MainGui object.  
 * 
 * @author Louis Amstutz
 */
public class GuidePane extends JScrollPane {
	private static final long serialVersionUID = 2814102167683401851L;
	
	/** Base sizes of grid and fonts */
	private static final int baseCellWidth = 100, baseCellHeight = 20, baseFontSize=12;
	
	/** Actual grid and font sizes, which are the base sizes multiplied by uiSize in options. */
	private int cellWidth, cellHeight, fontSize;
	
	/** Length in pixels of one day.  Used to determine the date of dateLabel */
	private int intervalSize;
	
	/** How many pixels behind start time the previous midnight would be.  Used to determine 
	 * the date of dateLabel */ 
	private int firstInterval;
	
	/** Distance in pixels from the start time to current time. Used to draw the current time
	 * marker and determine the initial scrollbar position. */
	private int currentTimePos;
	
	/** Time in millis between the start time and end time */
	private long timeSpan;
	
	/** True if listings are out of date (end time < current time) */
	private boolean outOfDate = false;
	
	/** Title of last program user right clicked on */
	private String currentProgramTitle="";
	
	/** Last channel user right clicked on */
	private Mapping currentMapping;
	
	/** Font to be used for all labels */
	private Font labelFont;
	
	/** Border used by all labels in the GuidePane */
	private Border border;
	
	/** Reference to the TVRadar object with the data, options, and favourites */
	private TVRadar tvRadar;
	
	/** Reference to the MainGui object that contains this */
	private MainGui mainGui;
	
	/////////////////////// UI Components /////////////////////////////
	private JLabel dateLabel;
	private JPanel programsPanel,  channelsPanel;
	private TimesPanel timesPanel;
	private JPopupMenu channelMenu, programMenu;
	private JMenuItem addChannelToFavoritesItem, addProgramToFavoritesItem, searchForProgramItem;
	
	private GuidePaneMouseListener gpMouseListener;
	private GuidePaneMouseMotionListener gpMouseMotionListener;
	private GuidePaneActionListener gpActionListener;

	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to TVRadar object
	 * @param _mg - Main Gui which the Guide Pane is inserted into
	 */
	public GuidePane(TVRadar _tvr, MainGui _mg) {
		super();
		tvRadar = _tvr;
		mainGui = _mg;
		
		init();
	}
	
	/**
	 * Initialises the GuidePane
	 */
	private void init() {
		
		border = BorderFactory.createLineBorder(Color.BLACK);
		
		timeSpan = tvRadar.getData().getStopTime() - tvRadar.getData().getStartTime();
		
		cellWidth = baseCellWidth * tvRadar.getOptions().getUiSize() / 100;
		cellHeight = baseCellHeight * tvRadar.getOptions().getUiSize() / 100;
		fontSize = baseFontSize * tvRadar.getOptions().getUiSize() / 100;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime > tvRadar.getData().getStopTime()) {
			outOfDate = true;
			currentTimePos = (int)((tvRadar.getData().getStopTime() - 7200000) * cellWidth / 1800000);
		}	
		else currentTimePos = (int)((currentTime - tvRadar.getData().getStartTime()) * cellWidth / 1800000);
			
		dateLabel = new JLabel();
		labelFont = new Font(dateLabel.getFont().getFontName(), dateLabel.getFont().getStyle(), fontSize);
		dateLabel.setFont(labelFont);
		dateLabel.setBorder(border);
		gpMouseListener = new GuidePaneMouseListener();
		gpMouseMotionListener = new GuidePaneMouseMotionListener();
		gpActionListener = new GuidePaneActionListener();
		
		createPopupMenus();
		
		createTimesPanel();
		createChannelsAndProgramsPanel();
		
		setViewportView(programsPanel);
		setColumnHeaderView(timesPanel);
		setRowHeaderView(channelsPanel);
		
		getHorizontalScrollBar().addMouseMotionListener(gpMouseMotionListener);
		getVerticalScrollBar().setUnitIncrement(16);

		setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, dateLabel);
		
		ToolTipManager.sharedInstance().setDismissDelay(120000);
		ToolTipManager.sharedInstance().setReshowDelay(0);
		ToolTipManager.sharedInstance().setInitialDelay(tvRadar.getOptions().getPopupDelay());
	}
	
	/**
	 * Functions as the column headings (time slots) for the GuidePane
	 * 
	 * JPanel was subclassed to allow for the painting of the current time marker
	 * 
	 * @author Louis Amstutz
	 */
	private class TimesPanel extends JPanel {
		private static final long serialVersionUID = -6925404890825458882L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (!outOfDate) {
				int [] xPoints = {currentTimePos-10, currentTimePos,currentTimePos+10}, yPoints = {0,25,0};
				g.setColor(Color.BLUE);
				g.fillPolygon(xPoints, yPoints, 3);
			}
		}
	}
	
	/**
	 * Create the times panel (time slot column headings)
	 */
	private void createTimesPanel() {
		timesPanel = new TimesPanel();
		timesPanel.setLayout(null); 
		
		int numTimeSlots = (int)(timeSpan/1800000);
		timesPanel.setPreferredSize(new Dimension(cellWidth*numTimeSlots,cellHeight));
		
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		for (int i=0; i<numTimeSlots; i++) {
			JLabel timeLabel = new JLabel(format.format(new Date(tvRadar.getData().getStartTime() + i*1800000)));
			timeLabel.setFont(labelFont);
			timeLabel.setBounds(i * cellWidth, 0, cellWidth, cellHeight );
			timeLabel.setBorder(border);
			timesPanel.add(timeLabel);
		}
	}
	
	/**
	 * This method creates both the channels panel (row headings on the left of the GuidePane)
	 * and the programs panel (main program grid).  Any channels that don't have any scheduled
	 * programs based on the current filters are omitted.  
	 */
	private void createChannelsAndProgramsPanel() {
		channelsPanel = new JPanel();
		channelsPanel.setLayout(null); 
		
		programsPanel = new JPanel();
		programsPanel.setLayout(null);
		
		int subtractedChannels = 0;
		boolean channelsFound = false;
		for (int i = 0; i<tvRadar.getData().getChannelMappings().size(); i++) {
			
			Mapping currentMapping = tvRadar.getData().getChannelMappings().get(i);
			if (mainGui.getCategoryIndex() != 1 || 
					tvRadar.getFavorites().getFavoriteChannels().contains(currentMapping) ) {
				
				ArrayList<Schedule> schedules = currentMapping.getChannel().getSchedules();
				boolean channelHasSchedules = false;
				for (int j=0; j<schedules.size(); j++) {
					if (drawSchedule(schedules.get(j), i-subtractedChannels)) 
						channelHasSchedules = true;
				}
				if (mainGui.getCategoryIndex() == 1);
				if (channelHasSchedules) {
					drawChannel(currentMapping, i-subtractedChannels);
					channelsFound = true;
				}
				else subtractedChannels++;
			}
			else subtractedChannels++;
		}
		
		if (!channelsFound) {
			JLabel errorLabel = new JLabel("No channels match criterea");
			errorLabel.setBounds(currentTimePos, 0, 200, 30);
			programsPanel.add(errorLabel);
		}
		
		int guideXSize = (int)(timeSpan/1800000*cellWidth);
		int guideYSize = (tvRadar.getData().getChannelMappings().size() - subtractedChannels) * cellHeight; 
		
		channelsPanel.setPreferredSize(new Dimension(cellWidth,guideYSize));
		programsPanel.setPreferredSize(new Dimension(guideXSize, guideYSize));
	}

	/**
	 * Draws a channel label
	 * 
	 * @param mapping - Mapping of channel to be drawn
	 * @param index - Index of channel
	 */
	private void drawChannel(Mapping mapping, int index) {
		ChannelLabel channelLabel = new ChannelLabel(mapping.toString(), mapping);
		channelLabel.setFont(labelFont);
		channelLabel.setBounds(0, index*cellHeight, cellWidth, cellHeight);
		channelLabel.setOpaque(true);
		channelLabel.setBackground(Color.LIGHT_GRAY);
		channelLabel.setToolTipText(mapping.toHtml(fontSize));
		channelLabel.setBorder(border);
		channelLabel.addMouseListener(gpMouseListener);
		
		channelsPanel.add(channelLabel);
	}
	
	/**
	 * Determines whether a schedule should be drawn (based on active filters), 
	 * what color it should be (based on genre and if it's a movie), and finally 
	 * draws the schedule label.
	 * 
	 * @param sched - Schedule to be drawn
	 * @param chanIndex - Index of channel which the schedule is on
	 * @return True if schedule was drawn, false if not.
	 */
	private boolean drawSchedule(Schedule sched, int chanIndex) {
		
		boolean isVisible = false;
		Color c = MainGui.getDefaultColor();
		int catIndex = mainGui.getCategoryIndex();
		
		if (catIndex < 3) isVisible = true;
		
		if (catIndex == 2 && !tvRadar.getFavorites().getFavoritePrograms().contains(
				sched.getProgram().getTitle())) return false;

		if (mainGui.getSearchString() != null) {
			String progTitle = sched.getProgram().getTitle().toLowerCase();
			String searchTitle = mainGui.getSearchString().toLowerCase();
			if (progTitle.indexOf(searchTitle) == -1) return false;
		}
		
		if (sched.getProgram().getMovie()) {
			if (catIndex == 3) isVisible = true;
			c = MainGui.getMovieColor();
		}
		else {
			Genre bestGenre = sched.getProgram().getBestGenre();
			if (bestGenre != null) {
				for (int i=0; i<MainGui.getSubCategories().length; i++) {
					for (int j=0; j<MainGui.getSubCategories()[i].length; j++) {
						if (bestGenre.getGenre().equalsIgnoreCase(MainGui.getSubCategories()[i][j]))
							c = MainGui.getCategoryColors()[i];
						if (sched.getProgram().hasGenre(MainGui.getSubCategories()[i][j]) && catIndex == i+4)
							isVisible = true;
					}
				}
			}
		}
		
		if (isVisible) {
			if (sched.getStop() < tvRadar.getData().getStartTime() ||
					sched.getStart() > tvRadar.getData().getStopTime()) return false;
			
			long adjustedStart=sched.getStart(), adjustedStop=sched.getStop();
			if (adjustedStart < tvRadar.getData().getStartTime()) 
				adjustedStart = tvRadar.getData().getStartTime();
			if (adjustedStop > tvRadar.getData().getStopTime()) 
				adjustedStop = tvRadar.getData().getStopTime();
			
			int startPosX = (int)(cellWidth * (adjustedStart - 
					tvRadar.getData().getStartTime()) / 1800000);
			int startPosY = chanIndex * cellHeight;
			int stopPosX = (int)(cellWidth * (adjustedStop - 
					tvRadar.getData().getStartTime()) / 1800000);
			
			JLabel programLabel = new JLabel(sched.toString());
			programLabel.setFont(labelFont);
			programLabel.setBounds(startPosX, startPosY, stopPosX - startPosX, cellHeight);
			programLabel.setOpaque(true);
			programLabel.setToolTipText(sched.toHtml(tvRadar.getOptions().getShowCrew(), fontSize));
			programLabel.setBackground(c);
			programLabel.setBorder(border);
			
			programLabel.addMouseListener(gpMouseListener);
			programsPanel.add(programLabel);
		}
		
		return isVisible;
	}
	
	/**
	 * Mouse Listener for GuidePane
	 * @author Louis Amstutz
	 */
	private class GuidePaneMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (e.getSource() instanceof ChannelLabel) {
					currentMapping = ( (ChannelLabel)e.getComponent() ).mapping;
					channelMenu.show(e.getComponent(), e.getX(), e.getY());
				}
				else if (e.getSource() instanceof JLabel) {
					currentProgramTitle = ((JLabel)e.getComponent()).getText();
					programMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}
	
	/**
	 * Mouse Motion Listener for GuidePane
	 * @author Louis Amstutz
	 */
	private class GuidePaneMouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getSource() == getHorizontalScrollBar()) {
				updateDateLabel();
			}
		}
	}

	/**
	 * Action Listener for GuidePane
	 * @author Louis Amstutz
	 */
	private class GuidePaneActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addChannelToFavoritesItem) {
				addChannelToFavorites(currentMapping);
			}
			else if (e.getSource() == addProgramToFavoritesItem) {
				addProgramToFavorites(currentProgramTitle);
			}
			else if (e.getSource() == searchForProgramItem) {
				searchProgram(currentProgramTitle);
			}
		}
	}

	/**
	 * Adds a channel to favorites
	 * 
	 * @param channel - Channel to add
	 */
	private void addChannelToFavorites(Mapping mapping) {
		tvRadar.getFavorites().addChannel(mapping);
		Favorites.saveFavorites(tvRadar.getFavorites());
	}
	
	/**
	 * Adds a program to favorites
	 * 
	 * @param programTitle - Title of program to add
	 */
	private void addProgramToFavorites(String programTitle) {
		tvRadar.getFavorites().addProgram(programTitle);
		Favorites.saveFavorites(tvRadar.getFavorites());
	}
	
	/**
	 * Creates the popup menus for when user right clicks a channel or program.
	 */
	private void createPopupMenus() {
		channelMenu = new JPopupMenu();
		addChannelToFavoritesItem = new JMenuItem("Add to favorites");
		addChannelToFavoritesItem.addActionListener(gpActionListener);
		channelMenu.add(addChannelToFavoritesItem);
		
		programMenu = new JPopupMenu();
		addProgramToFavoritesItem = new JMenuItem("Add to favorites");
		addProgramToFavoritesItem.addActionListener(gpActionListener);
		searchForProgramItem = new JMenuItem("Search for this program");
		searchForProgramItem.addActionListener(gpActionListener);
		programMenu.add(addProgramToFavoritesItem);
		programMenu.add(searchForProgramItem);
	}
	
	
	/**
	 * Searches for a program.
	 * 
	 * When the search is complete, the GuidePane will only show the channels that have this program
	 * 
	 * @param programTitle - Title of program to search for
	 */
	private void searchProgram(String programTitle) {
		mainGui.searchPrograms(programTitle);
	}
	
	/**
	 * Calculate intervalSize and firstInterval, then update the horizontal scroll bar and dateLabel
	 */
	public void calculateDayIntervals() {
		if (timeSpan == 0) return;
		
		int scrollBarSize = this.getHorizontalScrollBar().getMaximum();
		intervalSize = (int)((long)scrollBarSize * 86400000/ timeSpan);
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(tvRadar.getData().getStartTime());
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		firstInterval = (int)(scrollBarSize * (cal.getTimeInMillis() - 
				tvRadar.getData().getStartTime()) / timeSpan);
		updateDateLabel();
	}
	
	/**
	 * Update date label based on the current position of the horizontal scroll bar
	 */
	private void updateDateLabel() {
		long time;
		
		if (intervalSize == 0) time = tvRadar.getData().getStartTime();
		else time = tvRadar.getData().getStartTime() + ((this.getHorizontalScrollBar().getValue()
			- firstInterval) / intervalSize * 86400000);
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd");
		dateLabel.setText(format.format(new Date(time)));
	}
	
	/**
	 * Sets the horizontal scroll bar to the current time
	 */
	public void scrollToCurrentTime() {
		getHorizontalScrollBar().setValue(currentTimePos - cellWidth);
	}
	
	/**
	 * Extension of JLabel which contains a Channel object
	 * 
	 * @author Louis Amstutz
	 */
	private class ChannelLabel extends JLabel {
		private static final long serialVersionUID = -3071188628091121249L;
		
		public Mapping mapping;

		public ChannelLabel(String text, Mapping _mapping) {
			super(text);
			mapping = _mapping;
		}
	}
}
