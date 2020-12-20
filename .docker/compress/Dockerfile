FROM alpine

RUN apk --no-cache add xz bzip2 gzip inotify-tools coreutils bash

USER 1000

WORKDIR /tmp

RUN mkdir /tmp/in && \
    touch /tmp/in/filmliste.id && \
    mkdir /tmp/work && \
    mkdir /tmp/output

COPY entrypoint.sh .
ENTRYPOINT ["./entrypoint.sh"]
