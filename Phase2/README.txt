kfc35, ms969 Phase 2
A Social Networking System for A Cappella Groups

Contents
	**** A * next to an item means you will need it for setup!****

	TEAM.txt - required by phase description
	README.txt - required by phase description. You're in it!
	sql (folder) [
	  - createNewBoardDB.sql - A script used by the program
	  - createMainTable.sql* - Used to create the main table, which stores existing boards, users, a cappella groups, and friends
	  - createFreeForAllTable.sql* - Used to create a board accessible to all.
	  - initializeMainTable.sql* - Used to create the main table, which 
	]
	socialNetworkServer.jar* - Used to run the server.
	clientGUI.jar* - Used to run the client.
	TESTPLAN.txt - required by phase description.

Installation
1) Installing Mysql
	- Download the MySQL Community Server (http://www.mysql.com/downloads/mysql/) that most fits with your system spec. For Windows, use
the MSI Installer. For Mac, use the DMG Archive. You do not have to
 register, just simply click "No, take me to the downloads!" to get to the
mirrors. Choose the one that fits closest with your location.
	- Run the MSI, accepting the agreement. Do the typical install.
Launch the Configuration Wizard when prompted.
	- In the configuration wizard, use the Standard Config. Install
it as a windows service AND INCLUDE the bin directory in your path!
Put the password 'root' as your password. Finish.

2) Initialize Database Tables
	- Go to Start, Click on All Programs, and Click the MySQL folder.
	- Click the Server 5.5 folder, and open the Command Line Client
	- Enter the password (root) to connect to the database.
	- Run the 'source' command on the following files in the order given:
	1) createMainTable.sql
	2) createFreeForAllTable.sql
	3) initializeMainTable.sql
	4) initializeFreeForAllTable.sql
	5) createInitializeSampleBoard.sql
	6) createInitializeSampleBoardTwo.sql
	(Sorry there's this many!)
	
	You must feed the path to this file as the argument to 'source'

3) Running the Server and the client.
	- Start one instance for the command prompt.
	- Navigate to the folder that contains the socialNetworkServer.jar.
	- run that jar.
	- Start another instance for the command prompt.
	- Navigate to the folder that contains the clientGUI.jar
	- run that jar.
The client should be connected to the server now! It is ready at your command.

Tutorial
  First, you'll need to submit registration!
  1) At start up, type 'register'. Follow the prompts and submit
information accordingly! (username and group you want to join).
  2) Afterwards, login as "You" (type 'login You')!
  We have a user set up for you named "You".
  If you want to use this existing registration, just type 'login You'.
  3) "You" is the Super Admin of the a cappella group CS5430 TAs. There are friend
  requests and registration requests waiting for you. You can type "regRequests" to
  view a list of registration requests for your group and approve them. You can 
  also type "friendRequests" to view a list of people who wants to friend you. You 
  can then approve or remove these requests.
  4) At any point during the functions, you can type cancel to end the proceeding.
  5) Type "addFriend c" to view a list of users whose name starts with 'c'
     type any one of the names to send them a friend request.
  6) Type "logout" to sign off as "You"
  7) Type "login MJ" to sign on as MJ.
  8) Type "showFriends" to see a list of your friends.
  9) Type "changeUserRole" and then type someone's name to toggle their role 
     between admin and member
 10) Type "deleteUser" to delete someone from your group.
 11) Type "transferSA" to transfer your super admin role to another admin in the 
     group.
 12) You can also log in as a regular member and exercise the regular members
     commands listed below.
  

  Basic Commands
	- login <username>: log in to the system as user <username>
	- register: registers as a member of a specified a cappella group in the 
	system. After you register, your registration have to be approved by an admin 
	of your group. If you're approved, you will automatically be friends of 
	everyone in your a cappella group.
	- logout: signs you off from the system so a different user can sign on.
	
	- addFriend: Add a user as your friend in the system. The command "addFriend" 
	will display a list of all the users in the system. "addFriemd Kev" will 
	show a list of users whose name starts with "Kev"
	- friendRequests: Shows your pending friend requests.
	- showFriends: Displays a list of all your friends.
	
	Admin and SA only command:
		- regRequests: View all the registration requests in your a cappella 
		group. Here is where you can approve registration requests also.
	
	SA only commands:
		- deleteUser: deletes a user from the system.
		- changeUserRole: changes the role of a user in your a cappella group 
		from admin to member or from member to admin.
		- transferSA: transfer the super admin role from yourself to anothr admin 
		in your a cappella group.
	
	