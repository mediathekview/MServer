/*
 * BrWebAccessHelper.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.10.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.tasks;

import java.net.MalformedURLException;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSyntaxException;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.BrWebAccessExecution;

public class BrWebAccessHelper<V> {

    public static void handleWebAccessExecution(Logger log, AbstractCrawler crawler, BrWebAccessExecution action) {
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
    
    private static void handleError(String errorMessage, Exception e, Logger log, AbstractCrawler crawler) {
        log.error(errorMessage, e);
        crawler.incrementAndGetErrorCount();
        crawler.printErrorMessage();
    }
    
}
