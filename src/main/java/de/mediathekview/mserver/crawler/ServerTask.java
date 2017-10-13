package de.mediathekview.mserver.crawler;


public class ServerTask implements Runnable{
    @Override
    public void run() {
        CrawlerManager.getInstance().startCrawlers();
    }
}
