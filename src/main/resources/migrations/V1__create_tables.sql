CREATE TABLE players (
        id BIGINT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        position VARCHAR(30)
);

CREATE TABLE teams (
        id BIGINT PRIMARY KEY,
        name VARCHAR(255) NOT NULL
);

CREATE TABLE leagues (
        id BIGINT PRIMARY KEY,
        name VARCHAR(100) NOT NULL
);

CREATE TABLE contracts (
       id BIGINT PRIMARY KEY,
       player_id BIGINT UNIQUE NOT NULL,
       team_id BIGINT NOT NULL,
       salary DECIMAL(10,2) NOT NULL,
       start_date DATE NOT NULL,
       end_date DATE NOT NULL,
       FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
       FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);

CREATE TABLE leagues_teams (
       team_id BIGINT NOT NULL,
       league_id BIGINT NOT NULL,
       FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
       FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE
);