package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SocialNetworkDatabaseBoards {
	
	/**
	 * Utility function placed here for now...
	 * Function to get the user's role.
	 */
	public static String getUserRole(Connection conn, String username) {
		String userRole = "SELECT role FROM main.users WHERE username = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String role = "";
		try {
			pstmt = conn.prepareStatement(userRole);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				role = rs.getString("role");
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(rs);
		}
		return role;
	}
	
	/**
	 * Checks whether the user is part of this board.
	 * Assumes the board is valid.
	 */
	public static Boolean isBoardAdmin(Connection conn, String username, String boardName) {
		String isAdminQ = "SELECT * FROM " + boardName + ".admins WHERE username = ?";
		PreparedStatement pstmt = null;
		ResultSet adminResult = null;
		Boolean isAdmin = null;
		try {
			pstmt = conn.prepareStatement(isAdminQ);
			pstmt.setString(1, username);
			adminResult = pstmt.executeQuery();
			isAdmin = new Boolean(adminResult.next());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(adminResult);
		}
		return isAdmin;
	}
	
	/**
	 * Determine whether the board exists.
	 * The return arg is null if there was an exc.
	 */
	public static Boolean boardExists(Connection conn, String boardName) {
		String getBoard = "SELECT * FROM main.boards WHERE bname = ?";
		PreparedStatement pstmt = null;
		ResultSet boardResult = null;
		Boolean boardExists = null; //null if there is an error.
		try {
			pstmt = conn.prepareStatement(getBoard);
			pstmt.setString(1, boardName);
			boardResult = pstmt.executeQuery();
			boardExists = new Boolean(boardResult.next());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(boardResult);
		}
		return boardExists;
	}
	
	/**
	 * Using an SQL Script, creates a database for the provided board id.
	 * FUNCTION SHOULD BE RUN WHEN AUTO COMMIT = FALSE
	 * Returns true on success, false on failure.
	 * method is used within createBoard
	 * @throws IOException 
	 */
	private static boolean createBoardDatabase(Connection conn, String boardName) throws IOException {
		File createBoardSql = null;
		BufferedReader sqlReader = null;
		String fileContents = "";
		String line = null;
		String[] queries;
		/*Read in file contents and set correct references to bid*/
		try {
			createBoardSql = new File("src/database/createNewBoardDB.sql");
			sqlReader = new BufferedReader(new FileReader(createBoardSql));
			line = sqlReader.readLine();
			while (line != null) {
				fileContents += line;
				line = sqlReader.readLine();
			}
		}
		finally {
			if (sqlReader != null) {
				sqlReader.close();
			}
		}
		fileContents = fileContents.replaceAll("bname", boardName);
		queries = fileContents.split(";");
		
		/*Execute all queries*/
		Statement stmt = null;
		int[] results = null;
		boolean error = false;
		try {
			//conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for (int i = 0; i < queries.length; i++) {
				stmt.addBatch(queries[i]);
			}
			results = stmt.executeBatch();
		}
		catch (SQLException e) {
			//rollback done in the encapsulating function
			/*if (conn != null) {
				conn.rollback();
			}*/
			e.printStackTrace();
			error = true;
		}
		finally {
			DBManager.closeStatement(stmt);
			//conn.setAutoCommit(true);
		}
		/* The first result is of creating the database,
		 * which affects 1 row.
		 */
		if (error || results == null) {
			return false;
		}
		if (results[0] == 1) {
			/*
			 * All other results are of creating tables,
			 * which affects 0 rows.
			 */
			for (int j = 1; j < results.length; j++) {
				if (results[j] != 0) {
					return false;
				}
			}
			//Everything succeeded
			return true;
		}
		else {
		  return false;
		}
	}
	
	/**
	 * Transactional method that creates a board.
	 * Assumes that the user is an admin.
	 * TODO LATER (author) 1) Checks that the user has permission to create a board
	 * 2) It creates a reference to the board in the "main" database
	 * 3) It creates a database to store the board's regions, posts, etc.
	 * @throws IOException 
	 */
	public static String createBoard(Connection conn, String createdBy, String boardName) 
	throws IOException {
		//PreparedStatement rolePstmt = null;
		PreparedStatement insertBoardPstmt = null;
		PreparedStatement addAdminPstmt = null;
		ResultSet idresult = null;
		
		int firstsuccess = 0; //insertBoard success
		boolean secondsuccess = false; //create board db success
		int thirdsuccess = 0; //add admin success
		boolean sqlex = false;
		String sqlexmsg = "";
		String insertBoard = "INSERT INTO main.boards VALUES (?, ?)";
		String insertAdmin = "INSERT INTO " + boardName + ".admins VALUES (?)";
		try {
			conn.setAutoCommit(false);
			insertBoardPstmt = conn.prepareStatement(insertBoard);
			addAdminPstmt = conn.prepareStatement(insertAdmin);
			
			insertBoardPstmt.setString(1, boardName);
			insertBoardPstmt.setString(2, createdBy);
			firstsuccess = insertBoardPstmt.executeUpdate();
			if (firstsuccess == 1) { /*1 row successfully inserted*/
				secondsuccess = createBoardDatabase(conn, boardName);
				if (secondsuccess) {
					addAdminPstmt.setString(1, createdBy);
					thirdsuccess = addAdminPstmt.executeUpdate();
					if (thirdsuccess == 1) {
						conn.commit();
					}
					else {
						conn.rollback();
					}
				}
				else {
					conn.rollback();
				}
			}
			else {
				conn.rollback();
			}
		}
		catch (SQLException e) {
			DBManager.rollback(conn);
			/* The error code for a duplicate key insertion => Must be for board name*/
			if (e.getErrorCode() == DBManager.DUPLICATE_KEY_CODE) {
				sqlexmsg = "print A board already exists with that name. Try a different name.";
			}
			else {
				e.printStackTrace();
				sqlexmsg = "print Error: Connection error. Contact the admin.";
			}
			sqlex = true;
		}
		catch (FileNotFoundException fnfe) {
			DBManager.rollback(conn);
			fnfe.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(insertBoardPstmt);
			DBManager.closeResultSet(idresult);
			DBManager.trueAutoCommit(conn);
		}
		if (firstsuccess == 1 && secondsuccess && thirdsuccess == 1) {
			return "print Board \"" + boardName +"\" succesfully created.";
		}
		else if (firstsuccess == 0 && !sqlex) {
			return "print Error: Cannot create a board with the name \"" + boardName 
			  + "\". Please use a different name.";
		}
		//TODO later on, we might want to make these errors more ambiguous
		else if (secondsuccess && !sqlex) {
			return "print Error: Database error while creating/initializing a board database. Contact an admin.";
		}
		else if (!sqlex){
			return "print Error: Could not add admin to the board db. Contact the admin.";
		}
		else {
			return sqlexmsg;
		}
	}
	
	/**
	 * Gets a list of boards that the user has permission to view.
	 * For Admins: Must be within the "admin" list of the board
	 * For Users: Must be within the "RegionPrivileges" list of the board
	 * A user has permission to view a board if it has at least one
	 * region where it has view permissions within that board.
	 * */
	public static String getBoardList(Connection conn, String username) {
		String boardlist = "print Boards:;print \tfreeforall;";
		/*First, get a list of all the boards*/
		String allBoards = "SELECT bname FROM main.boards";

		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet boards = null;
		ResultSet privResult = null;
		boolean sqlex = false;
		try {
			stmt = conn.createStatement();
			boards = stmt.executeQuery(allBoards);
			String role = getUserRole(conn, username);
			String getRegionPrivs, getRegionAdmins;
			while (boards.next()) {
				/* For each Board ID, check its RegionPrivileges to see
				 * if there exists one tuple with username = this user
				 */
				String bname = boards.getString("bname");
				if (role.equals("member")) {
					getRegionPrivs = "SELECT privilege FROM " 
						+ bname + ".regionprivileges WHERE username = ?";
					pstmt = conn.prepareStatement(getRegionPrivs);
					pstmt.setString(1, username);
					privResult = pstmt.executeQuery();
					if (privResult.next()) { // returns true if there is a result set.
						boardlist += "print \t" + bname + ";";
					}
					privResult.close();
					pstmt.close();
					privResult = null;
					pstmt = null;
				}
				else if (!role.equals("")) { // an admin
					getRegionAdmins = "SELECT * FROM " 
						+ bname + ".admins WHERE username = ?";
					pstmt = conn.prepareStatement(getRegionAdmins);
					pstmt.setString(1, username);
					privResult = pstmt.executeQuery();
					if (privResult.next()) { // returns true if there is a result set.
						boardlist += "print \t" + bname + ";";
					}
					privResult.close();
					pstmt.close();
					privResult = null;
					pstmt = null;
				}
				else { //there was an sql exception when getting the role.
					sqlex = true;
					break;
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			DBManager.closeStatement(stmt);
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(boards);
		}
		if (boardlist.equals("print Boards:;print freeforall") && !sqlex) {
			return boardlist;
		}
		else if (sqlex) {
			return "print Error: Database Error while querying viewable boards. Contact an admin.";
		}
		else {
			return boardlist;
		}
	}
}
