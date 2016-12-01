# vertx-demo

A _very_ simple vertx demo project. Just run:
```
mvn clean install
```
Then execute the fatjar in the target dir with java -jar

It responds to a few endpoints:

- /ping -- returns _pong_
- /get/_param_ -- returns the contents of the param
- /post -- takes a block of json in the payload, and just bounces it back
