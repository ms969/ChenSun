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
INSERT INTO main.boardadmins
VALUES ("fantasiaboard", "MJ"), ("fantasiaboard", "April"); /*new*/
INSERT INTO fantasiaboard.regions
VALUES ("everyonebutcolinvp", "April"), ("onlyadmins", "MJ"), ("adminsandconnieview", "MJ");
INSERT INTO fantasiaboard.regionprivileges
VALUES ("everyonebutcolinvp", "Connie", 'viewpost', "MJ"),
("everyonebutcolinvp", "Robert", 'viewpost', "April"),
("adminsandconnieview", "Connie", 'view', "April");
INSERT INTO fantasiaboard.posts
VALUES ("everyonebutcolinvp", null, "Connie", NOW(), "gXYhiFZ/xQ8=mUDfM0+oIWBwLya0jQC9rQ==", NOW()),
("adminsandconnieview", null, "MJ", NOW(), "+/qaeXS5z3U=tAvE9h+T2L2qjdiIxMOzp9l05Yk9m1YnK5A2oOp+xNR0Xa0ZMJ8pI0tmSPCxCen5TJIhxfjAjWYp
tKMxpUVHJg==", NOW());
INSERT INTO fantasiaboard.replies
VALUES ("everyonebutcolinvp", 1, null, "Jocelyn", NOW(), "RuK3LVd6bQA=bDFM2Quk4LU=");