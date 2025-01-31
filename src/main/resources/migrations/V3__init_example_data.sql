INSERT INTO teams (id, name) VALUES
    (1, 'FC Barcelona'),
    (2, 'Real Madrid'),
    (3, 'Manchester City'),
    (4, 'Liverpool FC'),
    (5, 'Paris Saint-Germain');

INSERT INTO leagues (id, name) VALUES
    (1, 'La Liga'),
    (2, 'Premier League'),
    (3, 'Ligue 1'),
    (4, 'Serie A'),
    (5, 'Bundesliga');

INSERT INTO leagues_teams (team_id, league_id) VALUES
    (1, 1),
    (2, 1),
    (3, 2),
    (4, 2),
    (5, 3);
