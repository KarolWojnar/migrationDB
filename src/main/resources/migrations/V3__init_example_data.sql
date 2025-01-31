INSERT INTO teams (id, name) VALUES
    (1, 'FC Barcelona'),
    (2, 'Real Madrid'),
    (3, 'Manchester City'),
    (4, 'Liverpool FC'),
    (5, 'Paris Saint-Germain'),
    (6, 'Atlético Madrid'),
    (7, 'Chelsea FC'),
    (8, 'Olympique Lyon'),
    (9, 'Juventus'),
    (10, 'Borussia Dortmund');

INSERT INTO leagues (id, name) VALUES
    (1, 'La Liga'),
    (2, 'Premier League'),
    (3, 'Ligue 1'),
    (4, 'Serie A'),
    (5, 'Bundesliga'),
    (6, 'Champions League');

INSERT INTO leagues_teams (team_id, league_id) VALUES
    (1, 1),
    (2, 1),
    (3, 2),
    (4, 2),
    (5, 3),
    (6, 1),
    (7, 2),
    (8, 3),
    (9, 4),
    (10, 5),
    (1, 6),
    (2, 6),
    (3, 6),
    (4, 6),
    (5, 6),
    (6, 6),
    (7, 6),
    (8, 6),
    (9, 6),
    (10, 6);
