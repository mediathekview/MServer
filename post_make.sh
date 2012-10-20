#!/bin/sh

# Dateien ins dist-Verzeichnis kopieren
cp -r /mnt/daten/software/java/MediathekServer/bin/* /mnt/daten/software/java/MediathekServer/dist

# für Netbeans nochmal
cp -r /mnt/daten/software/java/MediathekServer/bin/* /mnt/daten/software/java/MediathekServer/build

# Aufräumen
rm /mnt/daten/software/java/MediathekServer/dist/README.TXT


# zip erstellen
cd /mnt/daten/software/java/MediathekServer/dist/
datum=$(date +%Y.%m.%d )
zip -r MediathekServer_$datum.zip .
 