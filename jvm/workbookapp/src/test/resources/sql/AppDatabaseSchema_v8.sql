CREATE TABLE IF NOT EXISTS "language_entity" (
	"id"	INTEGER,
	"slug"	TEXT NOT NULL UNIQUE,
	"name"	TEXT NOT NULL,
	"gateway"	INTEGER NOT NULL DEFAULT 0,
	"anglicized"	TEXT NOT NULL,
	"direction"	TEXT NOT NULL,
	"region"	clob,
	PRIMARY KEY("id" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "dublin_core_entity" (
	"id"	INTEGER,
	"conformsTo"	TEXT NOT NULL,
	"creator"	TEXT NOT NULL,
	"description"	TEXT NOT NULL,
	"format"	TEXT NOT NULL,
	"identifier"	TEXT NOT NULL,
	"issued"	TEXT NOT NULL,
	"language_fk"	INTEGER NOT NULL,
	"modified"	TEXT NOT NULL,
	"publisher"	TEXT NOT NULL,
	"subject"	TEXT NOT NULL,
	"type"	TEXT NOT NULL,
	"title"	TEXT NOT NULL,
	"version"	TEXT NOT NULL,
	"path"	TEXT NOT NULL,
	"derivedFrom_fk"	INTEGER,
	"license"	clob NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	UNIQUE("language_fk","identifier","version","creator","derivedFrom_fk"),
	FOREIGN KEY("derivedFrom_fk") REFERENCES "dublin_core_entity"("id"),
	FOREIGN KEY("language_fk") REFERENCES "language_entity"("id")
);
CREATE TABLE IF NOT EXISTS "rc_link_entity" (
	"rc1_fk"	INTEGER NOT NULL,
	"rc2_fk"	INTEGER NOT NULL,
	PRIMARY KEY("rc1_fk","rc2_fk"),
	CONSTRAINT "directionless" CHECK("rc1_fk" < "rc2_fk"),
	FOREIGN KEY("rc1_fk") REFERENCES "dublin_core_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("rc2_fk") REFERENCES "dublin_core_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "collection_entity" (
	"id"	INTEGER,
	"parent_fk"	INTEGER,
	"source_fk"	INTEGER,
	"label"	TEXT NOT NULL,
	"title"	TEXT NOT NULL,
	"slug"	TEXT NOT NULL,
	"sort"	INTEGER NOT NULL,
	"dublin_core_fk"	INTEGER NOT NULL,
	"modified_ts"	clob DEFAULT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	UNIQUE("slug","dublin_core_fk","label"),
	FOREIGN KEY("parent_fk") REFERENCES "collection_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("source_fk") REFERENCES "collection_entity"("id"),
	FOREIGN KEY("dublin_core_fk") REFERENCES "dublin_core_entity"("id")
);
CREATE TABLE IF NOT EXISTS "content_entity" (
	"id"	INTEGER,
	"collection_fk"	INTEGER NOT NULL,
	"type_fk"	INTEGER NOT NULL,
	"label"	TEXT NOT NULL,
	"selected_take_fk"	INTEGER,
	"start"	INTEGER NOT NULL,
	"sort"	INTEGER NOT NULL,
	"text"	TEXT,
	"format"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("selected_take_fk") REFERENCES "take_entity"("id") ON DELETE SET NULL,
	FOREIGN KEY("type_fk") REFERENCES "content_type"("id") ON DELETE RESTRICT,
	FOREIGN KEY("collection_fk") REFERENCES "collection_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "content_type" (
	"id"	INTEGER,
	"name"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	UNIQUE("name" COLLATE NOCASE) ON CONFLICT IGNORE
);
CREATE TABLE IF NOT EXISTS "content_derivative" (
	"id"	INTEGER,
	"content_fk"	INTEGER NOT NULL,
	"source_fk"	INTEGER NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("source_fk") REFERENCES "content_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("content_fk") REFERENCES "content_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "take_entity" (
	"id"	INTEGER,
	"content_fk"	INTEGER NOT NULL,
	"filename"	TEXT NOT NULL,
	"path"	TEXT NOT NULL,
	"number"	INTEGER NOT NULL,
	"created_ts"	TEXT NOT NULL,
	"deleted_ts"	TEXT DEFAULT NULL,
	"played"	INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("content_fk") REFERENCES "content_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "marker_entity" (
	"id"	INTEGER,
	"take_fk"	INTEGER NOT NULL,
	"number"	INTEGER NOT NULL,
	"position"	INTEGER NOT NULL,
	"label"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("take_fk") REFERENCES "take_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "resource_link" (
	"id"	INTEGER,
	"resource_content_fk"	INTEGER NOT NULL,
	"content_fk"	INTEGER,
	"collection_fk"	INTEGER,
	"dublin_core_fk"	INTEGER NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("resource_content_fk") REFERENCES "content_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("collection_fk") REFERENCES "collection_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("dublin_core_fk") REFERENCES "dublin_core_entity"("id"),
	CONSTRAINT "ensure_at_least_one_not_null" CHECK(("collection_fk" IS NOT NULL) OR ("content_fk" IS NOT NULL)),
	CONSTRAINT "prevent_both_not_null" CHECK(("collection_fk" IS NULL) OR ("content_fk" IS NULL)),
	UNIQUE("resource_content_fk","content_fk","collection_fk") ON CONFLICT IGNORE,
	FOREIGN KEY("content_fk") REFERENCES "content_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "subtree_has_resource" (
	"collection_fk"	INTEGER NOT NULL,
	"dublin_core_fk"	INTEGER NOT NULL,
	PRIMARY KEY("collection_fk","dublin_core_fk") ON CONFLICT IGNORE,
	FOREIGN KEY("dublin_core_fk") REFERENCES "dublin_core_entity"("id") ON DELETE CASCADE,
	FOREIGN KEY("collection_fk") REFERENCES "collection_entity"("id") ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "audio_plugin_entity" (
	"id"	INTEGER,
	"name"	TEXT NOT NULL,
	"version"	TEXT NOT NULL,
	"bin"	TEXT NOT NULL,
	"args"	TEXT NOT NULL,
	"record"	INTEGER NOT NULL DEFAULT 0,
	"edit"	INTEGER NOT NULL DEFAULT 0,
	"path"	TEXT,
	"mark"	int NOT NULL DEFAULT 0,
	PRIMARY KEY("id" AUTOINCREMENT),
	UNIQUE("name","version")
);
CREATE TABLE IF NOT EXISTS "preferences" (
	"key"	TEXT NOT NULL UNIQUE,
	"value"	TEXT
);
CREATE TABLE IF NOT EXISTS "installed_entity" (
	"name"	TEXT NOT NULL,
	"version"	INTEGER NOT NULL,
	PRIMARY KEY("name")
);
CREATE TABLE IF NOT EXISTS "translation_entity" (
	"id"	integer NOT NULL,
	"source_fk"	int NOT NULL,
	"target_fk"	int NOT NULL,
	"modified_ts"	clob DEFAULT NULL,
	"source_rate"	double DEFAULT 1.0,
	"target_rate"	double DEFAULT 1.0,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("target_fk") REFERENCES "language_entity",
	FOREIGN KEY("source_fk") REFERENCES "language_entity",
	UNIQUE("source_fk","target_fk")
);
CREATE INDEX IF NOT EXISTS "idx_content_entity_collection_start" ON "content_entity" (
	"collection_fk",
	"start",
	"type_fk"
);
CREATE INDEX IF NOT EXISTS "idx_content_derivative_content" ON "content_derivative" (
	"content_fk"
);
CREATE INDEX IF NOT EXISTS "idx_content_derivative_source" ON "content_derivative" (
	"source_fk"
);
CREATE INDEX IF NOT EXISTS "idx_resource_link_collection" ON "resource_link" (
	"collection_fk"
);
CREATE INDEX IF NOT EXISTS "idx_resource_link_content" ON "resource_link" (
	"content_fk"
);

INSERT INTO "installed_entity" VALUES ('DATABASE',8);
