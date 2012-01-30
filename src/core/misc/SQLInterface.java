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

package core.misc;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import data.guide.*;

/**
 * Provides an interface to the MySQL database
 * 
 * @author Louis Amstutz
 */
public class SQLInterface {
	
	public static final String SQL_USERNAME = "tvradaruser", SQL_DATABASE_NAME="tvradarlistings";
	public static final int ROWS_PER_SMALLER_PACKET = 3000;
	
	private Connection con;
	private ResultSet rSet;
	private Statement stmt;
	private PreparedStatement pstmt;
	  
	/**
	 * Connect to the database
	 * 
	 * @param username - MySQL username
	 * @param password - MySQL password
	 * @param url - URL of MySQL server
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 */
    public void connect(String username, String password, String url) throws SQLException {    	
    	try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
	    	con = DriverManager.getConnection(url,username, password);
	    	stmt = con.createStatement();
    	} catch (InstantiationException e) {
    		 throw new SQLException("Error initializing MySQL Driver", e);
		} catch (IllegalAccessException e) {
			throw new SQLException("Error initializing MySQL Driver", e);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Error initializing MySQL Driver", e);
		} catch (SQLException e) {
    		disconnect();
    		throw e;
    	}
    }
    
    /**
     * Disconnect from the database
     */
    public void disconnect() {
	    try {con.close();}
	    catch (SQLException e) {e.printStackTrace();}
    }
    
    /**
     * Initialize the database
     * 
     * Create the database and users, and give the users privileges on that database.
     * The users and database are deleted and recreated if they exist.
     * 
     * Note that 2 users are created, SQL_USERNAME@% and SQL_USERNAME@localhost.  This
     * is because depending on the MySQL version and config, the wild card host % may
     * not work for logging in locally.
     * 
     * @param password - Password to assign to the user
     * @throws SQLException
     */
    public void resetDatabase(String password) throws SQLException {
    	stmt.executeUpdate("DROP DATABASE IF EXISTS " + SQL_DATABASE_NAME + ";");
    	stmt.executeUpdate("CREATE DATABASE " + SQL_DATABASE_NAME + ";");
    	
    	try {stmt.executeUpdate("DROP USER " + SQL_USERNAME + "@localhost, " + SQL_USERNAME + ";");}
    	catch (SQLException e){}//Ignore the error, because it's most likely because the user didn't exist
    	
    	stmt.executeUpdate("CREATE USER " + SQL_USERNAME + "@localhost IDENTIFIED BY '" + password + "', " +
    			SQL_USERNAME + " IDENTIFIED BY '" + password + "';");

    	stmt.executeUpdate("USE " + SQL_DATABASE_NAME);
    	
    	stmt.executeUpdate("GRANT ALL ON *.* TO " + SQL_USERNAME + ", " + SQL_USERNAME + "@localhost");
    }
    
    /**
     * Creates all the tables
     * 
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        selectDatabase();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS startendtimes (startTime " +
        		"bigint, endTime bigint)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS channels (ChanTitle " +
        		"varchar(50), ChanId varchar(40), CallSign varchar(20), PRIMARY KEY(ChanId));");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS mappings (ChanNum int, " +
        		"ChanId varchar(40), PRIMARY KEY(ChanNum));");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS programs (ProgId varchar(40), " +
        		"title varchar(150), subtitle varchar(150), origAirDate varchar(10), " +
        		"rating varchar(10), year char(4), EpNo varchar(20), movie boolean, " +
        		"description varchar(1000), PRIMARY KEY(ProgId));");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS schedules (ChanId varchar(40), " +
        		"ProgId varchar(40), Rating varchar(5), Hdtv boolean, StartTime bigint, " +
        		"StopTime bigint, PRIMARY KEY(ChanId, StartTime));");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS crew (ProgId varchar(40), " +
        		"FName varchar(40), LName varchar(40), Role varchar(30));");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS genres (ProgId varchar(40), " +
        		"Genre varchar(40), Relevance int);");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS advisories (ProgId varchar(40), " +
        		"name varchar(40));");
    }
    
    /**
     * Sets the database as the current database in MySQL
     * 
     * @throws SQLException
     */
    public void selectDatabase() throws SQLException {
    	stmt.executeUpdate("USE " + SQL_DATABASE_NAME + ";");
    }
    
    /**
     * Clears all data from all tables
     * 
     * @throws SQLException
     */
    public void clearTables() throws SQLException {
		stmt.executeUpdate("DELETE FROM startendtimes;");
		stmt.executeUpdate("DELETE FROM channels;");
		stmt.executeUpdate("DELETE FROM mappings;");
		stmt.executeUpdate("DELETE FROM schedules;");
		stmt.executeUpdate("DELETE FROM programs;");
		stmt.executeUpdate("DELETE FROM crew;");
		stmt.executeUpdate("DELETE FROM genres;");
		stmt.executeUpdate("DELETE FROM advisories;");
    }
    
    /**
     * Sets the start and end times in millis of the span of the listings
     * 
     * @param start - Start time
     * @param stop = End time
     * @throws SQLException
     */
    public void setStartEndTimes(long start, long stop) throws SQLException {
		pstmt = con.prepareStatement("INSERT INTO startendtimes VALUES (?,?)");
		pstmt.setLong(1, start);
		pstmt.setLong(2, stop);
		pstmt.executeUpdate();
    }
    
    /**
     * Insert a single Channel
     * 
     * @param chan - channel to be inserted
     * @throws SQLException
     */
	public void insertChannel(Channel chan) throws SQLException {
		stmt.executeUpdate("INSERT INTO channels VALUES ('"  + chan.getTitle().replace("'", "''") +
				"', '" + chan.getChanId() + "', '" + chan.getCallSign() + "')");
	}
    
    /**
     * Insert a list of channels
     * 
     * This is done with one large INSERT statement
     * 
     * @param channels - Channels to be inserted
     * @throws SQLException
     */
	public void insertChannels(List<Channel> channels) throws SQLException {
		if (channels.size() == 0) return;
		
		StringBuilder command = new StringBuilder("INSERT INTO channels VALUES ");
    	
		for (int i=0; i<channels.size(); i++) {
			Channel chan = channels.get(i);
			command.append("('" + chan.getTitle().replace("'", "''") +
					"', '" + chan.getChanId() + "', '" + chan.getCallSign() + "'), ");
		}

		command.delete(command.length()-2, command.length()-1);
		command.append(";");

		stmt.executeUpdate(command.toString());
	}
    
    /**
    * Insert a single mapping
    * 
    * @param chan - mapping to be inserted
    * @throws SQLException
    */
   public void insertMapping(Mapping map) throws SQLException {
       stmt.executeUpdate("INSERT INTO mappings VALUES (" + map.getChanNum() + ", '" + 
    		   map.getChannel().getChanId() + "')");
   }
   
   /**
    * Insert a list of mappings
    * 
    * This is done with one large INSERT statement
    * 
    * @param mappings - Mappings to be inserted
    * @throws SQLException
    */
	public void insertMappings(List<Mapping> mappings) throws SQLException {
		if (mappings.size() == 0) return;
		
		StringBuilder command = new StringBuilder("INSERT INTO mappings VALUES ");
		
		for (int i=0; i<mappings.size(); i++) {
			Mapping map = mappings.get(i);
			command.append("(" + map.getChanNum() + ", '" + map.getChannel().getChanId() + "'), ");
		}
		
		command.delete(command.length()-2, command.length()-1);
		command.append(";");
		
		stmt.executeUpdate(command.toString());
	}
   
    /**
     * Insert a single Schedule
     * 
     * @param sched - Schedule to be inserted
     * @throws SQLException
     */
    public void insertSchedule(Schedule sched) throws SQLException {
		pstmt = con.prepareStatement("INSERT INTO schedules VALUES (?,?,?,?,?,?)");
		pstmt.setString(1, sched.getChanId());
		pstmt.setString(2, sched.getProgId());
		pstmt.setString(3, sched.getRating());
		pstmt.setBoolean(4, sched.getHdtv());
		pstmt.setLong(5, sched.getStart());
		pstmt.setLong(6, sched.getStop());
		pstmt.executeUpdate();
    }
    
    
    /**
     * Insert a list of schedules
     * 
     * This is done with one large INSERT statement
     * 
     * @param schedules
     * @throws SQLException
     */
    public void insertSchedules(List<Schedule> schedules) throws SQLException {
    	if (schedules.size() == 0) return;
    	
    	StringBuilder command = new StringBuilder("INSERT INTO schedules VALUES ");
    	
    	for (int i=0; i<schedules.size(); i++) {
    		Schedule sched = schedules.get(i);
    		command.append("('" + sched.getChanId() + "', '" + sched.getProgId() + "', '" + 
        		sched.getRating() + "', " + sched.getHdtv() + ", " + sched.getStart() + ", " +
        		sched.getStop() + "), ");
;    	}
    	
    	command.delete(command.length()-2, command.length()-1);
    	command.append(";");
    	
    	stmt.executeUpdate(command.toString());
    }
    
    /**
     * Insert a list of schedules, but use several INSERT statements of ROWS_PER_SMALLER_PACKET
     * rows.  This is to avoid getting a max_allowed_packet error
     * 
     * @param schedules
     * @throws SQLException
     */
    public void insertSchedulesDivided(List<Schedule> schedules) throws SQLException {
    	Iterator<Schedule> iterator;
    	for (iterator = schedules.iterator(); iterator.hasNext();) {
    		List<Schedule> smallerList = new ArrayList<Schedule>();
    		for (int i=0; i<ROWS_PER_SMALLER_PACKET && iterator.hasNext(); i++) {
    			smallerList.add(iterator.next());
    		}
    		insertSchedules(smallerList);
    	}
    }

    /**
     * Insert a single Program
     * 
     * @param sched - Schedule to be inserted
     * @throws SQLException
     */
    public void insertProgram(Program prog) throws SQLException {    	
    	pstmt = con.prepareStatement("INSERT INTO programs VALUES (?,?,?,?,?,?,?,?,?)");
    	pstmt.setString(1, prog.getProgramId());
        pstmt.setString(2, prog.getTitle());
        pstmt.setString(3, prog.getSubtitle());
        pstmt.setString(4, prog.getOriginalAirDate());
        pstmt.setString(5, prog.getRating());
        pstmt.setString(6, prog.getYear());
        pstmt.setString(7, prog.getEpisodeNo());
        pstmt.setBoolean(8, prog.getMovie());
        pstmt.setString(9, prog.getDescription());
        
        pstmt.executeUpdate();
    }
    
    /**
     * Insert a list of programs
     * 
     * This is done with one large INSERT statement
     * 
     * @param programs
     * @throws SQLException
     */
    public void insertPrograms(List<Program> programs) throws SQLException {
    	if (programs.size() == 0) return;
    	
    	StringBuilder command = new StringBuilder("INSERT INTO programs VALUES ");
    	
    	for (int i=0; i<programs.size(); i++) {
    		Program prog = programs.get(i);
    		command.append("('" + prog.getProgramId() + "', '" + prog.getTitle().replace("'", "''") +
    			"', '" + prog.getSubtitle().replace("'", "''") + "', '" + prog.getOriginalAirDate() +
    			"', '" + prog.getRating() + "', '" + prog.getYear() + "', '" + prog.getEpisodeNo() +
    			"', " + prog.getMovie() + ", '" + prog.getDescription().replace("'", "''") + "'), ");
;    	}
    	
    	command.delete(command.length()-2, command.length()-1);
    	command.append(";");
    	
    	stmt.executeUpdate(command.toString());
    }
    
    /**
     * Insert a list of programs, but use several INSERT statements of ROWS_PER_SMALLER_PACKET
     * rows.  This is to avoid getting a max_allowed_packet error
     * 
     * @param programs
     * @throws SQLException
     */
    public void insertProgramsDivided(List<Program> programs) throws SQLException {
    	Iterator<Program> iterator;
    	for (iterator = programs.iterator(); iterator.hasNext();) {
    		List<Program> smallerList = new ArrayList<Program>();
    		for (int i=0; i<ROWS_PER_SMALLER_PACKET && iterator.hasNext(); i++) {
    			smallerList.add(iterator.next());
    		}
    		insertPrograms(smallerList);
    	}
    }
    
    /**
     * Insert a single Genre
     * 
     * @param genre - Schedule to be inserted
     * @throws SQLException
     */
    public void insertGenre(Genre genre) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("INSERT INTO genres VALUES (?, ?, ?)");
		pstmt.setString(1, genre.getProgId());
		pstmt.setString(2, genre.getGenre());
        pstmt.setInt(3, genre.getRelevance());
        pstmt.executeUpdate();
    }
    
    /**
     * Insert a list of genres
     * 
     * This is done with one large INSERT statement
     * 
     * @param genres
     * @throws SQLException
     */
    public void insertGenres(List<Genre> genres) throws SQLException {
    	if (genres.size() == 0) return;
    	
    	StringBuilder command = new StringBuilder("INSERT INTO genres VALUES ");
    	
    	for (int i=0; i<genres.size(); i++) {
    		Genre genre = genres.get(i);
    		command.append("('" + genre.getProgId() + "', '" + genre.getGenre() + "', " + 
        		genre.getRelevance() + "), ");
;    	}
    	
    	command.delete(command.length()-2, command.length()-1);
    	command.append(";");
    	
    	stmt.executeUpdate(command.toString());
    }
    
    /**
     * Insert a list of genres, but use several INSERT statements of ROWS_PER_SMALLER_PACKET
     * rows.  This is to avoid getting a max_allowed_packet error
     * 
     * @param genres
     * @throws SQLException
     */
    public void insertGenresDivided(List<Genre> genres) throws SQLException {
    	Iterator<Genre> iterator;
    	for (iterator = genres.iterator(); iterator.hasNext();) {
    		List<Genre> smallerList = new ArrayList<Genre>();
    		for (int i=0; i<ROWS_PER_SMALLER_PACKET && iterator.hasNext(); i++) {
    			smallerList.add(iterator.next());
    		}
    		insertGenres(smallerList);
    	}
    }
    
    /**
     * Insert a single Crew object
     * 
     * @param crew
     * @throws SQLException
     */
    public void insertCrew(Crew crew) throws SQLException {
		pstmt = con.prepareStatement("INSERT INTO crew VALUES (?, ?, ?, ?)");
		pstmt.setString(1, crew.getProgId());
		pstmt.setString(2, crew.getFirstName());
		pstmt.setString(3, crew.getLastName());
		pstmt.setString(4, crew.getRole());
		pstmt.executeUpdate();
    }
    
    /**
     * Insert a list of crew
     * 
     * This is done with one large INSERT statement
     * 
     * @param crew
     * @throws SQLException
     */
    public void insertCrew(List<Crew> crew) throws SQLException {
    	if (crew.size() == 0) return;
    	
    	StringBuilder command = new StringBuilder("INSERT INTO crew VALUES ");
    	
    	for (int i=0; i<crew.size(); i++) {
    		Crew cr = crew.get(i);
    		command.append("('" + cr.getProgId() + "', '" + cr.getFirstName().replace("'", "''") +
    			"', '" + cr.getLastName().replace("'", "''") + "', '" + 
    			cr.getRole().replace("'", "''") + "'), ");
    	}
    	
    	command.delete(command.length()-2, command.length()-1);
    	command.append(";");
    	
    	stmt.executeUpdate(command.toString());
    }
    
    /**
     * Insert a list of crew, but use several INSERT statements of ROWS_PER_SMALLER_PACKET
     * rows.  This is to avoid getting a max_allowed_packet error
     * 
     * @param crew
     * @throws SQLException
     */
    public void insertCrewDivided(List<Crew> crew) throws SQLException {
    	Iterator<Crew> iterator;
    	for (iterator = crew.iterator(); iterator.hasNext();) {
    		List<Crew> smallerList = new ArrayList<Crew>();
    		for (int i=0; i<ROWS_PER_SMALLER_PACKET && iterator.hasNext(); i++) {
    			smallerList.add(iterator.next());
    		}
    		insertCrew(smallerList);
    	}
    }
    
    /**
     * Insert a single advisory
     * 
     * @param progId - Program ID of the Program this advisory is for
     * @Param name - The advisory
     * @throws SQLException
     */
    public void insertAdvisory(String progId, String name) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("INSERT INTO advisories VALUES (?, ?)");
		pstmt.setString(1, progId);
		pstmt.setString(2, name);
		pstmt.executeUpdate();
    }
    
    /**
     * Insert a list of advisories
     * 
     * This is done with one large INSERT statement
     * 
     * @param advisories
     * @param progIDs
     * @throws SQLException
     */
    public void insertAdvisories(List<String> advisories, List<String> progIDs) throws SQLException {
    	if (advisories.size() == 0) return;
    	
    	StringBuilder command = new StringBuilder("INSERT INTO advisories VALUES ");
    	
    	for (int i=0; i<advisories.size(); i++) {
    		command.append("('" + progIDs.get(i) + "', '" + 
    				advisories.get(i).replace("'", "''") + "'), ");
    	}
    	
    	command.delete(command.length()-2, command.length()-1);
    	command.append(";");
    	
    	stmt.executeUpdate(command.toString());
    }
    
    /**
     * Insert a list of advisories, but use several INSERT statements of ROWS_PER_SMALLER_PACKET
     * rows.  This is to avoid getting a max_allowed_packet error
     * 
     * @param advisories - List of advisories
     * @param progIDs - List of program IDs associated with the advisories
     * @throws SQLException
     */
    public void insertAdvisoriesDivided(List<String> advisories, 
    		List<String> progIDs) throws SQLException {
    	
    	int index=0;
    	while (index<advisories.size()) {
    		
    		List<String> smallerAdvisoryList = new ArrayList<String>();
    		List<String> smallerProgIdList = new ArrayList<String>();
    		for (int i=0; i<ROWS_PER_SMALLER_PACKET && index < advisories.size(); i++) {
    			smallerAdvisoryList.add(advisories.get(index));
    			smallerProgIdList.add(progIDs.get(index));
    			index++;
    		}
    		insertAdvisories(smallerAdvisoryList, smallerProgIdList);
    	}
    }
    
    //The following methods load an SQL table into rSet
    
    public void loadChannels() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM channels");}
    public void loadMappings() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM mappings");}
    public void loadSchedules() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM schedules");}
    public void loadPrograms() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM programs");}
    public void loadGenres() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM genres");}
    public void loadCrew() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM crew");}
    public void loadAdvisories() throws SQLException {rSet = stmt.executeQuery("SELECT * FROM advisories");}
    public boolean loadStartEnd() throws SQLException {
    	rSet = stmt.executeQuery("SELECT * FROM startendtimes");
    	return rSet.next();
    }
    
    /**
     * Advances rSet to the next element
     * 
     * @return - False if there are no more elements, true otherwise
     * @throws SQLException
     */
    public boolean next() throws SQLException {return rSet.next();}
    
    /**
     * Gets the start time
     * 
     * @return - Start time
     * @throws SQLException
     */
    public long getStartTime() throws SQLException {
    	return rSet.getLong(1);
    }
    
    /**
     * Gets the stop time
     * 
     * @return - Stop time
     * @throws SQLException
     */
    public long getStopTime() throws SQLException {
    	return rSet.getLong(2);
    }
    
    /**
     * Gets the next Channel
     * 
     * @return - channel
     * @throws SQLException
     */
    public Channel getChannel() throws SQLException {
    	Channel ch = new Channel();
    	ch.setTitle(rSet.getString(1));
    	ch.setId(rSet.getString(2));
    	ch.setCallSign(rSet.getString(3));
    	return ch;
    }
    
    /**
     * Gets the next Mapping
     * 
     * @return - mapping
     * @throws SQLException
     */
    public Mapping getMapping() throws SQLException {
    	Mapping map = new Mapping();
    	map.setChanNum(rSet.getInt(1));
    	map.setChanID(rSet.getString(2));
    	return map;
    }
    
    /**
     * Gets the next program
     * 
     * @return - program
     * @throws SQLException
     */
    public Program getProgram() throws SQLException {
    	Program pg = new Program();
    	pg.setProgramId(rSet.getString(1));
    	pg.setTitle(rSet.getString(2));
    	pg.setSubtitle(rSet.getString(3));
    	pg.setOriginalAirDate(rSet.getString(4));
    	pg.setRating(rSet.getString(5));
    	pg.setYear(rSet.getString(6));
    	pg.setEpisodeNo(rSet.getString(7));
    	pg.setMovie(rSet.getBoolean(8));
    	pg.setDesc(rSet.getString(9));
    	return pg;
    }
    
    /**
     * Gets the next schedule
     * 
     * @return - schedule
     * @throws SQLException
     */
    public Schedule getSchedule() throws SQLException {
    	Schedule schedule = new Schedule();
    	schedule.setChanId(rSet.getString(1));
    	schedule.setProgId(rSet.getString(2));
    	schedule.setRating(rSet.getString(3));
    	schedule.setHdtv(rSet.getBoolean(4));
    	schedule.setStart(rSet.getLong(5));
       	schedule.setStop(rSet.getLong(6));
       	return schedule;
    }
    
    /**
     * Gets the next Genre
     * 
     * @return genre
     * @throws SQLException
     */
    public Genre getGenre() throws SQLException {
    	Genre genre = new Genre();
    	genre.setProgId(rSet.getString(1));
    	genre.setGenre(rSet.getString(2));
    	genre.setRelevance(rSet.getInt(3));
    	return genre;
    }
    
    /**
     * Gets the next crew
     * 
     * @return - crew
     * @throws SQLException
     */
    public Crew getCrew() throws SQLException {
    	Crew crew = new Crew();
    	crew.setProgId(rSet.getString(1));
    	crew.setFirstName(rSet.getString(2));
    	crew.setLastName(rSet.getString(3));
    	crew.setRole(rSet.getString(4));
    	return crew;
    }
    
    /**
     * Gets the next advisory
     * 
     * @param advisory
     * @return program ID assiciated with this advisory
     * @throws SQLException
     */
    public String getAdvisory(StringBuilder advisory) throws SQLException {
    	advisory.append(rSet.getString(2));
    	return rSet.getString(1);
    }

}
 