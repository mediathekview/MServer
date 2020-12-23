FROM alpine

RUN apk --no-cache add xz bzip2 gzip inotify-tools coreutils bash \
    && chown 1000:1000 /tmp \
    && mkdir /tmp/in \
    && touch /tmp/in/filmliste.id \
    && mkdir /tmp/work \
    && mkdir /tmp/output \
    && chown -R 1000:1000 /tmp/in \
    && chown -R 1000:1000 /tmp/work \
    && chown -R 1000:1000 /tmp/output

WORKDIR /tmp
USER 1000

COPY entrypoint.sh .
ENTRYPOINT ["./entrypoint.sh"]
