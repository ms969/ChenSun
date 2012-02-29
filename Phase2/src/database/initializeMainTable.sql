INSERT INTO main.acappella
VALUES (null, "Fantasia"), (null, "Hangovers"),(null, "CS Majors"),(null, "After Eight"), (null, "CS5430 TAs");
INSERT INTO main.users
VALUES ("MJ", 1, 'sa'),("Adam", 2, 'sa'),("Kevin", 3, 'sa'),
("Sam", 4, 'sa'),("You", 5, 'sa'),("April", 1, 'admin'),
("Brook", 4, 'member'),("Bryan", 2, 'admin'),("Colin", 1, 'admin'),
("Connie", 1, 'member'),("Jocelyn", 1, 'admin'),("Robert", 1, 'member'),
("Steve", 2, 'member'),("OtherTA", 5, 'admin');
INSERT INTO main.friends
VALUES ("April", "MJ"),("Brook", "Sam"),("Adam", "Bryan"),("April", "Colin"),
("Colin","MJ"),("April","Connie"),("Colin","Connie"),("Connie","MJ"),
("April","Jocelyn"),("Colin","Jocelyn"),("Connie","Jocelyn"),("Jocelyn","MJ"),
("April","Robert"),("Colin","Robert"),("Connie","Robert"),("Jocelyn","Robert"),("MJ","Robert"),
("Adam","Steve"),("Bryan","Steve"),("OtherTA","You"),("Kevin","MJ");
INSERT INTO main.friendrequests
VALUES ("You", "Kevin"),("You", "MJ");
INSERT INTO main.registrationrequests
VALUES ("fbs", 5);