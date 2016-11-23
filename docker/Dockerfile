# vertx demo: 
# docker build -t vertx-demo .
# docker run -t -i -p 8080:8080 vertx-demo &
# docker ps
# docker kill <name>
# docker exec -ti <name> /bin/bash

FROM oracle_jdk:latest

MAINTAINER Steve Bell

EXPOSE 8080

COPY ./*-fat.jar /

# Launch the verticle
WORKDIR /
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar *-fat.jar"]                                    
