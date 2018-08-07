CREATE TABLE if NOT EXISTS "LANGUAGE ENTITY"
(
  id INTEGER primary key autoincrement,
  slug VARCHAR(8)  not null UNIQUE,
  name VARCHAR(50) not null,
  isGateway INT default 0 not null,
  anglicizedName VARCHAR(50) not null
);

CREATE TABLE if NOT EXISTS "USER ENTITY"
(
  id INTEGER primary key autoincrement,
  audioHash VARCHAR(50) not null UNIQUE ,
  audioPath VARCHAR(255) not null UNIQUE ,
  imgPath VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE if NOT EXISTS "USER PREFERENCES ENTITY"
(
  userfk INTEGER PRIMARY KEY references "USER ENTITY" ON DELETE CASCADE ,
  sourceLanguagefk INTEGER references "LANGUAGE ENTITY" ON DELETE CASCADE ,
  targetLanguagefk INTEGER references "LANGUAGE ENTITY" ON DELETE CASCADE
);

CREATE TABLE if NOT EXISTS "USER LANGUAGES ENTITY"
(
  userfk INTEGER references "USER ENTITY" ON DELETE CASCADE ,
  languagefk INTEGER references "LANGUAGE ENTITY" ON DELETE CASCADE ,
  isSource INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (userfk, languagefk, isSource)
);

