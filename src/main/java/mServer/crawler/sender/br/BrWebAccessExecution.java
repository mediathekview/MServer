/*
 * BrWebAccessExecution.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.10.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package mServer.crawler.sender.br;

import java.net.MalformedURLException;

import com.google.gson.JsonSyntaxException;

@FunctionalInterface
public interface BrWebAccessExecution {
    void run() throws JsonSyntaxException,
                      MalformedURLException;
}