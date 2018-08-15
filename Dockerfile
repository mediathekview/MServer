FROM "8-jre-slim"
MAINTAINER "MediathekView <info@mediathekview.de>"

# On Build
ARG required VERSION
ARG BASE_DIR="/opt/MServer"

# On Run
ENV MIN_RAM "256M"
ENV MAX_RAM "2G"

RUN "mkdir $BASE_DIR"
WORKDIR $BASE_DIR

ADD ./build/distributions/MServer-$VERSION.tar.gz
RUN "mkdir config && \"
    "mv *.xml config/ && \"
    "mv live-streams.json config/"
VOLUME "config/"

CMD ["java -Xms$MIN_RAM -Xmx$MAX_RAM -jar ./MServer.jar $BASE_DIR"]