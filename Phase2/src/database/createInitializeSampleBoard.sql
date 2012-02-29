CREATE DATABASE helloworldboard;
CREATE TABLE helloworldboard.regions (
    rname VARCHAR(500) NOT NULL,
    PRIMARY KEY(rname)
);
CREATE TABLE helloworldboard.posts (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
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
    PRIMARY KEY(eid, rname, pid),
    FOREIGN KEY(rname, pid) REFERENCES posts(rname, pid) ON DELETE CASCADE
);
CREATE TABLE helloworldboard.admins (
    username VARCHAR(100) PRIMARY KEY
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
VALUES ("helloworldboard", "Kevin");
INSERT INTO helloworldboard.admins
VALUES ("Kevin"),("MJ");
INSERT INTO helloworldboard.regions
VALUES ("withmjsfriendconnie"), ("postswithreplies");
INSERT INTO helloworldboard.regionprivileges
VALUES ("withmjsfriendconnie", "Connie", 'viewpost', "MJ");
INSERT INTO helloworldboard.posts
VALUES ("withmjsfriendconnie", null, "Connie", NOW(), "Hello World!", NOW());
INSERT INTO helloworldboard.replies
VALUES ("withmjsfriendconnie", 1, null, "MJ", NOW(), "The World says Hi Back!"),
("withmjsfriendconnie", 1, null, "Kevin", NOW(), "The World says Goodbye.");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "Kevin", NOW(), "Bologna", NOW()),
("postswithreplies", null, "Kevin", NOW(), "Ham", NOW());
INSERT INTO helloworldboard.replies
VALUES ("postswithreplies", 2, null, "MJ", NOW(), "Roast Beef");
INSERT INTO helloworldboard.posts
VALUES ("postswithreplies", null, "Kevin", NOW(), "Salami", NOW())