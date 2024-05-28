-- USER
INSERT INTO USER 
VALUES ('1', 'root', 'root', 'admin@gamemaster.ro', false, 'admin');

INSERT INTO USER 
VALUES ('2', 'Ubisoft', '1234', 'ubisoft@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('3', 'Team Cherry', '1234', 'teamcherry@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('4', 'From Software', '1234', 'fromsoft@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('5', '18Light Game', '1234', '18lightgame@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('6', 'Remedy Entertainment', '1234', 'remedyentertainment@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('7', '505 Games', '1234', '505games@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('8', 'Activision', '1234', 'activision@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('9', 'Deep Silver', '1234', 'deepsilver@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('10', 'Square Enix', '1234', 'squareenix@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('11', 'Electronic Arts', '1234', 'electronicarts@contact.com', false, 'provider');

INSERT INTO USER
VALUES ('12', 'Andrei', '1234', 'andrei.fabian188@gmail.com', false, 'client');

INSERT INTO USER
VALUES ('13', 'Giulian', '1234', 'buzatu.giulian@yahoo.com', false, 'client');

INSERT INTO USER
VALUES ('14', 'Robert', '1234', 'robert13@gmail.com', false, 'client');

INSERT INTO USER
VALUES ('15', 'Vlad', '1234', 'grigore.vlad@yahoo.com', false, 'client');


-- CLIENT
INSERT INTO CLIENT
VALUES ('12', 'Andrei', 'Neculae', '0726538199');

INSERT INTO CLIENT
VALUES ('13', 'Giulian', 'Buzatu', '0756181299');

INSERT INTO CLIENT
VALUES ('14', 'Robert', 'Schmidt', '0749100372');

INSERT INTO CLIENT
VALUES ('15', 'Vlad', 'Grigore', '0714910139');


-- PROVIDER
INSERT INTO PROVIDER
VALUES ('2', 'https://www.ubisoft.com/', 'developer');

INSERT INTO PROVIDER
VALUES ('3', 'https://www.teamcherry.com.au/', 'developer');

INSERT INTO PROVIDER
VALUES ('4', 'https://www.fromsoftware.jp/ww/', 'developer');

INSERT INTO PROVIDER
VALUES ('5', 'https://18light.cc/en/', 'developer');

INSERT INTO PROVIDER
VALUES ('6', 'https://www.remedygames.com/', 'developer');

INSERT INTO PROVIDER
VALUES ('7', 'https://505games.com/', 'publisher');

INSERT INTO PROVIDER
VALUES ('8', 'https://www.activision.com/activision', 'publisher');

INSERT INTO PROVIDER
VALUES ('9', 'https://www.deepsilver.com/en/', 'publisher');

INSERT INTO PROVIDER
VALUES ('10', 'https://square-enix-games.com/en_US/home', 'publisher');

INSERT INTO PROVIDER
VALUES ('11', 'https://www.ea.com/', 'publisher');


-- ADMIN
INSERT INTO ADMIN 
VALUES ('1', '30');


-- GAME
INSERT INTO GAME
VALUES ('1', '2', '2', 'Assassin''s Creed Syndicate', 39.99, '2015-10-23', true, 'game');

INSERT INTO GAME
VALUES ('2', '2', '2', 'Assassin''s Creed Syndicate Jack the Ripper', 14.99, '2015-12-15', true, 'dlc');

INSERT INTO GAME
VALUES ('3', '2', '2', 'Assassin''s Creed Syndicate The Last Maharaja', 6.99, '2016-03-01', true, 'dlc');

INSERT INTO GAME
VALUES ('4', '2', null, 'Assassin''s Creed Syndicate The Dreadful Crimes', null, null, true, 'dlc');

INSERT INTO GAME
VALUES ('5', '3', '3', 'Hollow Knight', 14.79, '2017-02-24', true, 'game');

INSERT INTO GAME
VALUES ('6', '3', '3', 'Hollow Knight Godmaster', 0, '2018-08-23', true, 'dlc');

INSERT INTO GAME
VALUES ('7', '3', null, 'Hollow Knight Silksong', null, null, true, 'game');

INSERT INTO GAME
VALUES ('8', '4', '10', 'Elden Ring', 59.99, '2022-02-25', true, 'game');

INSERT INTO GAME
VALUES ('9', '4', null, 'Elden Ring Shadow of the Erdtree', null, null, true, 'dlc');

INSERT INTO GAME
VALUES ('10', '5', '9', 'Pronty', 14.99, '2021-11-19', true, 'game');

INSERT INTO GAME
VALUES ('11', '6', '6', 'Alan Wake', 12.49, '2010-05-14', true, 'game');

INSERT INTO GAME
VALUES ('12', '6', '7', 'Alan Wake the Signal', 0, '2010-10-12', true, 'dlc');

INSERT INTO GAME
VALUES ('13', '6', null, 'Alan Wake 2', null, null, true, 'game');


-- DLC
INSERT INTO DLC
VALUES ('2', '1');

INSERT INTO DLC
VALUES ('3', '1');

INSERT INTO DLC
VALUES ('4', '1');

INSERT INTO DLC
VALUES ('6', '5');

INSERT INTO DLC
VALUES ('9', '8');

INSERT INTO DLC
VALUES ('12', '11');


-- PURCHASE
INSERT INTO PURCHASE
VALUES ('5', '12', '2017-02-24', 14.79);

INSERT INTO PURCHASE
VALUES ('6', '12', '2018-08-23', 0);

INSERT INTO PURCHASE
VALUES ('8', '13', '2023-02-18', 49.99);

INSERT INTO PURCHASE
VALUES ('10', '14', '2021-11-19', 14.99);


-- WISHLIST
INSERT INTO WISHLIST
VALUES ('10', '12', '2021-11-19');

INSERT INTO WISHLIST
VALUES ('1', '15', '2024-05-26');


-- CONTRACT
INSERT INTO CONTRACT
VALUES ('5', '9', 'accepted', '2024-05-15', '2025-05-15');

INSERT INTO CONTRACT
VALUES ('6', '7', 'accepted', '2024-02-15', '2026-07-15');

INSERT INTO CONTRACT
VALUES ('4', '10', 'accepted', '2023-10-04', '2026-10-04');

INSERT INTO CONTRACT
VALUES ('3', '8', 'pending', '2024-05-28', '2025-05-28');