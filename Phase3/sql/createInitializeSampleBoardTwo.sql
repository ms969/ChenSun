CREATE DATABASE fantasiaboard;
CREATE TABLE fantasiaboard.regions (
    rname VARCHAR(500) NOT NULL,
    PRIMARY KEY(rname)
);
CREATE TABLE fantasiaboard.posts (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
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
    PRIMARY KEY(eid, rname, pid),
    FOREIGN KEY(rname, pid) REFERENCES posts(rname, pid) ON DELETE CASCADE
);
CREATE TABLE fantasiaboard.admins (
    username VARCHAR(100) PRIMARY KEY
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
VALUES ("fantasiaboard", "MJ");
INSERT INTO fantasiaboard.admins
VALUES ("MJ"),("April");
INSERT INTO fantasiaboard.regions
VALUES ("everyonebutcolinvp"), ("onlyadmins"), ("adminsandconnieview");
INSERT INTO fantasiaboard.regionprivileges
VALUES ("everyonebutcolinvp", "Connie", 'viewpost', "MJ"),
("everyonebutcolinvp", "Robert", 'viewpost', "April"),
("adminsandconnieview", "Connie", 'view', "April");
INSERT INTO fantasiaboard.posts
VALUES ("everyonebutcolinvp", null, "Connie", NOW(), "I hate Colin!", NOW()),
("adminsandconnieview", null, "MJ", NOW(), "Haha there's not a star next to this region for you Connie!", NOW());
INSERT INTO fantasiaboard.replies
VALUES ("everyonebutcolinvp", 1, null, "Jocelyn", NOW(), "Me Too!");