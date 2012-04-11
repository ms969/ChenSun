CREATE DATABASE freeforall;
CREATE TABLE freeforall.posts (
    pid INT NOT NULL AUTO_INCREMENT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    dateLastUpdated DATETIME NOT NULL,
    PRIMARY KEY(pid)
    /*FOREIGN KEY(postedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.replies (
    pid INT NOT NULL,
    eid INT NOT NULL AUTO_INCREMENT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(4000) NOT NULL,
    PRIMARY KEY(eid, pid),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(repliedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
/*CREATE TABLE freeforall.files (
    pid INT NOT NULL,
    furl INT NOT NULL,
    PRIMARY KEY(pid, furl),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
);*/
CREATE TABLE freeforall.postprivileges (
    pid INT NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    PRIMARY KEY(pid, username),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(username, grantedBy) REFERENCES Main.Users(username) ON DELETE CASCADE*/
)