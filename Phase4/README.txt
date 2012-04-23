kfc35, ms969 Phase 2
A Social Networking System for A Cappella Groups

Contents
	**** A * next to an item means you will need it for setup!****

	TEAM.txt - required by phase description
	README.txt - required by phase description. You're in it!
	src (folder) - Contains our source code.
	jars (folder) - Contains the JDBC pckg we used and the apache common codec.
	SocialNetworkServer.jar* - Used to run the server.
	ClientGUI.jar* - Used to run the client.
	db_init.sql - the SQL file that initiates the database and populates it with sample data
	createNewBoardDB.sql - SQL that the system needs as a template for creating new boards
	HELP.txt - Contains a list of commands. user can type
	help after they have logged in to prompt the list

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
	- Run the 'source' command on db_init.sql.
	  You must feed the path to this file as the argument to 'source'

3) Running the Server and the client.
	- Start one instance of the command prompt.
	- Navigate to the folder that contains the SocialNetworkServer.jar.
	- run that jar by typing 'java -jar SocialNetworkServer.jar'
	- when prompted for passphrase, type the passphrase 'Cs5430!'
	- Start another instance of the command prompt.
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

Note: We've changed the way admins are added and removed from the boards so it aligns 
with the DAC policy. Whoever creates the board is now the owner of the board and has 
all the privilegs (post, view, grant post and view). The owner can now give permission 
to other admins to view and post under the board.

Tutorial
  First, you'll need to submit registration!
  1) At start up, type 'register'. Follow the prompts and submit
  information accordingly! (username, group you want to join, choose your password and 
  answer the security question).
  2) Afterwards, login as "you" (type 'login you' and then 'Spring1' as password)!
  We have a user set up for you named "you".
  If you want to use this existing registration, just type 'login you'.
  3) "you" is the Super Admin of the a cappella group CS5430 TAs. There are friend
  requests and registration requests waiting for you. You can type "regRequests" to
  view a list of registration requests for your group and approve them. You can 
  also type "friendRequests" to view a list of people who wants to friend you. You 
  can then approve or remove these requests.
  4) At any point during the functions, you can type cancel to end the proceeding.
  5) Type "addFriend c" to view a list of users whose name starts with 'c'
     type any one of the names to send them a friend request.
  6) Type "logout" to sign off as "you." Logging out exits the client GUI.
  7) Start another client GUI to log in as someone else. Type "login mj" and then 
  "Spring1" for password to sign on as MJ.
  8) Type "showFriends" to see a list of your friends.
  9) Type "changeUserRole" and then type someone's name to toggle their role 
     between admin and member
 10) Type "deleteUser" to delete someone from your group.
 11) Type "transferSA" to transfer your super admin role to another admin in the 
     group.
 12) You can also log in as a regular member and exercise the regular members. 
 (Everybody's passwords are 'Spring1')
 13) Type "createBoard <boardname>" while at Home to create a board. (Must be an
     admin)
 14) Type "addAdmin" to add admins to your newly created board. You can only add
     admins you're friends with to the board. Admins can view and post in every 
	 region in the board. You can also type "removeAdmin" to remove admins from 
	 your board. You can only do this if you're the creator of the board.
 15) Type "createRegion <regionname>" when in any board (not the freeforallboard) 
     to create a region.
 16) Type "addParticipants" to add participants to your newly created region. 
     You can also type "editParticipants" and "removeParticipants" to change 
	 participant options.
 17) Type "post" when in any region or in the freeforallboard to create a post. 
     (You can only post in starred regions!) 
     Type in your content and press enter to submit (or type cancel)
 18) Type "reply" when viewing any post to add a reply.
     (You can only reply to starred posts in the freeforall board, or under
     starred regions!)
     Type in your content and press enter to submit (or type cancel)
     commands listed below.
 19) Type "refresh" to refresh the page you are on.
 20) Type "goto <destination>" to go to the specified place. Typing destination as 
     "home" or "/" immediately goes home. Typing ".." goes back one in the current path.
     You may only go forward or backward one unit at a time.
Suggestion: Try experimenting logging in with different users! All users have the password 
"Spring1". Logging in as various Fantasia members yields very unique board viewing 
experiences based on the privileges individuals have for the fantasiaboard.
Suggestion: Go crazy with creating boards, regions, posts, and replies!


-------------------------------------------------------------------
commands                            functionalities				
-------------------------------------------------------------------
goto                    Command for moving between boards, regions,
                        and posts. If the current page contains 
                        board1, goto board1 will take you into 
                        board1.

refresh                 Refreshes the current page.

post                    Creates a post in the freeforall board or 
                        in a region in another board.

reply                   Creates a reply in a post.

participant             Displays a list of the participants in the 
                        current region. Must be in a region to 
                        invoke this command.

addParticipants         Adds participants to the current region,
                        or a freeforall post. You must be in a 
                        region or freeforall post to invoke this 
                        command, and you must be the manager of 
                        the region or owner of the freeforall post.
                        
removeParticipants      Displays a list of participants that you 
                        can remove. You must be in a region or a
                        freeforall post to invoke this command, 
                        and you must be the manager of the region 
                        or owner of the freeforall post.
                        
editParticipant         Edits the privilege level of a participant 
                        in the current region. You must be in a 
                        region or a freeforall post to invoke this 
                        command, and you must be the manager of 
                        the region or owner of the freeforall post.
                        
showFriends             Displays your friend list.

addFriend               Displays a list of users that you could 
                        friend. addFriend fr will display a list
                        of usernames starting with fr.

friendRequests          View friend requests.

logout                  Signs out the current user.
-------------------------------------------------------------------
Admin only commands:
-------------------------------------------------------------------
createBoard <bname>     Creates a posting board.

createRegion <rname>    Creates a region within a board. Does not
                        work in freeforall board because it doesn't
                        contain regions.

regRequests             View registration requests for your group.
                        
addAdmins               Adds admin to the current board. You must 
                        be within a board to invoke this command,
                        and you must be the manager of the board.
                        Freeforall boards don't have admins.
                        
removeAdmins            Remove admins from the current board. You
                        must be within a board to invoke this 
                        command, and you must be the manager of the
                        board. Freeforall boards don't have admins.
-------------------------------------------------------------------
SA only commands:
-------------------------------------------------------------------
changeUserRole          SA only command. Change an user's role.

deleteUser              SA only command. Displays a list of users
                        from your group that you can delete.

transferSA              SA only command. Transfer your SA role to
                        another admin in your group.


