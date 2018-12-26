# AkkaWithKafkaCassandra
This is a sample POC created using AKKA, Kafka and Cassandra.

### Following AKKA Actors are created in this sample:
1. Kafka Supervisor Actor - Responsible to start Consumer Actor. There are 4  consumer actors created in this sample.
2. Kafka Consumer Actor - Responsible to collect message from Kafka and pass it to Message handler Actor.
3. Message Handler Actor - Responsible to process message and pass it to Persistence Actor.
4. Persistence Actor - Persists messages in Cassandra DB. 

### Kafka Producer:
KafkaProducer is repsonsible to produce and send messages to Kafka.

### Cassandra DB table:
CREATE TABLE TempData
( 
	id TIMEUUID, 
	deviceid text,
	temperature double,
	PRIMARY KEY (deviceid, id)
)WITH CLUSTERING ORDER BY (id DESC);

* Partitioning key - deviceid and
* Clustering key - id



