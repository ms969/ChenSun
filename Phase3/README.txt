kfc35, ms969 Phase 2
A Social Networking System for A Cappella Groups

Contents
	**** A * next to an item means you will need it for setup!****

	TEAM.txt - required by phase description
	README.txt - required by phase description. You're in it!
	sql* (folder) - Contains sql scripts, one used by the program, and
	others needed to initialize the database.
	src (folder) - Contains our source code.
	jars (folder) - Contains the JDBC pckg we used.
	SocialNetworkServer.jar* - Used to run the server.
	ClientGUI.jar* - Used to run the client.
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
	- Navigate to the folder that contains the SocialNetworkServer.jar.
	- run that jar by typing 'java -jar SocialNetworkServer.jar'
	- open up ServerPrivExp.txt and copy the number as the first secret when prompted
	- open up ServerSharedRaw.txt and copy the string as the second secret when prompted
	- Start another instance for the command prompt.
	- Navigate to the folder that contains the ClientGUI.jar
	- run that jar by typing 'java -jar ClientGUI.jar'
The client should be connected to the server now! It is ready at your command.

Tutorial Environment (zzz stuff, can skip down to Tutorial if you'd like)
Users by A cappella group (You may log in as any of these users)
	Fantasia: April, Colin, Connie, Jocelyn, MJ, Robert
	Hangovers: Adam, Bryan Steve
	CS Majors: Kevin
	After Eight: Brook, Sam
	CS5430 TA's: OtherTA, You
All individuals in the A cappella group are already friends with each other.
The only pair of friends that cross the boundary: (Kevin, MJ).
There are two friend requests from Kevin and MJ for You.
There is a pending registration request from fbs for You.
There are three pre-existing boards: freeforall, fantasiaboard, and 
helloworldboard.

The freeforall board
Posts only exist for users Kevin and MJ, who are having a conversation.
One post is created by Kevin, and MJ has viewpost privileges in it. 
Two posts are created by MJ. One post Kevin can only view. The other he can
viewpost.
(Reminder: No stars = Only view. One star = View Post. Two stars = you
created the post)

The fantasiaboard has two admins, MJ and April.
Jocelyn cannot view the board even though she's an admin for Fantasia.
The board has three regions:
  adminsandconnieview: Connie is the only non-admin to have access.
  everyonebutcolinvp: Everoyne but Jocelyn and Colin have viewpost.
  onlyadmins: A board only the admins can see.
This board demonstrates the different views of the program to diff. users.
Jocelyn and Colin: Cannot see the fantasiaboard!
Connie: Can see the board and two regions, adminsandconnieview and everyonebutcolinvp. adminsandconnieview does not have a star next to it.
Robert: Can only see everyonebutcolinvp
April and MJ: Can see all boards.

The helloworldboard has two admins, Kevin and MJ.
Connie is the only other person who can see it.
The board has two regions:
  withmjsfriendconnie - One post by Connie, with two replies.
  postwithreplies - Lots of post by Kevin, with a reply by MJ to one post.

All board posts and replies are now encrypted in the database and will not be able
for viewing directly through the database.

Tutorial
  First, you'll need to submit registration!
  1) At start up, type 'register'. Follow the prompts and submit
information accordingly! (username, group you want to join, and choose your password).
  2) Afterwards, login as "You" (type 'login You' and then 'asdf!@#$' as password)!
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
  7) Type "login MJ" and then "springbreak" for password to sign on as MJ.
  8) Type "showFriends" to see a list of your friends.
  9) Type "changeUserRole" and then type someone's name to toggle their role 
     between admin and member
 10) Type "deleteUser" to delete someone from your group.
 11) Type "transferSA" to transfer your super admin role to another admin in the 
     group.
 12) You can also log in as a regular member and exercise the regular members
 13) Type "createBoard <boardname>" while at Home to create a board. (Must be an
     admin, but not enforced currently)
 14) Type "createRegion <regionname>" when in any board (not the freeforallboard) 
     to create a region.
 15) Type "post" when in any region or in the freeforallboard to create a post. 
     (You should only post in starred regions! Not enforced currently.) 
     Type in your content and press enter to submit (or type cancel)
 16) Type "reply" when viewing any post to add a reply.
     (You should only reply to starred posts in the freeforall board, or under
     starred regions! Not enforced currently.)
     Type in your content and press enter to submit (or type cancel)
     commands listed below.
 17) Type "refresh" to refresh the page you are on.
 18) Type "goto <destination>" to go to the specified place. Typing destination as 
     "home" or "/" immediately goes home. Typing ".." goes back one in the current path.
     You may only go forward or backward one unit at a time!
Suggestion: Try experimenting logging in with different users! All users have the password 
"springbreak" except for 'You' which has password "asdf!@#$". Logging in as various Fantasia
members yields very unique board viewing experiences based on the privileges individuals have for
the fantasiaboard.
Suggestion: Go crazy with creating boards, regions, posts, and replies!

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

	- refresh: Refreshes the current "page" as listed in the current path.
	i.e. a refresh on the homepage will refresh the list of boards.
	- goto <destination>: Based on your current path, heads to the destination.
	Can only go forward or backward, or straight back home.
	For posts, input goto ###.
	- createBoard <boardname>: Creates a board. MUST be at home to do this
	- createRegion <regionname>: Creates a region. MUST be in a board !=
	freeforallboard to do this.
	- post: goes into "posting" mode. MUST be in a board's region or the
	free for all board to do this command. After typing post, type in your content,
	or type 'cancel' to cancel posting.
	- reply: goes into "replying" mode. MUST be inside a post to do this command.
	After typing reply, type in your content, or type 'cancel' to cancel posting
	
	Admin and SA only command:
		- regRequests: View all the registration requests in your a cappella 
		group. Here is where you can approve registration requests also.
	
	SA only commands:
		- deleteUser: deletes a user from the system.
		- changeUserRole: changes the role of a user in your a cappella group 
		from admin to member or from member to admin.
		- transferSA: transfer the super admin role from yourself to anothr admin 
		in your a cappella group.

