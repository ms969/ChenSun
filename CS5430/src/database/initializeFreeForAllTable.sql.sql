INSERT INTO freeforall.posts
VALUES (null, "Kevin", NOW(), "We meet again! There are two starts next to this post for me! One star for MJ!", NOW()),
(null, "MJ", NOW(), "Kevin can only view this. There's no star next to it for him.", NOW()),
(null, "MJ", NOW(), "Kevin can post under this! There is a single star next to it for him.", NOW());
INSERT INTO freeforall.postprivileges
VALUES (1, "MJ", 'viewpost'), (2, "Kevin", 'view'), (3, "Kevin", 'viewpost');
INSERT INTO freeforall.replies
VALUES (1, null, "MJ", NOW(), "You're a loser."),
(3, null, "Kevin", NOW(), "I can't reply to your other post over there... so I'm posting here. Dork.")