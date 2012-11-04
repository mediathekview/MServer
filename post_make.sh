#!/bin/sh

# Dateien ins dist-Verzeichnis kopieren
cp -r /mnt/daten/software/java/MediathekServer/bin/* /mnt/daten/software/java/MediathekServer/dist
cp -r /mnt/daten/software/java/MediathekServer/src /mnt/daten/software/java/MediathekServer/dist

# für Netbeans nochmal
cp -r /mnt/daten/software/java/MediathekServer/bin/* /mnt/daten/software/java/MediathekServer/build

# Aufräumen
rm /mnt/daten/software/java/MediathekServer/dist/README.TXT

# release
relNr=$(cat /mnt/daten/software/java/Mediathek_3/src/version.properties | grep BUILD | sed 's#BUILD=##g')

# zip erstellen
cd /mnt/daten/software/java/MediathekServer/dist/
zip -r MediathekServer_$relNr.zip .
