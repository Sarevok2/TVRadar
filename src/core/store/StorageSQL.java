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

package core.store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import core.misc.SQLInterface;
import data.guide.*;

/**
 * Stores a GuideData object in a MySQL database
 * 
 * @author Louis Amstutz
 */
public class StorageSQL implements StorageInterface {
	private SQLInterface si;
	private String mySqlUrl=null, mySqlPassword=null;
	
	/** If true then divide the SQL INSERT commands into several smaller INSERTs, which may
	 * be necessary if the max_allowed_packet variable is set too low on the server */
	private boolean smallerPackets;
	
	private long start, stop;
	
	private List<Mapping> mappings;
	private List<Channel> channels = new ArrayList<Channel>();
	private List<Schedule> schedules = new ArrayList<Schedule>();
	private List<Program> programs = new ArrayList<Program>();
	private List<Genre> genres = new ArrayList<Genre>();
	private List<Crew> crew = new ArrayList<Crew>();
	private List<String> advisories = new ArrayList<String>();
	private List<String> advisoryProgIDs = new ArrayList<String>();
	
	/**
	 * Constructor
	 * 
	 * @param _mySqlUrl - URL of the MySQL server
	 * @param pwd - Password to login to the server (username is a constant in SQLInterface)
	 * @param _smallerPackets - Whether to divide commands into smaller commands
	 */
	public StorageSQL(String _mySqlUrl, String pwd, boolean _smallerPackets) {
		mySqlUrl = _mySqlUrl;
		mySqlPassword = pwd;
		smallerPackets = _smallerPackets;
	}
	
	@Override
	public void store(GuideData data, StorageWorker worker) throws SQLException {
		try {
			buildLists(data, worker);
			initDatabase();
			insertIntoDatabase(worker);
		}
		finally {si.disconnect();}
	}
	
	/**
	 * Connect to the database and clear out the existing data
	 * 
	 * @throws SQLException
	 */
	public void initDatabase() throws SQLException {
		si = new SQLInterface();
		si.connect(SQLInterface.SQL_USERNAME, mySqlPassword, mySqlUrl);
		si.selectDatabase();
		si.clearTables();
	}
	
	/**
	 * Separates the data in the GuideData object into separate ArrayLists for each class, so
	 * that they can be stored in the SQL database.
	 * 
	 * @param data - Source of the data
	 * @param worker - parentStorageWorker
	 * @throws SQLException
	 */
	public void buildLists(GuideData data, StorageWorker worker) throws SQLException{
		start = data.getStartTime();
		stop = data.getStopTime();
		
		mappings = data.getChannelMappings();
		
		for (int i=0; i<mappings.size(); i++) {
			Channel chan = mappings.get(i).getChannel();
			if (chan != null && !channels.contains(chan)) channels.add(chan);
		}
		
		int numChannels = channels.size();
		
		worker.setProgressPublic(5);
		
		for (int i=0; i<numChannels; i++) {
			schedules.addAll(channels.get(i).getSchedules());
		}
		
		for (int i=0; i<schedules.size(); i++) {
			if (worker.isCancelled()) {
				cancelStore();
				throw new SQLException("User cancelled");
			}
			
			Program currentProg = schedules.get(i).getProgram();
			if (!programs.contains(currentProg)) {
				programs.add(currentProg);
				for (int j=0; j<currentProg.getGenres().size(); j++) {
					genres.add(currentProg.getGenres().get(j));
				}
				for (int j=0; j<currentProg.getCrew().size(); j++) {
					crew.add(currentProg.getCrew().get(j));
				}
				for (int j=0; j<currentProg.getAdvisories().size(); j++) {
					advisories.add(currentProg.getAdvisories().get(j));
					advisoryProgIDs.add(currentProg.getProgramId());
				}
			}
		}
		worker.setProgressPublic(10);
	}
	
	/**
	 * Inserts all the data into the database
	 * 
	 * @param worker - parent StorageWorker
	 * @throws SQLException
	 */
	public void insertIntoDatabase(StorageWorker worker) throws SQLException {
		si.setStartEndTimes(start, stop);
		
		si.insertChannels(channels);
		
		si.insertMappings(mappings);
		
		worker.setProgressPublic(20);
		
		if (smallerPackets)	si.insertProgramsDivided(programs);
		else si.insertPrograms(programs);
		
		worker.setProgressPublic(40);
		
		if (worker.isCancelled()) {
			cancelStore();
			throw new SQLException("User cancelled");
		}

		if (smallerPackets)	si.insertSchedulesDivided(schedules);
		else si.insertSchedules(schedules);
		
		worker.setProgressPublic(60);
		
		if (worker.isCancelled()) {
			cancelStore();
			throw new SQLException("User cancelled");
		}
		
		if (smallerPackets)	si.insertCrewDivided(crew);
		else si.insertCrew(crew);
		
		worker.setProgressPublic(80);
		
		if (worker.isCancelled()) {
			cancelStore();
			throw new SQLException("User cancelled");
		}

		if (smallerPackets)	si.insertGenresDivided(genres);
		else si.insertGenres(genres);
		
		worker.setProgressPublic(95);

		if (smallerPackets)	si.insertAdvisoriesDivided(advisories, advisoryProgIDs);
		else si.insertAdvisories(advisories, advisoryProgIDs);
		
		worker.setProgressPublic(100);
	}
	
	/**
	 * Called when the user cancels the storage operation
	 * Clears all data from tables and disconnects
	 */
	public void cancelStore(){
		try {si.clearTables();}
		catch (SQLException e) {System.out.println(e.toString());}
		si.disconnect();
	}
}
