INSERT INTO main.acappella
VALUES (null, "Fantasia"), (null, "Hangovers"),(null, "CS Majors"),(null, "After Eight"), (null, "CS5430 TAs");
INSERT INTO main.users
VALUES ("MJ", "w9f70HG6N+CzeLYzvMD60A==kjCoAX4qDSojahEETPXvxA==", 1, 'sa'),("Adam", "XLZdAqa/m3yo/kgkv08sCg==V8VaUX1a2uT9uDoOI4opaQ==", 2, 'sa'),("Kevin", "TsygEax40qEju/DFGUYczQ==uCUeiKf2+QJVEIWuPi9sZQ==", 3, 'sa'),
("Sam", "LToMSIiWqFrb0ymExXer4g==uxATu2X8J4cYUtkqA5ygew==", 4, 'sa'),("You", "0/9ksQuimGvltlhBS/j/xg==jz9j9Q5XHqgkaquOR7d62w==", 5, 'sa'),("April", "lVx8NGZ2u64OAkRhAHD5uA==B1K9To/NW/EUSs4CQhBDsQ==", 1, 'admin'),
("Brook", "WjxjWtPcm+vQXe52Itgj5w==xYflVVq3/dWOZCKrNPROPw==", 4, 'member'),("Bryan", "+6VnR7LRMbJzuJuROnjK1w==Lbb4XlWxEl1wPp6BQUq5bA==", 2, 'admin'),("Colin", "MlK9UHwcyhf+owfM5cvNJQ==Xdp1Rjd2dMOkeZMX307g0Q==", 1, 'admin'),
("Connie", "N27yqvAy+qMssAlGoLj8lw==oaTruuTf7Aku9eKZXrqI4w==", 1, 'member'),("Jocelyn", "b/4VY62r8rXrFo5KBzUDXA==7miLyHYhKsqOMa8yUAJrog==", 1, 'admin'),("Robert", "rmKA2h5gSPaECVyt7uc7hw==c2Mv762TEa8JmIiGVKoRcg==", 1, 'member'),
("Steve", "8gqHs3f4c2hCmNlsgWgw3A==PBCbLrGG4E+Sw7Lxo/7HeA==", 2, 'member'),("OtherTA", "SmUbMmVOG5mZzHp+9CvtVw==Ynpvfscj6QvuTuqP5ES+Xw==", 5, 'admin');
INSERT INTO main.friends
VALUES ("April", "MJ"),("Brook", "Sam"),("Adam", "Bryan"),("April", "Colin"),
("Colin","MJ"),("April","Connie"),("Colin","Connie"),("Connie","MJ"),
("April","Jocelyn"),("Colin","Jocelyn"),("Connie","Jocelyn"),("Jocelyn","MJ"),
("April","Robert"),("Colin","Robert"),("Connie","Robert"),("Jocelyn","Robert"),("MJ","Robert"),
("Adam","Steve"),("Bryan","Steve"),("OtherTA","You"),("Kevin","MJ");
INSERT INTO main.friendrequests
VALUES ("You", "Kevin"),("You", "MJ");
INSERT INTO main.registrationrequests
VALUES ("fbs", "hwfJMWs9u7VDDKnfHlEbAg==bYb21OyGkXgMzeEj35CtlA==", 5);