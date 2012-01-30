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

package core.load;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import core.misc.SQLInterface;
import data.guide.*;

/**
 * Loads TV listings from a MySQL database into a GuideData object
 * 
 * @author Louis Amstutz
 */
public class SQLLoader implements ListingLoader {
	private SQLInterface si;
	
	private ArrayList<Channel> channels;
	private ArrayList<Program> programs;
	
	private String mySqlPwd, mySqlUrl;
	private Program currentProgram = new Program();
	private GuideData data;
	private LoadListingsWorker loadWorker;
	
	/**
	 * Constructor
	 * 
	 * @param _mySqlPwd - Password for MySQL account (username is a constant in SQLInterface class)
	 * @param _mySqlUrl - URL for MySQL server
	 */
	public SQLLoader(String _mySqlPwd, String _mySqlUrl) {
		mySqlPwd = _mySqlPwd;
		mySqlUrl = _mySqlUrl;
	}
	
	@Override
	public GuideData loadData(LoadListingsWorker _worker) throws SQLException {
		loadWorker = _worker;
		channels = new ArrayList<Channel>();
		programs = new ArrayList<Program>();
		data = new GuideData();
		si = new SQLInterface();
		
		try {
			si.connect(SQLInterface.SQL_USERNAME, mySqlPwd, mySqlUrl);
			
			si.selectDatabase();
			
			loadStartEnd();
			loadWorker.setProgressPublic(2);
			loadChannels();
			loadMappings();
			loadPrograms();
			loadWorker.setProgressPublic(15);
			loadCrew();
			loadWorker.setProgressPublic(30);
			loadGenres();
			loadWorker.setProgressPublic(45);
			loadAdvisories();
			loadWorker.setProgressPublic(60);
			loadSchedules();
			loadWorker.setProgressPublic(90);
		}
		finally {si.disconnect();}
		
		return data;
	}
	
	/**
	 * Loads the Channels from the database
	 * 
	 * @throws SQLException
	 */
	private void loadChannels() throws SQLException {
		si.loadChannels();
		while (si.next()) {
			channels.add(si.getChannel());
			if (loadWorker.isCancelled()) return;
		}
		Collections.sort(channels);
	}
	
	/**
	 * Loads the Mappings from the database
	 * 
	 * @throws SQLException
	 */
	private void loadMappings() throws SQLException {
		data.setChannelMappings(new ArrayList<Mapping>());
		si.loadMappings();
		while (si.next()) {
			Mapping map = si.getMapping();
			Channel chan = Channel.findChannel(channels, map.getChanID());
	
			if (chan != null) {
				map.setChannel(chan);
				data.getChannelMappings().add(map);
			}
			
			if (loadWorker.isCancelled()) return;
		}
		Collections.sort(data.getChannelMappings());
	}
	
	/**
	 * Loads the programs from the database
	 * 
	 * @throws SQLException
	 */
	private void loadPrograms() throws SQLException {
		si.loadPrograms();
		while (si.next()) {
			programs.add(si.getProgram());
			if (loadWorker.isCancelled()) return;
		}
		Collections.sort(programs);
	}
	
	/**
	 * Loads the schedules from the database
	 * 
	 * Will not add any schedules that are not between the start time and stop time
	 * 
	 * @throws SQLException
	 */
	private void loadSchedules() throws SQLException {
		si.loadSchedules();
		while (si.next()) {
			Schedule currentSchedule = si.getSchedule();
			if (currentSchedule.getStart() < data.getStopTime() &&
				currentSchedule.getStop() > data.getStartTime()) {
				
				Program program = Program.findProgram(programs, currentSchedule.getProgId());
				Channel channel = Channel.findChannel(channels, currentSchedule.getChanId());
				if (program != null && channel != null){
					currentSchedule.setProgram(program);
					channel.addSchedule(currentSchedule);
				}
			}
			if (loadWorker.isCancelled()) return;
		}
	}
	
	/**
	 * Loads the crew from the database
	 * 
	 * @throws SQLException
	 */
	private void loadCrew() throws SQLException {
		si.loadCrew();
		while (si.next()) {
			Crew crew = si.getCrew();
			if (!crew.getProgId().equals(currentProgram.getProgramId()))
				currentProgram = Program.findProgram(programs, crew.getProgId());
			currentProgram.addCrew(crew);
			if (loadWorker.isCancelled()) return;
		}
	}
	
	/**
	 * Loads the genres into the database
	 * 
	 * @throws SQLException
	 */
	private void loadGenres() throws SQLException {
		si.loadGenres();
		while (si.next()) {
			Genre genre = si.getGenre();
			if (!genre.getProgId().equals(currentProgram.getProgramId())) {
				currentProgram.sortGenres();
				currentProgram = Program.findProgram(programs, genre.getProgId());
			}
			currentProgram.addGenre(genre);
			if (loadWorker.isCancelled()) return;
		}
		currentProgram.sortGenres();
	}
	
	/**
	 * Loads the advisories from the database
	 * 
	 * @throws SQLException
	 */
	private void loadAdvisories() throws SQLException {
		si.loadAdvisories();
		while (si.next()) {
			StringBuilder advisory = new StringBuilder();
			String progId = si.getAdvisory(advisory);
			if (!progId.equals(currentProgram.getProgramId()))
				currentProgram = Program.findProgram(programs, progId);
			currentProgram.addAdvisory(advisory.toString());
			if (loadWorker.isCancelled()) return;
		}
	}
	
	/**
	 * Loads the start end times from the database
	 * 
	 * @throws SQLException
	 */
	private void loadStartEnd() throws SQLException {
		if (si.loadStartEnd()){
			data.setStartTime(si.getStartTime());
			data.setStopTime(si.getStopTime());
		}
	}
	
	@Override
	public boolean dataExists() {
		if (mySqlPwd == null || mySqlUrl == null) return false;
		
		si = new SQLInterface();
		
		try {
			si.connect(SQLInterface.SQL_USERNAME, mySqlPwd, mySqlUrl);
			si.selectDatabase();
		} 
		catch (SQLException e) {return false;}
		finally {si.disconnect();}

		return true;
	}
}
