/*
 * BrWebAccessHelper.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.10.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package mServer.crawler.sender.br;

import java.net.MalformedURLException;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSyntaxException;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;

public class BrWebAccessHelper {

    private BrWebAccessHelper() {}
  
    public static void handleWebAccessExecution(Logger log, MediathekReader crawler, BrWebAccessExecution action) {
        try {
            action.run();
        } catch (final JsonSyntaxException jsonSyntaxException) {
            handleError("The json syntax for the BR task to get all Sendungen has an error.", jsonSyntaxException, log, crawler);
        } catch (final MalformedURLException malformedURLException) {
            handleError("The URL given to WebAccess is not valid.", malformedURLException, log, crawler);
        } catch (final IllegalArgumentException illegalArgumentException) {
            handleError("URL given to the WebAccess was Null", illegalArgumentException, log, crawler);
        }
    }
    
    private static void handleError(String errorMessage, Exception e, Logger log, MediathekReader crawler) {
        log.error(errorMessage, e);
        FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
    }
}
