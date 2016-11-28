/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.crawler;

/**
 *
 * @author emil
 */
public class CrawlerConfig {

    //alle Programmeinstellungen
    public static String proxyUrl = "";
    public static int proxyPort = -1;
    public static String importLive = ""; // live-streams
    public static String importUrl_2__anhaengen = "";
    public static String importOld = ""; // alte Liste importieren
    public static String importAkt = ""; // akteuelle Liste eines anderen Crawler importieren
    public static String importUrl_1__anhaengen = "";
    public static final int LOAD_SHORT = 0;
    public static final int LOAD_LONG = 1;
    public static int senderLoadHow = LOAD_SHORT;
    public static final int LOAD_MAX = 2;
    public static boolean updateFilmliste = false; // die bestehende Filmliste wird aktualisiert und bleibt erhalten
    public static boolean orgFilmlisteErstellen = false; // dann wird eine neue Org-Liste angelegt, typ. die erste Liste am Tag
    public static String orgFilmliste = ""; // OrgFilmliste, zum Erstellen des Diff, angelegt wird sie immer im Ordner der Filmlisten, wenn leer wird die eigene Org-Liste gesucht
    public static String[] nurSenderLaden = null; // es wird nur dieser Sender geladen => "senderAllesLaden"=false, "updateFillmliste"=true
    // Verzeichnis zum Speichern der Programmeinstellungen
    public static String dirFilme = ""; // Pfad mit den Filmlisten

}
