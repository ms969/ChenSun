package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import shared.ProjectConfig;

public class DatabaseDBA {
	private static final boolean DEBUG = ProjectConfig.DEBUG;
	
	private static final int NUMCOLUMNSKEYS = 5;
	
	/**Function fetches the record in the database
	 * that corresponds to keys
	 */
	public static String fetchKeys(Connection conn) {
		String query = "SELECT * FROM main.keys ORDER BY keyid ASC";
		
		Statement stmt = null;
		ResultSet rs = null;
		
		String keys = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			keys = "";
			while (rs.next()) {
				for (int i = 1; i <= NUMCOLUMNSKEYS; i++) {
					keys += rs.getString(i) + " ";
				}
				//remove the last space; replace it with a semicolon.
				keys = keys.substring(0, keys.length() - 1) + ";";
			}
			keys = keys.substring(0, keys.length() - 1);
		}
		catch (SQLException e) {
			System.out.println("Error fetching the keys from the database");
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			DBManager.closeStatement(stmt);
			DBManager.closeResultSet(rs);
		}
		return keys;
	}
	
	/**ATOMIC DATABASE UPDATE
	 * Inserts Acappella Group, and the new user as an SA for this group.
	 * Used as a DBA Function.
	 * 
	 * Since this function is intended to be used server side, it does not
	 * return anything. It prints things itself.
	 */
	public static void createAcappellaGroup(Connection conn, String aname, String username, String pwhash, String secA) {
		String insertACappella = "INSERT INTO main.acappella VALUES (null, ?)";
		String getACappellaID = "SELECT aid FROM main.acappella WHERE aname = ?";
		String insertSA = "INSERT INTO main.users VALUES (?, ?, ?, 'sa', ?)";
		
		PreparedStatement pstmtInsertAC = null;
		PreparedStatement pstmtGetID = null;
		PreparedStatement pstmtInsertSA = null;
		int success;
		ResultSet rs = null;
		
		boolean sqlex = false;
		//String sqlexmsg = null;
		try {
			conn.setAutoCommit(false);
			pstmtInsertAC = conn.prepareStatement(insertACappella);
			pstmtInsertAC.setString(1, aname);
			success = pstmtInsertAC.executeUpdate();
			if (success == 1) {
				pstmtGetID = conn.prepareStatement(getACappellaID);
				pstmtGetID.setString(1, aname);
				rs = pstmtGetID.executeQuery();
				if (rs.next()) {
					//need the ID for insertion into the user DB
					int aid = rs.getInt("aid");
					pstmtInsertSA = conn.prepareStatement(insertSA);
					pstmtInsertSA.setString(1, username);
					pstmtInsertSA.setString(2, pwhash);
					pstmtInsertSA.setInt(3, aid);
					pstmtInsertSA.setString(4, secA);
					success = pstmtInsertSA.executeUpdate();
					if (success == 1) {
						conn.commit();
						System.out.println("Group & SA creation success");
					}
					else {
						System.out.println("Error creating the new user");
						conn.rollback();
					}
				}
				else {
					System.out.println("Error fetching the new group's id");
					conn.rollback();
				}
			}
			else {
				System.out.println("Error creating the new group");
				conn.rollback();
			}
		}
		catch (SQLException e) {
			DBManager.rollback(conn);
			if (e.getErrorCode() == DBManager.DUPLICATE_KEY_CODE) {
				System.out.println("Error: Duplicate user being inserted into the database, or A Cappella group already exists.");
			}
			else {
				e.printStackTrace();
				System.out.println("Error: Database error during creation of group / user (stack trace above)");
			}
			sqlex = true;
		}
		finally {
			DBManager.closeResultSet(rs);
			DBManager.closePreparedStatement(pstmtInsertAC);
			DBManager.closePreparedStatement(pstmtGetID);
			DBManager.closePreparedStatement(pstmtInsertSA);
		}
	}

}
