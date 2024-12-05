CREATE TABLE IF NOT EXISTS game (
    game_id int NOT NULL AUTO_INCREMENT,
    startDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endDate DATE NOT NULL,
    endTime TIME NOT NULL,
    seed int NOT NULL,
    isRow boolean NOT NULL,
    endTimeRow int NOT NULL,
    PRIMARY KEY (game_id)     
);

CREATE TABLE IF NOT EXISTS teams (
    fetchr_team_id varchar(200) NOT NULL, --Change to max length in-game (Dont forget some leeway)
    displayName varchar(255) NOT NULL,
    PRIMARY KEY (fetchr_team_id)
);

CREATE TABLE IF NOT EXISTS players (
    mc_uuid char(36) NOT NULL,
    displayName varchar (20) DEFAULT 'Steve',
    headFileLocation varchar(255) NULL,
    availableOnDisk boolean NOT NULL,
    PRIMARY KEY (mc_uuid)
);

CREATE TABLE IF NOT EXISTS teamInGame (
    game_id int NOT NULL,
    teams_id varchar(200) NOT NULL, -- Change according to above comment
    player_id char(36) NOT NULL,
    UNIQUE (game_id, teams_id, player_id),
    CONSTRAINT FK_game_id_game FOREIGN KEY (game_id)
    REFERENCES game(game_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_teams_id_teams FOREIGN KEY (teams_id)
    REFERENCES teams(fetchr_team_id),
    CONSTRAINT FK_player_id_players FOREIGN KEY (player_id)
    REFERENCES players(mc_uuid)
);

CREATE TABLE IF NOT EXISTS categories (
    fetchr_category_id varchar(100) NOT NULL, --Need to lookup
    displayName varchar(100) DEFAULT 'Placeholder Category',
    PRIMARY KEY (fetchr_category_id)
);

CREATE TABLE IF NOT EXISTS items (
    mc_id varchar(100) NOT NULL,
    displayName varchar(100) DEFAULT 'Red Bed',
    itemFileLocation varchar(255) NULL,
    availableOnDisk boolean NOT NULL,
    PRIMARY KEY (mc_id)
);

CREATE TABLE IF NOT EXISTS itemInCategory (
    mc_id varchar(100) NOT NULL,
    fetchr_category_id varchar(100) NOT NULL,
    itemWeightCategory tinyint DEFAULT 1,
    CONSTRAINT FK_mc_id_itemInCategory_items FOREIGN KEY (mc_id)
    REFERENCES items(mc_id),
    CONSTRAINT FK_fetchrCategory_itemInCategory_categories FOREIGN KEY (fetchr_category_id)
    REFERENCES categories(fetchr_category_id)
);

CREATE TABLE IF NOT EXISTS itemsInGame (
    item_id varchar(100) NOT NULL,
    game_id int NOT NULL,
    player_id char(36) NOT NULL,
    timeGotten TIME NOT NULL,
    CONSTRAINT FK_item_id_itemsInGame_items FOREIGN KEY (item_id)
    REFERENCES items(mc_id),
    CONSTRAINT FK_game_id_itemsInGame_game FOREIGN KEY (game_id)
    REFERENCES game(game_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FK_player_id_itemsInGame_players FOREIGN KEY (player_id)
    REFERENCES players(mc_uuid)
);