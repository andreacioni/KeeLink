DROP TABLE IF EXISTS Keepass;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users(
  UserId varchar(255) NOT NULL PRIMARY KEY,
  LastAccess timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  SidCreated int unsigned NOT NULL DEFAULT 1
) ENGINE=MyISAM DEFAULT CHARSET utf8;

CREATE TABLE Keepass (
  SessionId varchar(255) NOT NULL PRIMARY KEY,
  Username longtext,
  Psw longtext,
  CreationDate timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UserId varchar(255) NOT NULL,
  PublicKey varchar(512) NOT NULL,
  FOREIGN KEY (UserId) REFERENCES USER(UserId)
) ENGINE=MyISAM DEFAULT CHARSET utf8;