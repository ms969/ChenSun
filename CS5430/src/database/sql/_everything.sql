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
    checksum VARCHAR(200) NOT NULL,
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
/*Stores the keys to be fetched by the admin*/
CREATE TABLE main.keys (
	keyid INT NOT NULL PRIMARY KEY,
	salt VARCHAR(30) NOT NULL,
	iterations INT NOT NULL,
	enckey VARCHAR(200) NOT NULL,
	checksum VARCHAR(200) NOT NULL
);

INSERT INTO main.acappella
VALUES (null, "Fantasia"), (null, "Hangovers"),(null, "CS Majors"),(null, "After Eight"), (null, "CS5430 TAs");
INSERT INTO main.users
VALUES ("mj", "2tTus6wjDMg=qXUxwsgSySvlKbKRotvHmraArDIUVzO+/mm16Dkb4cnojsaoCFRYE2/Olr7jEkD7WUgFeF60kWo=", 1, 'sa', 
"eXRZwNrQIyM=plbeMgIyOJB/e8yC8pC3PqMvoR9jKamOktmZnaoE+8yYpMOms+t/aWCi01jNFLscT04soUbgToE=", "l9EI262o8ilrApRQkQa2rg=="),
("adam", "GgLiGTBJqM0=rHh5OZEWKFF5fM5kqt7g6kwuy/MvHbmA+CfFr87gmS5mnwORI0uqkzvvAKRX/nwoscH37zhicTs=", 2, 'sa', 
"3K2gIJCORLg=UdNZE2AWeockefOmQ3IMnbP7UhOtAu6uirFsJiKTUsI1ra5HjiMyQIrT4mPEd9CFWw9fceUzuwA=", "rV2NumrzqugoYizJmP8wMg=="),
("kevin", "bXlaOuRsBO8=ptmCoIRC6klNWVh/2Vrb0W7ptejymMwmycizEOg4RXyM7F1Ry7fGlijcHwVX0ezWhDNtyHzt9gQ=" , 3, 'sa', 
"kaEFcDAhy8E=hAR0gUpA9mTh3MktYI+tSe5WK3AH17pxByXs9RCmDdzXKarujjC1wVK1uXaND/C1Jch0/Zwn4wU=", "UTZmKD0l9jTScIghEauXFw=="),
("sam", "W9KrnI4UAqQ=7me1NIsH3oW8rH6VPPZS6oU/czpzrnOt7obV6R0+W3/UbQXKdnp9FoBggIf5msm8xWR5zDLJSvM=", 2, 'sa', 
"bcUWoyuvAmA=RJJrZGZBEl0jY5VTlpeWrYRmVZ/I7RIu/DPaUaFRrZQDSaaRiqiGOPqYpFxY6oT1UMVYfazoeOE=", "T/5T0kCC08VgEYo0Bhm2xA=="),
("you", "v3CSnQL/3TE=gSEMZp7exI+vrnZavrkBpl25YxuWtKX4zzvvWU69cdKNwSVXFcML8AhCFXGpCbOKM7WHeZa3qkg=", 5, 'sa', 
"vJJ3q4rYx6Q=TV45qNwz/6RZGZmsDQnL0cmr/6vQbeOw4xkzKsiyRUCf9eHsxNgjMqTwHm73sbjAcghVdZxsjc0=", "W8aufSw/uBTfLup4QiJj7Q=="),
("april", "grpDsnuskIo=Qmv/P9N4RQPF49c0nLsj3cZYz12CXvkKyzEJwAU89CVqnw50RuEI0VCRu3NhFxux1HCqdTpvJP8=", 1, 'admin', 
"Od2taSyUH3o=pz7sB+l5GAEpngzApsNmXB6bsSTtc67r0fRblfamdrc+LmQocy3MWJjBMIVifcSCSRoE4HdUwwQ=", "WjDDDtR2UlYob1l0q2jV1w=="),
("brook", "mT0M4HvWzbY=Vs3hj/QVa+P8cYMfUQFCnloXdcPnE5lbPsjzM0ZKUy0TExM+RiIqAwIv3BR47O4gjCuvE5f1TQw=", 4, 'member', 
"wvB20rM1Kes=JHFelphljsoowLj9pOy4qMWxa7Vvt7VwSg1NuFvMACslCAEzI+ALWPXm5kcxbdEaw0XN1SWlMH0=", "1ONgXpnk9melOnYa0eZh/A=="),
("bryan", "A9KDLJATOvo=liAXK1vPXp9IbDf76bSuntEuUgtpoPTCBpBlyqI5iZembGwVg4pgCsuD6zSNKVpRa3b0YvK/ynA=", 2, 'admin', 
"LPDL1s/29fM=CEQ0dOnC8tW/8B94+L77nFMWU4uBGUpGD8S/FLyOnYeT5amcgX7PFT85HY2Jh6X1tryz5DZqTtc=", "xK1AEGx4+IlOBYYKOYp+BQ=="),
("colin", "N1iJL1n8KrA=SEjjORiIDtxZM2JPMZOK6Vkkqxf86Qd81nw25c66FjNnhlUPygbVsLfckOSGmE1ggYOggRtFCD0=", 1, 'admin', 
"B+JLxSXAk9s=khZYFTTbsboEI2H4VPMq1ZOrgParwqOxJWhAxGVcJmGHckjNCACanHWVjdsVR+QzTQMfz93GJDE=", "NFTl13SgM0XwyiAzYjTgCQ=="),
("connie", "aDyG8VKfjtY=Vcjug3pbqq+QBoMapDgL8cxXVrI+uraCk05j8a2crNf0IYviazLscr5eTwhzLgUsTTyy+u1Pvro=", 1, 'member', 
"DGkHKmdDUmE=X3/OrHT3/isfoNip5zfWHJ7RdbofAJ98IUYSwg7cB1wvRSVSQf59Zc/2CSoXpWAEXI85Lowo9Qk=", "CcgciqoOnrqdPLOr5Ez5pA=="),
("jocelyn", "w4amEWjOVuo=M7hU/3/u3qoeJKixw0hf0dBDAFNTA+lh8c3xx66gD+jTm78EsSdtSnOAn4vP2w1Rh6wjR9UyYhk=", 1, 'admin', 
"LvR5FeSskhE=aVW+g5sEEX0C0rySKPEKP0ZP5v6T7SbI5dcB/1GGkWGb5KEiKODB5zy/3qrb1lqC0MgpzTr1dHg=", "Qgu9SJbTOu8Mub2b5QzPbQ=="),
("robert", "cnRBj5GaLrc=Uf/DCuRnqc/JPxJOnIiSfhn49S+b7jhrx5bBQ6LHkkFAyDcy6UFmrSUopT7qntnXaVDznNiCCtk=", 1, 'member', 
"XRqFhCRNxTc=3yc6QvuQsfVEb5/L+IN850ilKYIuAaMldvgbZpJcQ4lFEh+1F56yqAbcFWNtbSw5DN+qSwZok28=", "xqqBCbmXzohD13QP8n9ESw=="),
("steve", "baK0hw52JPM=aUU3WCdkeafnSDhpJwrAovaXX876oCjBPEj9fQNgM4KVW0N9NQ23M55SagvxSmzPIdMHZKoE6hQ=", 2, 'member', 
"jFhTV6bGd0c=ITTxlSi/mSKq5KL81/7+bVQ7QYPREeX3evyGlc/PjqbBxmUxFrNtCl7JJOO+gbYzvEhHdlmqoVY=", "exYWT8KAJK5y0jHInqR4Vw=="),
("otherta", "Jo/PHpgyurE=1rm+0k949UTDkQulJR2JQ7J78oAfUSHYtiOBVdkzqbz8DjsL3eBXllNEi95vRC3axHoAAopl6r8=", 5, 'admin', 
"c8WM0C93Osc=dR8NDWjjDW4aanN1yimei+wvEa+Ws7yQMgnDTasZNa2O6BSyYC40FOU3Npa4dHLTx7xyPlgQWKU=", "X0hLsAB5K2jW03Ocg0rhcQ==");
INSERT INTO main.friends
VALUES ("april", "mj"),("brook", "sam"),("adam", "bryan"),("april", "colin"),
("colin","mj"),("april","connie"),("colin","connie"),("connie","mj"),
("april","jocelyn"),("colin","jocelyn"),("connie","jocelyn"),("jocelyn","mj"),
("april","robert"),("colin","robert"),("connie","robert"),("jocelyn","robert"),("mj","robert"),
("adam","steve"),("bryan","steve"),("otherta","you"),("kevin","mj");
INSERT INTO main.friendrequests
VALUES ("you", "kevin"),("you", "mj");
INSERT INTO main.registrationrequests
VALUES ("fbs", "DSH1ni2SWUTNeLJnYUCwIg==1Q45Fl6NBEeTpcZVUK3aqQ==", 5, "2YZP43bS3qRc2X957peAzg==wbjGVNRI28bp6H0lMWUFKg==");
INSERT INTO main.keys
VALUES (0, "c+qjqf5pTNk=", 1305, "mVxFQ2G+Fr2lDTGciYOozb9NbBco48kDkc9SH4rgVcB9pBYjKY10T9IIBkd30tKYoKdVZbFohX3ihS60gVn6YBgtBEPe64+UGOWq3gKQxCAGP+XTBOIYdaj0KdWb1su4EmhFUYmz/N4gYzBw+gMCIq7TUUtnlNfWOUA+uBnp3tkX1R/WNxfAog==", "OiFAqODR1TspUSA1oHWcMA=="),
(1, "wFBF6XUc0wI=", 1530, "DNBaz9cZ2fWyXPYNIyJSSA==", "O+lAqmiDFJyEU12xp5GLqg==");

