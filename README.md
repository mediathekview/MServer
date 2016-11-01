[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.org/mediathekview/MServer.svg?branch=master)](https://travis-ci.org/mediathekview/MServer)
[![Quality Gate](https://sonarqube.com/api/badges/gate?key=mediathekview%3AMServer)](https://sonarqube.com/dashboard/index/mediathekview%3AMServer)

# MServer
Server zum Steuern des Crawler. Teil von [MediathekView](https://github.com/mediathekview).

# Entwicklung

## Code auschecken
```bash
mkdir mediathekview
cd mediathekview
git clone https://github.com/mediathekview/MServer.git
git clone https://github.com/mediathekview/MSearch.git
```

## Bauen und starten an der Kommandozeile
```bash
cd MServer
./gradlew run
```

## Entwicklung mit Netbeans
* Verzeichnisse `MSearch` und `MServer` mit Netbeans öffnen

## Entwicklung mit Eclipse
* Falls noch nicht vorhanden: [Plugin buildship](https://projects.eclipse.org/projects/tools.buildship) installieren
* Projekt `MServer` als Gradle-Projekt importieren. `MSearch` wird automatisch mit importiert.

# Autor
@xaverW
