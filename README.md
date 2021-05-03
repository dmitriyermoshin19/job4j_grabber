[![Build Status](https://travis-ci.org/dmitriyermoshin19/job4j_grabber.svg?branch=master)](https://travis-ci.org/dmitriyermoshin19/job4j_grabber)
[![codecov](https://codecov.io/gh/dmitriyermoshin19/job4j_grabber/branch/master/graph/badge.svg)](https://codecov.io/gh/dmitriyermoshin19/job4j_grabber)
# job4j_grabber

#### Description of the project.
This project represents basic HTML grabber. The project is a job parser from the site sql.ru with recording them in the database and displaying them in an orderly manner in the console. The implementation is made so that the parser checks for updates after a specified period of time.

#### Functionality:
- Grabbing programmer vacancies from sql.ru
- CRUD operations for vacancies
- Loading the HTML page with vacancies using simple HTTP Server

#### Settings
- JDBC connection detail
- Run intervals
- parsing address data for connecting to the database are set in the configuration file - grabber.properties.

#### Used technologies
- JSOUP for parsing
- liquibase for create schemas  
- JDBC for manipulating with DB(Postgres)
- Quartz library for scheduling 
- Slf4j for logging
- Junit for Testing
- travic
- jacoco
- maven

#### output to the console of the found vacancies:

![GitHub Logo](images/Grabber.png)

#### starting the Scheduler

![GitHub Logo](images/Scheduler.png)

#### Contacts
Telegram: https://t.me/dmtriiii