#!/bin/sh

dir=`dirname "$0"`
cd "$dir"

# Dateien ins dist-Verzeichnis kopieren
cp -r res/* dist

# für Netbeans nochmal
cp -r res/* build

# Aufräumen
rm dist/README.TXT

# Anlegen
mkdir dist/info

# release
relNr=$(cat src/version.properties | grep BUILD | sed 's#BUILD=##g')
datum=$(date +%d.%m.%Y )
echo Datum: $datum >> dist/info/$relNr.build
echo MServer Buildnummer: $relNr >> dist/info/$relNr.build

# zip erstellen
cd dist/
zip -r MServer_$relNr.zip .

cd $OLDPWD
