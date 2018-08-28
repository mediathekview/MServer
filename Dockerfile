FROM "openjdk:8-jre-slim"
MAINTAINER "MediathekView <info@mediathekview.de>"

# On Build
ARG VERSION
ARG BASE_DIR="/opt/MServer"

# On Run
ENV MIN_RAM "256M"
ENV MAX_RAM "2G"

RUN mkdir -p $BASE_DIR
WORKDIR $BASE_DIR

ADD ./build/distributions/MServer-$VERSION.tar.gz .
RUN mkdir config && \
    mv MServer-$VERSION/* . && \
    mv mserver.xml config/ && \
    mv upload.xml config/ && \
    mv live-streams.json config/

VOLUME /opt/MServer/config
CMD java -Xms$MIN_RAM -Xmx$MAX_RAM -jar ./MServer.jar config