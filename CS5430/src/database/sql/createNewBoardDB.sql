CREATE DATABASE bname;
CREATE TABLE bname.regions (
    rname VARCHAR(500) NOT NULL,
    managedBy VARCHAR(100) NOT NULL, /*new */
    PRIMARY KEY(rname)
);
CREATE TABLE bname.posts (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    checksum VARCHAR(200) NOT NULL,
    PRIMARY KEY(pid, rname),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
    /*FOREIGN KEY(postedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE bname.replies (
    rname VARCHAR(500) NOT NULL,
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    checksum VARCHAR(200) NOT NULL,
    PRIMARY KEY(eid, rname, pid),
    FOREIGN KEY(rname, pid) REFERENCES posts(rname, pid) ON DELETE CASCADE
    /*FOREIGN KEY(repliedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE bname.regionprivileges (
    rname VARCHAR(500) NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    grantedBy VARCHAR(100) NOT NULL,
    PRIMARY KEY(rname, username),
    FOREIGN KEY(rname) REFERENCES regions(rname) ON DELETE CASCADE
    /*FOREIGN KEY(username, grantedBy) REFERENCES Main.Users(username) ON DELETE CASCADE*/
)