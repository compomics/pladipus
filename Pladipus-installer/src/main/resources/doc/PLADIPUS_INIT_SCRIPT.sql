CREATE SCHEMA pladipus;

USE pladipus;

CREATE  TABLE IF NOT EXISTS users (
  `user_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `user_name` TEXT NOT NULL ,
  `contact` TEXT NOT NULL ,
  `password` TEXT NOT NULL ,
  PRIMARY KEY (`user_id`) );

CREATE  TABLE IF NOT EXISTS user_roles (
  `user_id` INT(11) NOT NULL ,
  `role_id` INT(11) NOT NULL 
 );

CREATE  TABLE IF NOT EXISTS roles (
  `role_id` INT(11) NOT NULL ,
  `role` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`role_id`));

CREATE  TABLE IF NOT EXISTS run (
  `run_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `title` TEXT NULL DEFAULT NULL ,
  `user_name` TEXT NULL DEFAULT NULL ,
  `template` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`run_id`) );

CREATE  TABLE IF NOT EXISTS process (
  `process_id` INT(11) NOT NULL AUTO_INCREMENT ,
  `run_id` INT(11) NOT NULL ,
  `failcount` INT(11) NULL DEFAULT '0' ,
  `stepcount` INT(11) NULL DEFAULT '0' ,
  `state` TEXT NULL DEFAULT NULL ,
  `complete` TINYINT(1) NULL DEFAULT '0' ,
  `on_queue` TINYINT(1) NULL DEFAULT '0' ,
  PRIMARY KEY (`PROCESS_ID`) );

CREATE  TABLE IF NOT EXISTS process_parameters (
  `process_id` INT(11) NOT NULL ,
  `name` TEXT NOT NULL ,
  `value` TEXT NOT NULL);

INSERT INTO roles(role_id,ROLE) VALUES(1,'ADMIN');
INSERT INTO roles(role_id,ROLE) VALUES(2,'USER');

INSERT INTO users(user_name,contact,password) VALUES('pladmin','pladipus@ugent.be','qh1mFAwsB9evmvD+gXeoY58dGU9ze72oIb4648t66BGt4jzV9TM/72z8GezdPKN3');
  
INSERT INTO user_roles(user_id,role_id) VALUES(1,1);