# vertx-demo

A _very_ simple vertx demo project. Just run:
```
mvn clean install
```
Then execute the fatjar in the target dir with java -jar

It's now hooked into Cassandra, so the _get_ endpoint won't work unless you have a node started with a keyspace called _dev_. 

It responds to a few endpoints:

- /ping -- returns _pong_
- /get/_param_ -- executes the param as a Cassandra CQL statement
- /post -- takes a block of json in the payload, and just bounces it back

Hints:

- docker run -p 127.0.0.1:9042:9042 --name my-cass -d cassandra:3.0
- cqlsh -f ./db.cql

