/*
 * BrWebAccessExecution.java
 *
 * Projekt    : MServer
 * erstellt am: 07.10.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import java.net.MalformedURLException;

@FunctionalInterface
public interface BrWebAccessExecution {
  void run() throws MalformedURLException;
}
