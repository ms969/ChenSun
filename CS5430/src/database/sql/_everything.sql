/* ------------------------------DELETING TABLES------------------------------------------ */
DROP DATABASE IF EXISTS main;
DROP DATABASE IF EXISTS freeforall;
DROP DATABASE IF EXISTS fantasiaboard;
DROP DATABASE IF EXISTS helloworldboard;

/* ------------------------------MAIN TABLE------------------------------------------ */
CREATE DATABASE main;
CREATE TABLE main.acappella (
    aid INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    aname VARCHAR(500) NOT NULL UNIQUE
);
CREATE TABLE main.users (
    username VARCHAR(100) NOT NULL PRIMARY KEY,
    pwhash VARCHAR(200) NOT NULL,
    aid INT NOT NULL,
    role ENUM('sa', 'admin', 'member') NOT NULL DEFAULT 'member',
    secanswer VARCHAR(200) NOT NULL,
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
    secanswer VARCHAR(200) NOT NULL,
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

INSERT INTO main.acappella
VALUES (null, "Fantasia"), (null, "Hangovers"),(null, "CS Majors"),(null, "After Eight"), (null, "CS5430 TAs");
INSERT INTO main.users
VALUES ("mj", "w9f70HG6N+CzeLYzvMD60A==kjCoAX4qDSojahEETPXvxA==", 1, 'sa'),
("adam", "XLZdAqa/m3yo/kgkv08sCg==V8VaUX1a2uT9uDoOI4opaQ==", 2, 'sa'),
("kevin", "6EpyG1uU/4n5Np9o9zvoOQ==43VzSCDbRlMlCXlPVIu19Q==", 3, 'sa'),
("sam", "LToMSIiWqFrb0ymExXer4g==uxATu2X8J4cYUtkqA5ygew==", 4, 'sa'),
("you", "0/9ksQuimGvltlhBS/j/xg==jz9j9Q5XHqgkaquOR7d62w==", 5, 'sa'),
("april", "lVx8NGZ2u64OAkRhAHD5uA==B1K9To/NW/EUSs4CQhBDsQ==", 1, 'admin'),
("brook", "WjxjWtPcm+vQXe52Itgj5w==xYflVVq3/dWOZCKrNPROPw==", 4, 'member'),
("bryan", "+6VnR7LRMbJzuJuROnjK1w==Lbb4XlWxEl1wPp6BQUq5bA==", 2, 'admin'),
("colin", "MlK9UHwcyhf+owfM5cvNJQ==Xdp1Rjd2dMOkeZMX307g0Q==", 1, 'admin'),
("connie", "N27yqvAy+qMssAlGoLj8lw==oaTruuTf7Aku9eKZXrqI4w==", 1, 'member'),
("jocelyn", "b/4VY62r8rXrFo5KBzUDXA==7miLyHYhKsqOMa8yUAJrog==", 1, 'admin'),
("robert", "rmKA2h5gSPaECVyt7uc7hw==c2Mv762TEa8JmIiGVKoRcg==", 1, 'member'),
("steve", "8gqHs3f4c2hCmNlsgWgw3A==PBCbLrGG4E+Sw7Lxo/7HeA==", 2, 'member'),
("otherta", "SmUbMmVOG5mZzHp+9CvtVw==Ynpvfscj6QvuTuqP5ES+Xw==", 5, 'admin');
INSERT INTO main.friends
VALUES ("april", "mj"),("brook", "sam"),("adam", "bryan"),("april", "colin"),
("colin","mj"),("april","connie"),("colin","connie"),("connie","mj"),
("april","jocelyn"),("colin","jocelyn"),("connie","jocelyn"),("jocelyn","mj"),
("april","robert"),("colin","robert"),("connie","robert"),("jocelyn","robert"),("mj","robert"),
("adam","steve"),("bryan","steve"),("otherta","you"),("kevin","mj");
INSERT INTO main.friendrequests
VALUES ("you", "kevin"),("you", "mj");
INSERT INTO main.registrationrequests
VALUES ("fbs", "hwfJMWs9u7VDDKnfHlEbAg==bYb21OyGkXgMzeEj35CtlA==", 5);

/* ------------------------------FREE FOR ALL------------------------------------------ */
CREATE DATABASE freeforall;
CREATE TABLE freeforall.posts (
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(pid)
    /*FOREIGN KEY(postedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.replies (
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(eid, pid),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(repliedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.postprivileges (
    pid INT NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    PRIMARY KEY(pid, username),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(username, grantedBy) REFERENCES Main.Users(username) ON DELETE CASCADE*/
);

INSERT INTO freeforall.posts
VALUES (null, "kevin", NOW(), "JXV+k4PPCgU=79GJyI5/CkoEhY/A8XV+dS4Qkc7vLhBaIQIibZMmi7wm9TG9AkZCXCAFEOcpl+TNk9KqnSw3vqUi
OdB8wgHFqfEUgNjC+bjDlwoC5NIdN34=", NOW()),
(null, "mj", NOW(), "rYkFLcz4Z8E=nCu3FnDk8YIvH53UF8VzBk1nLk6vD2uZdQpIwzdXAn1tmx6h/3eF8D5FycK/M1RWMdjui7F8JVP+
PVIjrH9qqg==", NOW()),
(null, "mj", NOW(), "+iDZIUm3onI=8oVFVM0MNeoU8w+6oLISanmGO/c+nh8VWhc/ji1T12jr4HnH85zDdfbvn0LLbFn0aBSGHFpF4t++
vQmpMh5QJYi79WSmvcoa", NOW());
INSERT INTO freeforall.postprivileges
VALUES (1, "mj", 'viewpost'), (2, "kevin", 'view'), (3, "kevin", 'viewpost');
INSERT INTO freeforall.replies
VALUES (1, null, "mj", NOW(), "bO1CpTSnofY=UUYkipNIiEUJc5WHQLCLRw=="),
(3, null, "kevin", NOW(), "YJ157FYsgrI=MeHTF385DENow8/EjrlVw3Fd9KieMpA+TQ5m0OMJ46ingLlJpx34Q5DpcXNSOJYygq1seSWm+Cds
hfeyXn16qYegr2I2qNi+DSBTh0FzxaQ=");

/* ------------------------------HELLOWORLDBOARD------------------------------------------ */
CREATE DATABASE helloworldboard;
CREATE TABLE helloworldboard.regions (
    rname VARCHAR(500) NOT NULL,
    managedBy VARCHAR(100) NOT NULL, /* new */
    PRIMARY KEY(rname)
);
CREATE TABLE helloworldboard.posts (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(pid, rname),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
);
CREATE TABLE helloworldboard.replies (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(eid, rname, pid),
    FOREIGN KEY(rname, pid) REFERENCES posts(rname, pid) ON DELETE CASCADE
);
CREATE TABLE helloworldboard.regionprivileges (
    rname VARCHAR(500) NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    grantedBy VARCHAR(100) NOT NULL,
    PRIMARY KEY(rname, username),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
);
/* Inserting Admins!*/
INSERT INTO main.boards
VALUES ("helloworldboard", "kevin");
INSERT INTO main.boardadmins
VALUES ("helloworldboard", "kevin"), ("helloworldboard", "mj"); /*new*/
INSERT INTO helloworldboard.regions
VALUES ("withmjsfriendconnie", "mj"), ("postswithreplies", "mj"); /*changes*/
INSERT INTO helloworldboard.regionprivileges
VALUES ("withmjsfriendconnie", "connie", 'viewpost', "mj");
INSERT INTO helloworldboard.posts
VALUES ("withmjsfriendconnie", null, "connie", NOW(), "Q/7u5WaohZY=iVeb2TI3XU8pqH29FZ9rWA==", NOW());
INSERT INTO helloworldboard.replies
VALUES ("withmjsfriendconnie", 1, null, "mj", NOW(), "DaZwcPImF/k=YWVEaffaP+qHQaKrQzWd4dZ/9mBCRxk2"),
("withmjsfriendconnie", 1, null, "kevin", NOW(), "mhoPvsXTBKU=Lt9w+u58q/gHfXLh8YxFstz086vyib7G");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "kevin", NOW(), "XybtTMu+sC8=KStQPekGLEA=", NOW()),
("postswithreplies", null, "kevin", NOW(), "X6c8DUw8FVI=hz5t3U9ew+k=", NOW());
INSERT INTO helloworldboard.replies
VALUES ("postswithreplies", 2, null, "mj", NOW(), "79zfBNMaHuM=/KJWGEHlpfdIvQ5H5MHraw==");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "kevin", NOW(), "66SOxcRFM3Q=Wh7Dvh+TdF4=", NOW());

/* ------------------------------FANTASIABOARD------------------------------------------ */
CREATE DATABASE fantasiaboard;
CREATE TABLE fantasiaboard.regions (
    rname VARCHAR(500) NOT NULL,
    managedBy VARCHAR(100) NOT NULL, /* new */
    PRIMARY KEY(rname)
);
CREATE TABLE fantasiaboard.posts (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(pid, rname),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
);
CREATE TABLE fantasiaboard.replies (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    /*checksum VARCHAR(200) NOT NULL,*/
    PRIMARY KEY(eid, rname, pid),
    FOREIGN KEY(rname, pid) REFERENCES posts(rname, pid) ON DELETE CASCADE
);
CREATE TABLE fantasiaboard.regionprivileges (
    rname VARCHAR(500) NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    grantedBy VARCHAR(100) NOT NULL,
    PRIMARY KEY(rname, username),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
);

/* Inserting Admins!*/
INSERT INTO main.boards
VALUES ("fantasiaboard", "mj");
INSERT INTO main.boardadmins
VALUES ("fantasiaboard", "mj"), ("fantasiaboard", "april"); /*new*/
INSERT INTO fantasiaboard.regions
VALUES ("everyonebutcolinvp", "april"), ("onlyadmins", "mj"), ("adminsandconnieview", "mj");
INSERT INTO fantasiaboard.regionprivileges
VALUES ("everyonebutcolinvp", "connie", 'viewpost', "mj"),
("everyonebutcolinvp", "robert", 'viewpost', "april"),
("adminsandconnieview", "connie", 'view', "april");
INSERT INTO fantasiaboard.posts
VALUES ("everyonebutcolinvp", null, "connie", NOW(), "gXYhiFZ/xQ8=mUDfM0+oIWBwLya0jQC9rQ==", NOW()),
("adminsandconnieview", null, "mj", NOW(), "+/qaeXS5z3U=tAvE9h+T2L2qjdiIxMOzp9l05Yk9m1YnK5A2oOp+xNR0Xa0ZMJ8pI0tmSPCxCen5TJIhxfjAjWYp
tKMxpUVHJg==", NOW());
INSERT INTO fantasiaboard.replies
VALUES ("everyonebutcolinvp", 1, null, "jocelyn", NOW(), "RuK3LVd6bQA=bDFM2Quk4LU=");

