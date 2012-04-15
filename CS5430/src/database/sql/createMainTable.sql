CREATE DATABASE main;
CREATE TABLE main.acappella (
    aid INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    aname VARCHAR(500) NOT NULL
);
CREATE TABLE main.users (
    username VARCHAR(100) NOT NULL PRIMARY KEY,
    pwhash VARCHAR(200) NOT NULL,
    aid INT NOT NULL,
    role ENUM('sa', 'admin', 'member') NOT NULL DEFAULT 'member',
    FOREIGN KEY (aid) REFERENCES acappella(aid) ON DELETE CASCADE
);
CREATE TABLE main.friends (
    username1 VARCHAR(100) NOT NULL,
    username2 VARCHAR(100) NOT NULL,
    PRIMARY KEY (username1, username2),
    FOREIGN KEY (username1) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (username2) REFERENCES users(username) ON DELETE CASCADE
);
CREATE TABLE main.friendrequests (
    requestee VARCHAR(100) NOT NULL,
    requester VARCHAR(100) NOT NULL,
    PRIMARY KEY (requestee, requester),
    FOREIGN KEY (requestee) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (requester) REFERENCES users(username) ON DELETE CASCADE
);
CREATE TABLE main.registrationrequests (
    username VARCHAR(100) NOT NULL PRIMARY KEY,
    pwhash VARCHAR(200) NOT NULL,
    aid INT NOT NULL,
    FOREIGN KEY (aid) REFERENCES acappella(aid) ON DELETE CASCADE
);
CREATE TABLE main.boards (
    bname VARCHAR(500) NOT NULL PRIMARY KEY,
    managedby VARCHAR(100),
    FOREIGN KEY (managedby) REFERENCES users(username) ON DELETE SET NULL
);
CREATE TABLE main.boardadmins (
    bname VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    PRIMARY KEY (bname, username),
    FOREIGN KEY (bname) REFERENCES boards(bname) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);/* new */
