#!/bin/sh

# Dateien ins dist-Verzeichnis kopieren
cp -r /mnt/daten/software/Mediathek/MServer/res/* /mnt/daten/software/Mediathek/MServer/dist

# für Netbeans nochmal
cp -r /mnt/daten/software/Mediathek/MServer/res/* /mnt/daten/software/Mediathek/MServer/build

# Aufräumen
rm /mnt/daten/software/Mediathek/MServer/dist/README.TXT

# release
relNr=$(cat /mnt/daten/software/Mediathek/MServer/src/version.properties | grep BUILD | sed 's#BUILD=##g')

# zip erstellen
cd /mnt/daten/software/Mediathek/MServer/dist/
zip -r MServer_$relNr.zip .
