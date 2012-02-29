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

  Basic Commands



