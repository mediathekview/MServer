FROM openjdk:8-jre-slim
LABEL maintainer="MediathekView <info@mediathekview.de>"

# On Build
ARG VERSION

RUN mkdir -p "/opt/mserver"

WORKDIR /opt/mserver
ADD ./build/distributions/MServer-$VERSION.tar.gz .
RUN chown -R 1000:1000 /opt/mserver

USER 1000

# On Run
ENV MIN_RAM "256M"
ENV MAX_RAM "2G"


RUN mkdir data \
    && mkdir data/filmlisten \
    && mv MServer-$VERSION/MServer.jar . \
    && mv MServer-$VERSION/lib . \
    && mv MServer-$VERSION/mserver.xml data/ \
    && mv MServer-$VERSION/live-streams.json data/ \
    && rm -r MServer-$VERSION

CMD java -Xms$MIN_RAM -Xmx$MAX_RAM -jar ./MServer.jar data
