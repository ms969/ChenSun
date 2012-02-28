CREATE DATABASE freeforall;
CREATE TABLE freeforall.posts (
    pid INT,
    postedBy VARCHAR(100),
    datePosted DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    PRIMARY KEY(pid)
    /*FOREIGN KEY(postedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.replies (
    pid INT,
    eid INT,
    repliedBy VARCHAR(100),
    dateReplied DATETIME NOT NULL,
    content VARCHAR(2500) NOT NULL,
    PRIMARY KEY(pid, eid),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(repliedBy) REFERENCES Main.Users(username) ON DELETE SET NULL*/
);
CREATE TABLE freeforall.files (
    pid INT NOT NULL,
    furl INT NOT NULL,
    PRIMARY KEY(pid, furl),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
);
CREATE TABLE freeforall.postprivileges (
    pid INT NOT NULL,
    username VARCHAR(100),
    privilege ENUM('view', 'viewpost') NOT NULL,
    PRIMARY KEY(pid, username),
    FOREIGN KEY(pid) REFERENCES posts(pid) ON DELETE CASCADE
    /*FOREIGN KEY(username, grantedBy) REFERENCES Main.Users(username) ON DELETE CASCADE*/
)