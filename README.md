[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.org/mediathekview/MServer.svg?branch=master)](https://travis-ci.org/mediathekview/MServer)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=mediathekview%3AMServer&metric=alert_status)](https://sonarcloud.io/dashboard?id=mediathekview%3AMServer)

# MServer
Server zum crawlen der Mediatheken. Teil von [MediathekView](https://github.com/mediathekview).

# Entwicklung

## Code auschecken
```bash
mkdir mediathekview
cd mediathekview
git clone https://github.com/mediathekview/MServer.git
git clone https://github.com/mediathekview/Mlib.git
```

## Bauen und starten an der Kommandozeile
```bash
cd MServer
./gradlew run
```

## Entwicklung mit Netbeans
* Verzeichnisse `MLib` und `MServer` mit Netbeans öffnen

## Entwicklung mit Eclipse
* Falls noch nicht vorhanden: [Plugin buildship](https://projects.eclipse.org/projects/tools.buildship) installieren
* Projekt `MServer` als Gradle-Projekt importieren. `MLib` wird automatisch mit importiert.

