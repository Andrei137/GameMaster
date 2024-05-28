@echo off
cls
cd ..
java -cp .;src;exceptions;interfaces;misc;models;repository;services;lib/commons-lang3-3.14.0.jar;lib/mysql-connector-java-8.3.0.jar;lib/jansi-2.4.0.jar src.Main
cd scripts