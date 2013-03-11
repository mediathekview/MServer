#!/bin/sh

# Dateien ins dist-Verzeichnis kopieren
cp -r /mnt/daten/software/Mediathek/MediathekServer/bin/* /mnt/daten/software/Mediathek/MediathekServer/dist
cp -r /mnt/daten/software/Mediathek/MediathekServer/src /mnt/daten/software/Mediathek/MediathekServer/dist

# für Netbeans nochmal
cp -r /mnt/daten/software/Mediathek/MediathekServer/bin/* /mnt/daten/software/Mediathek/MediathekServer/build

# Aufräumen
rm /mnt/daten/software/Mediathek/MediathekServer/dist/README.TXT

# release
relNr=$(cat /mnt/daten/software/Mediathek/Mediathek/src/version.properties | grep BUILD | sed 's#BUILD=##g')

# zip erstellen
cd /mnt/daten/software/Mediathek/MediathekServer/dist/
zip -r MediathekServer_$relNr.zip .
