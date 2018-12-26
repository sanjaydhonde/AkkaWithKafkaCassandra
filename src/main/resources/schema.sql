CREATE TABLE TempData
( 
	id TIMEUUID, 
	deviceid text,
	temperature double,
	PRIMARY KEY (deviceid, id))WITH CLUSTERING ORDER BY (id DESC);
