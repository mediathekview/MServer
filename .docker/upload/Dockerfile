FROM python:3-alpine3.9
LABEL maintainer="Nicklas Wiegandt <nicklas@wiegandt.eu>"

WORKDIR /usr/src/app

COPY . .

RUN mkdir /tmp/in \
    && chown -R 1000:1000 /tmp/in \
    && pip install --no-cache-dir -r requirements.txt

ENV MC_ENDPOINT=""
ENV MC_ACCESS_KEY=""
ENV MC_SECRET_KEY=""
ENV MC_BUCKET_NAME=""

USER 1000

CMD ["sh", "-c", "python ./FileWatcher.py ${MC_ENDPOINT} ${MC_ACCESS_KEY} ${MC_SECRET_KEY} ${MC_BUCKET_NAME} /tmp/in"]