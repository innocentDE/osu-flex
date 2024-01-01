CREATE DATABASE flex;

USE flex;

CREATE TABLE servers (
    serverId BIGINT PRIMARY KEY,
    channelId BIGINT
);

CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(255),
    bestId VARCHAR(255)
);

CREATE TABLE user_servers (
    userId INT,
    serverId BIGINT,
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (serverId) REFERENCES servers(serverId)
);

CREATE TABLE credentials (
    id INT PRIMARY KEY AUTO_INCREMENT,
    createdAt DATETIME,
    accessToken VARCHAR(1500)
);