/* ------------------------------FREE FOR ALL------------------------------------------ */
CREATE DATABASE freeforall;
CREATE TABLE freeforall.posts (
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    checksum VARCHAR(200) NOT NULL,
    PRIMARY KEY(pid)
    /*FOREIGN KEY(postedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.replies (
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    checksum VARCHAR(200) NOT NULL,
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
OdB8wgHFqfEUgNjC+bjDlwoC5NIdN34=", NOW(), "ffJl/AcjkiVklbxj5fnK4A=="),
(null, "mj", NOW(), "rYkFLcz4Z8E=nCu3FnDk8YIvH53UF8VzBk1nLk6vD2uZdQpIwzdXAn1tmx6h/3eF8D5FycK/M1RWMdjui7F8JVP+
PVIjrH9qqg==", NOW(), "b0tZOxQItCEHtRNq6nc2jA=="),
(null, "mj", NOW(), "+iDZIUm3onI=8oVFVM0MNeoU8w+6oLISanmGO/c+nh8VWhc/ji1T12jr4HnH85zDdfbvn0LLbFn0aBSGHFpF4t++
vQmpMh5QJYi79WSmvcoa", NOW(), "0/2u2K/Kb9iN5qGyjxdGig==");
INSERT INTO freeforall.postprivileges
VALUES (1, "mj", 'viewpost'), (2, "kevin", 'view'), (3, "kevin", 'viewpost');
INSERT INTO freeforall.replies
VALUES (1, null, "mj", NOW(), "bO1CpTSnofY=UUYkipNIiEUJc5WHQLCLRw==", "WQ2ry4qlR7FVjhsglfSfNQ=="),
(3, null, "kevin", NOW(), "YJ157FYsgrI=MeHTF385DENow8/EjrlVw3Fd9KieMpA+TQ5m0OMJ46ingLlJpx34Q5DpcXNSOJYygq1seSWm+Cds
hfeyXn16qYegr2I2qNi+DSBTh0FzxaQ=", "8PbKXaiyZ8jFdQod6Kxsmg==");

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
    checksum VARCHAR(200) NOT NULL,
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
    checksum VARCHAR(200) NOT NULL,
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
VALUES ("withmjsfriendconnie", null, "connie", NOW(), "Q/7u5WaohZY=iVeb2TI3XU8pqH29FZ9rWA==", NOW(), "7Qdih1MuhjZehB6Sv8UNjA==");
INSERT INTO helloworldboard.replies
VALUES ("withmjsfriendconnie", 1, null, "mj", NOW(), "DaZwcPImF/k=YWVEaffaP+qHQaKrQzWd4dZ/9mBCRxk2", "RKK1YzlvA0PfyQI0XLDfjw=="),
("withmjsfriendconnie", 1, null, "kevin", NOW(), "mhoPvsXTBKU=Lt9w+u58q/gHfXLh8YxFstz086vyib7G", "uIGzk5sOfLKbDVQA0aqFcw==");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "kevin", NOW(), "XybtTMu+sC8=KStQPekGLEA=", NOW(), "cBTQGBkU+0ciuQpJ1T9UcQ=="),
("postswithreplies", null, "kevin", NOW(), "X6c8DUw8FVI=hz5t3U9ew+k=", NOW(), "8Hd9yoJcM3xUIFRk/PoRmQ==");
INSERT INTO helloworldboard.replies
VALUES ("postswithreplies", 2, null, "mj", NOW(), "79zfBNMaHuM=/KJWGEHlpfdIvQ5H5MHraw==", "y1tFqRsjewkroz4jIxPmog==");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "kevin", NOW(), "66SOxcRFM3Q=Wh7Dvh+TdF4=", NOW(), "PsQWhwnhr1zfn4/WbexvKQ==");

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
    checksum VARCHAR(200) NOT NULL,
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
    checksum VARCHAR(200) NOT NULL,
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
VALUES ("everyonebutcolinvp", null, "connie", NOW(), "gXYhiFZ/xQ8=mUDfM0+oIWBwLya0jQC9rQ==", NOW(), "sOYC6w0LF+oEvAO7vgFxkw=="),
("adminsandconnieview", null, "mj", NOW(), "+/qaeXS5z3U=tAvE9h+T2L2qjdiIxMOzp9l05Yk9m1YnK5A2oOp+xNR0Xa0ZMJ8pI0tmSPCxCen5TJIhxfjAjWYp
tKMxpUVHJg==", NOW(), "PsavAHvY+MlkWfFiQ8uW+Q==");
INSERT INTO fantasiaboard.replies
VALUES ("everyonebutcolinvp", 1, null, "jocelyn", NOW(), "RuK3LVd6bQA=bDFM2Quk4LU=", "soox5sbDSYHk4jC0qRgDdA==");

