FROM openjdk:8-jre-slim
LABEL maintainer="MediathekView <info@mediathekview.de>"

# On Build
ARG VERSION
USER 1000

# On Run
ENV MIN_RAM "256M"
ENV MAX_RAM "2G"

RUN mkdir -p "/opt/mserver"
WORKDIR /opt/mserver

ADD ./build/distributions/MServer-$VERSION.tar.gz .
RUN mkdir data && \
    mkdir data/filmlisten && \
    mv MServer-$VERSION/MServer.jar . && \
    mv -r MServer-$VERSION/lib . && \
    mv MServer-$VERSION/mserver.xml data/ && \
    mv MServer-$VERSION/live-streams.json data/ && \
    rm -R MServer-$VERSION

CMD java -Xms$MIN_RAM -Xmx$MAX_RAM -jar ./MServer.jar data
