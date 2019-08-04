FROM openjdk:11

ARG VERSION

ENV JAVA_OPTS="-Xmx500m"

USER mediathekview

RUN mkdir /opt/mserver
WORKDIR /opt/mserver

ADD target/MServer-$VERSION-bin.tar.bz2 /opt/mserver

# Move the files out of the useless subdirectory
RUN mv /opt/mserver/MServer-$VERSION/*(DN) /opt/mserver
RUN rm -rf /opt/mserver/MServer-$VERSION

# Sources don't needed in docker, so lets delet em:
RUN rm /opt/mserver/MServer-sources.jar

VOLUME /opt/mserver/filmlists

ENTRYPOINT java ${JAVA_OPTS} -jar /opt/mserver/MServer.jar
