FROM "openjdk:8-jre-slim"
MAINTAINER "MediathekView <info@mediathekview.de>"

# On Build
ARG VERSION
ARG BASE_DIR="/opt/mserver"

# On Run
ENV MIN_RAM "256M"
ENV MAX_RAM "2G"

RUN mkdir -p $BASE_DIR
WORKDIR $BASE_DIR

ADD ./build/distributions/MServer-$VERSION.tar.gz .
RUN mkdir data && \
    mv MServer-$VERSION/MServer.jar . && \
    mv -r MServer-$VERSION/lib . && \
    mv MServer-$VERSION/mserver.xml data/ && \
    mv MServer-$VERSION/live-streams.json data/ && \
    rm -R MServer-$VERSION

CMD java -Xms$MIN_RAM -Xmx$MAX_RAM -jar ./MServer.jar data
