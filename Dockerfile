FROM openjdk:11

# The server version like in the pom file
ARG VERSION

ENV JAVA_OPTS="-Xmx500m"

RUN mkdir /opt/mserver

VOLUME /opt/mserver/filmlists

RUN useradd mediathekview
RUN chown -R mediathekview:mediathekview /opt/mserver

USER mediathekview

WORKDIR /opt/mserver

ADD target/MServer-$VERSION-bin.tar.bz2 /opt/mserver

# Move the files out of the useless subdirectory
RUN mv /opt/mserver/MServer-$VERSION/* /opt/mserver
RUN rm -rf /opt/mserver/MServer-$VERSION

# Sources don't needed in docker, so lets delet em:
RUN rm /opt/mserver/MServer-sources.jar


ENTRYPOINT java ${JAVA_OPTS} -jar /opt/mserver/MServer.jar
