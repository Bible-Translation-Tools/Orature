CREATE TABLE IF NOT EXISTS language_entity (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  slug          TEXT  NOT NULL UNIQUE,
  name          TEXT NOT NULL,
  gateway       INTEGER DEFAULT 0 NOT NULL,
  anglicized    TEXT NOT NULL,
  direction     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS dublin_core_entity (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    conformsTo  TEXT NOT NULL,
    creator     TEXT NOT NULL,
    description TEXT NOT NULL,
    format      TEXT NOT NULL,
    identifier  TEXT NOT NULL,
    issued      TEXT NOT NULL,
    language_fk INTEGER NOT NULL REFERENCES language_entity(id),
    modified    TEXT NOT NULL,
    publisher   TEXT NOT NULL,
    subject     TEXT NOT NULL,
    type        TEXT NOT NULL,
    title       TEXT NOT NULL,
    version     TEXT NOT NULL,
    path        TEXT NOT NULL,
    derivedFrom_fk INTEGER REFERENCES dublin_core_entity(id),
    UNIQUE (language_fk, identifier, version, creator, derivedFrom_fk)
);

CREATE TABLE IF NOT EXISTS rc_link_entity (
    rc1_fk      INTEGER NOT NULL REFERENCES dublin_core_entity(id) ON DELETE CASCADE,
    rc2_fk      INTEGER NOT NULL REFERENCES dublin_core_entity(id) ON DELETE CASCADE,
    PRIMARY KEY (rc1_fk, rc2_fk),
    CONSTRAINT directionless CHECK (rc1_fk < rc2_fk)
);

CREATE TABLE IF NOT EXISTS collection_entity (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    parent_fk       INTEGER REFERENCES collection_entity(id) ON DELETE CASCADE,
    source_fk       INTEGER REFERENCES collection_entity(id),
    chunked         INTEGER DEFAULT NULL,
    label           TEXT NOT NULL,
    title           TEXT NOT NULL,
    slug            TEXT NOT NULL,
    sort            INTEGER NOT NULL,
    dublin_core_fk  INTEGER NOT NULL REFERENCES dublin_core_entity(id),
    UNIQUE (slug, dublin_core_fk, label)
);

CREATE TABLE IF NOT EXISTS content_entity (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    collection_fk    INTEGER NOT NULL REFERENCES collection_entity(id) ON DELETE CASCADE,
    type_fk          INTEGER NOT NULL REFERENCES content_type(id) ON DELETE RESTRICT,
    label            TEXT NOT NULL,
    selected_take_fk INTEGER REFERENCES take_entity(id) ON DELETE SET NULL,
    start            INTEGER NOT NULL,
    sort             INTEGER NOT NULL,
    text             TEXT,
    format           TEXT
);
CREATE INDEX IF NOT EXISTS idx_content_entity_collection_start ON content_entity (collection_fk, start, type_fk);

CREATE TABLE IF NOT EXISTS content_type (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    name             TEXT NOT NULL,
    UNIQUE (name COLLATE NOCASE) ON CONFLICT IGNORE
);

CREATE TABLE IF NOT EXISTS content_derivative (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    content_fk       INTEGER NOT NULL REFERENCES content_entity(id) ON DELETE CASCADE,
    source_fk        INTEGER NOT NULL REFERENCES content_entity(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_content_derivative_content ON content_derivative (content_fk);
CREATE INDEX IF NOT EXISTS idx_content_derivative_source ON content_derivative (source_fk);

CREATE TABLE IF NOT EXISTS take_entity (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    content_fk       INTEGER NOT NULL REFERENCES content_entity(id) ON DELETE CASCADE,
    filename         TEXT NOT NULL,
    path             TEXT NOT NULL,
    number           INTEGER NOT NULL,
    created_ts       TEXT NOT NULL,
    deleted_ts       TEXT DEFAULT NULL,
    played           INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS marker_entity (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    take_fk          INTEGER NOT NULL REFERENCES take_entity(id) ON DELETE CASCADE,
    number           INTEGER NOT NULL,
    position         INTEGER NOT NULL,
    label            TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS resource_link (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    resource_content_fk INTEGER NOT NULL REFERENCES content_entity(id) ON DELETE CASCADE,
    content_fk          INTEGER REFERENCES content_entity(id) ON DELETE CASCADE,
    collection_fk       INTEGER REFERENCES collection_entity(id) ON DELETE CASCADE,
    dublin_core_fk      INTEGER NOT NULL REFERENCES dublin_core_entity(id),
    UNIQUE (resource_content_fk, content_fk, collection_fk) ON CONFLICT IGNORE,
    CONSTRAINT ensure_at_least_one_not_null
        CHECK ((collection_fk is NOT NULL) or (content_fk is NOT NULL)),
    CONSTRAINT prevent_both_not_null
        CHECK ((collection_fk is NULL) or (content_fk is NULL))
);
CREATE INDEX IF NOT EXISTS idx_resource_link_collection ON resource_link (collection_fk);
CREATE INDEX IF NOT EXISTS idx_resource_link_content ON resource_link (content_fk);

CREATE TABLE IF NOT EXISTS subtree_has_resource (
  collection_fk       INTEGER NOT NULL REFERENCES collection_entity (id) ON DELETE CASCADE,
  dublin_core_fk      INTEGER NOT NULL REFERENCES dublin_core_entity (id) ON DELETE CASCADE,
  PRIMARY KEY (collection_fk, dublin_core_fk) ON CONFLICT IGNORE
);

CREATE TABLE IF NOT EXISTS audio_plugin_entity (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    name                TEXT NOT NULL,
    version             TEXT NOT NULL,
    bin                 TEXT NOT NULL,
    args                TEXT NOT NULL,
    record              INTEGER DEFAULT 0 NOT NULL,
    edit                INTEGER DEFAULT 0 NOT NULL,
    mark                INTEGER DEFAULT 0 NOT NULL,
    path                TEXT,
    UNIQUE (name, version)
);

CREATE TABLE IF NOT EXISTS preferences (
    key                 TEXT NOT NULL UNIQUE,
    value               TEXT
);

CREATE TABLE IF NOT EXISTS installed_entity (
    name                TEXT PRIMARY KEY NOT NULL,
    version             INTEGER NOT NULL
);
