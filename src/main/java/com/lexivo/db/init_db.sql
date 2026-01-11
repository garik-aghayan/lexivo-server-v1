ROLLBACK;
BEGIN;

CREATE DOMAIN ID as VARCHAR(200);
CREATE DOMAIN SHORT_TEXT as VARCHAR(255);

CREATE TABLE IF NOT EXISTS users(
    email SHORT_TEXT PRIMARY KEY,
    name SHORT_TEXT NOT NULL,
    password_hash SHORT_TEXT NOT NULL,
    confirmed BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS email_confirmation_codes(
    email SHORT_TEXT PRIMARY KEY,
    code CHAR(7) NOT NULL,
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS logs(
    created_at BIGINT NOT NULL,
    category SHORT_TEXT NOT NULL,
    stack_trace TEXT[],
    messages TEXT[] NOT NULL,
    user_email SHORT_TEXT REFERENCES users(email)
);

CREATE TABLE IF NOT EXISTS lang(
    name SHORT_TEXT PRIMARY KEY,
    name_native SHORT_TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS dict(
    id ID PRIMARY KEY,
    lang SHORT_TEXT REFERENCES lang(name),
    user_email SHORT_TEXT REFERENCES users(email) NOT NULL
);

CREATE TABLE IF NOT EXISTS word(
    id ID PRIMARY KEY,
    dict_id ID REFERENCES dict(id) ON DELETE CASCADE NOT NULL,
    user_email SHORT_TEXT REFERENCES users(email) NOT NULL,
    type SHORT_TEXT NOT NULL,
    level SHORT_TEXT NOT NULL,
    gender SHORT_TEXT,
    practice_countdown INT NOT NULL,
    native TEXT,
    native_details TEXT,
    plural TEXT,
    past1 TEXT,
    past2 TEXT,
    description TEXT,
    description_details TEXT
);

CREATE TABLE IF NOT EXISTS grammar(
    id ID PRIMARY KEY,
    dict_id ID REFERENCES dict(id) ON DELETE CASCADE NOT NULL,
    user_email SHORT_TEXT REFERENCES users(email) NOT NULL,
    header SHORT_TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS grammar_submenu(
    id ID PRIMARY KEY,
    grammar_id ID REFERENCES grammar(id) ON DELETE CASCADE NOT NULL,
    user_email SHORT_TEXT REFERENCES users(email) NOT NULL,
    header SHORT_TEXT NOT NULL,
    explanations_json TEXT NOT NULL,
    examples_json TEXT NOT NULL
);

COMMIT;