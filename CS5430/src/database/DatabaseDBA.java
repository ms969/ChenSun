package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import shared.ProjectConfig;

public class DatabaseDBA {
	private static final boolean DEBUG = ProjectConfig.DEBUG;
	
	/**ATOMIC DATABASE UPDATE
	 * Inserts Acappella Group, and the new user as an SA for this group.
	 * Used as a DBA Function.
	 * 
	 * Since this function is intended to be used server side, it does not
	 * return anything. It prints things itself.
	 */
	public static void createAcappellaGroup(Connection conn, String aname, String username, String pwhash) {
		String insertACappella = "INSERT INTO main.acappella VALUES (null, ?)";
		String getACappellaID = "SELECT aid FROM main.acappella WHERE aname = ?";
		String insertSA = "INSERT INTO main.users VALUES (?, ?, ?, 'sa')";
		
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
