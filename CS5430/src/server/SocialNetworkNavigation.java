package server;

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
		//first case covers being in a regular board's post
		//second case covers being in the freeforall board's post
		if (currentPath[currentPath.length-1] != null || 
				(currentPath[0] != null && ("freeforall").equals(currentPath[0]) 
						&& currentPath[1] != null)) {
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
	public static void goBack(String[] currentPath) {
		for (int i = currentPath.length - 1; i >= 0; i--) {
			if (currentPath[i] != null) {
				currentPath[i] = null;
				return ;
			}
		}
		//The path was already in the home directory.
	}
	
	/**
	 * Assuming the path is in the right position, 
	 * navigate to this board.
	 * TODO (author) ensure the user can go to this board.
	 */
	public static String goToBoard(String username, String[] currentPath, String boardName) {
		String toReturn = "";
		if (boardName.trim().toLowerCase().equals("freeforall")) {
			toReturn = SocialNetworkPosts.viewPostList(username, "freeforall", null, true);
			if (!toReturn.substring(0, ("print Error:").length()).equals("print Error:")) {
				currentPath[0] = "freeforall";
				toReturn = printPath(currentPath) + "print ;" + toReturn;
			}
			else {
				toReturn = "print Error while heading to destination;" + toReturn;
			}
		}
		else {
			toReturn = SocialNetworkRegions.viewRegions(username, boardName, true);
			if (!toReturn.substring(0, ("print Error:").length()).equals("print Error:")) {
				currentPath[0] = boardName;
				toReturn = printPath(currentPath) + "print ;" + toReturn;
			}
			else {
				toReturn = "print Error while heading to destination;" + toReturn;
			}
		}
		return toReturn;
	}
	
	/**
	 * Assuming the path is in the right position, 
	 * navigate to this region
	 * TODO (author) ensure the user can go to this region.
	 */
	public static String goToRegion(String username, String[] currentPath, String regionName) {
		if (currentPath[0] == null || ("freeforall").equals(currentPath[0])) {
			return "Invalid Call to Function";
		}
		else {
			String toReturn = SocialNetworkPosts.viewPostList(username, currentPath[0], regionName, true);
			if (!toReturn.substring(0, ("print Error:").length()).equals("print Error:")) {
				currentPath[1] = regionName;
				toReturn = printPath(currentPath) + "print ;" + toReturn;
			}
			else {
				toReturn = "print Error while heading to destination;" + toReturn;
			}
			return toReturn;
		}
		
	}
	
	/**
	 * Assuming the path is in the right position, 
	 * navigate to this post
	 * TODO (author) ensure the user can view this post
	 */
	public static String goToPost(String username, String[] currentPath, int postNum) {
		if (("freeforall").equals(currentPath[0])) {
			String toReturn = SocialNetworkPosts.viewPost(username, currentPath[0], null, postNum, true);
			if (!toReturn.substring(0, ("print Error:").length()).equals("print Error:")) {
				currentPath[1] = "" + postNum;
				toReturn = printPath(currentPath) + "print ;" + toReturn;
			}
			else {
				toReturn = "print Error while heading to destination;" + toReturn;
			}
			return toReturn;
		}
		else if (currentPath[1] != null && currentPath[0] != null){
			String toReturn = SocialNetworkPosts.viewPost(username, currentPath[0], currentPath[1], postNum, true);
			if (!toReturn.substring(0, ("print Error:").length()).equals("print Error:")) {
				currentPath[2] = "" + postNum;
				toReturn = printPath(currentPath) + "print ;" + toReturn;
			}
			else {
				toReturn = "print Error while heading to destination;" + toReturn;
			}
			return toReturn;
		}
		else {
			return "Invalid Call to Function";
		}
	}
}
