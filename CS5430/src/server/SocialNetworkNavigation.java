package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBManager;

/**
 * Provides navigation functionality for the user to go to
 * Boards, Regions, and Posts.
 * Conventions for current path:
 *   - path is stored as a String[3] array.
 *   - The first index is the board.
 *   	- If the board.equals("freeforall"), the second index
 *   	is the post.
 *   	- Else, the second index is the region, and the third index
 *   is the post.
 *   - If an entry is null, they are not in the corresponding level.
 *   - The array should be filled from smallest index to biggest index.
 * Conventions for destination:
 *   - All destinations are relative
 *   -"home" (case insensitive) or "/" automatically heads home.
 *   -Can only move one unit from your current position 
 *   in the hierarchy at a time (one forward and one back)
 *     The hierarchy is: Home >> Board (>> Region) >> Post
 *   So, for example, from board, you can only go home or to a region.
 *   - ".." goes back a unit.
 *   
 *   TODO special case: free for all board
 *   TODO navigation should be concerned with whether the board/region/post exists
 */
public class SocialNetworkNavigation {
	
	/** Prints the path in a user friendly way.
	 * "Home/Board/Region/Post#" or "Home/freeforall/Post#"
	 */
	public static String printPath(String[] currentPath) {
		String path = "print Home";
		for (int i = 0; i < currentPath.length; i++) {
			if (currentPath[i] != null) {
				path += "/" + currentPath[i];
			}
			else {
				break;
			}
		}
		return path + ";";
	}
	
	/* 
	 * Assuming non-null destination.
	 * Return values:
	 *   -1 = Go back one in the hierarchy
	 *   0 = Invalid destination
	 *   1 = Go Forward one in the hierarchy to destination
	 *   2 = Go Home
	 */
	public static int validDestination(String[] currentPath, String destination) {
		if (destination.trim().equals("..")) {
			return -1;
		}
		if (destination.trim().toLowerCase().equals("home") || destination.trim().equals("/")) {
			return 2;
		}
		if (currentPath[currentPath.length] != null) {
			return 0;
		}
		else { //actual destination and can advance.
			return 1;
		}
	}
	
	/** Modifies the current path, and returns the last index
	 * where there is a valid entry in the path.
	 * -1 means the path is empty (is in the home dir)
	 * */
	public int goBack(String[] currentPath) {
		for (int i = currentPath.length - 1; i >= currentPath.length; i--) {
			if (currentPath[i] != null) {
				currentPath[i] = null;
				return i-1;
			}
		}
		//The path was already in the home directory.
		return -1;
	}
	
	/**
	 * Assuming the path is in the right position, 
	 * navigate to this board.
	 * TODO (author) ensure the user can go to this board.
	 */
	public String goToBoard(Connection conn, String username, String[] currentPath, String boardName) {
		if (boardName.trim().toLowerCase().equals("freeforall")) {
			currentPath[0] = "freeforall";
			String toReturn = printPath(currentPath);
			toReturn += "print ;"; //empty line
			return toReturn += SocialNetworkPosts.getPostListFreeForAll(username);
		}
		/*else... check that there exists a board with such a name*/
		
		Boolean boardExists = SocialNetworkBoards.boardExists(boardName);
		if (boardExists == null) {
			return "print Database Error trying to access the board. If this persists, contact an admin.";
		}
		else if (boardExists.booleanValue()) {
			currentPath[0] = boardName;
			String toReturn = printPath(currentPath);
			toReturn += "print ;";
			return toReturn + SocialNetworkRegions.viewRegions(username, boardName);
			
		}
		else {
			return "print The board \"" + boardName + "\" does not exist.";
		}
	}
	/*
	public String goToRegion(String currentPath, String boardName, String regionName) {
		if (boardName.trim().toLowerCase().equals("")) {
			return "Incorrect Call to Function";
		}
		String region;
		if (regionName != null) {
			region = regionName;
		}
		else if (currentPath.indexOf("/") == -1) {
			return null;
		}
		else {
			
		}
	}
	public String goToPost(String currentPath, String boardName, String regionName, Integer postNum) {
		if (postNum == null) {
			return "";
		}
	}*/
}
