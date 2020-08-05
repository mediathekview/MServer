FROM openjdk:11

# The server version like in the pom file
ARG VERSION

ENV JAVA_OPTS="-Xmx4G"

RUN mkdir /opt/mserver
ADD target/MServer-$VERSION-bin.tar.bz2 /opt/mserver
RUN chown -R 1000:1000 /opt/mserver

USER 1000

WORKDIR /opt/mserver

# Move the files out of the useless subdirectory and create fimlists dir
RUN mv /opt/mserver/MServer-$VERSION/* /opt/mserver && \
    rm -rf /opt/mserver/MServer-$VERSION && \
    rm /opt/mserver/MServer-sources.jar && \
    mkdir filmlists

ENTRYPOINT java ${JAVA_OPTS} -jar /opt/mserver/MServer.jar
