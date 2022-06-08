CREATE TABLE IF NOT EXISTS fruits (
    id uuid DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS accounts (
    id uuid DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_username ON accounts(LOWER(username));
CREATE UNIQUE INDEX ux_name ON fruits(LOWER(name));
