CREATE TABLE IF NOT EXISTS CO2concentration (
         observationID BINARY(16)   NOT NULL,
	 sensorID      VARCHAR(30)  NOT NULL,
         result        VARCHAR(30)  ,
	 simpleResult  VARCHAR(30)  ,
         time          TIMESTAMP(3)  ,
         location      POINT NOT NULL ,
         featureOfInterest  VARCHAR(30)  ,
         PRIMARY KEY  (observationID)
       );

CREATE TABLE IF NOT EXISTS location (
         observationID BINARY(16)   NOT NULL,
	 sensorID      VARCHAR(30)  NOT NULL,
         result        VARCHAR(30)  ,
	 simpleResult  VARCHAR(30)  ,
         time          TIMESTAMP(3)  ,
         location      POINT NOT NULL ,
         featureOfInterest  VARCHAR(30)  ,
         PRIMARY KEY  (observationID)
       );

CREATE TABLE IF NOT EXISTS employee (
         employeeCode  BINARY(16)   NOT NULL,
	 entityID      VARCHAR(30)  NOT NULL,
         name          VARCHAR(30)  ,
	 surname       VARCHAR(30)  ,
	 age           TINYINT,
         PRIMARY KEY  (employeeCode)
       );

CREATE TABLE IF NOT EXISTS room (
         roomCode  BINARY(16)   NOT NULL,
	 entityID  VARCHAR(30)  NOT NULL,
         location  POLYGON NOT NULL,
         PRIMARY KEY  (roomCode)
       );
