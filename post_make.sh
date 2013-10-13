#!/bin/sh

# Dateien ins dist-Verzeichnis kopieren
cp -r /mnt/daten/software/Mediathek/MServer/bin/* /mnt/daten/software/Mediathek/MServer/dist
cp -r /mnt/daten/software/Mediathek/MServer/src /mnt/daten/software/Mediathek/MServer/dist
cp -r /mnt/daten/software/Mediathek/MServer/dist/lib/* /mnt/daten/software/Mediathek/MServer/libs

# für Netbeans nochmal
cp -r /mnt/daten/software/Mediathek/MServer/bin/* /mnt/daten/software/Mediathek/MServer/build

# Aufräumen
rm /mnt/daten/software/Mediathek/MServer/dist/README.TXT

# release
relNr=$(cat /mnt/daten/software/Mediathek/MServer/src/version.properties | grep BUILD | sed 's#BUILD=##g')

# zip erstellen
cd /mnt/daten/software/Mediathek/MServer/dist/
zip -r MServer_$relNr.zip .
