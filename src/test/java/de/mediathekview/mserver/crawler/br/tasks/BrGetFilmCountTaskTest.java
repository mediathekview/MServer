/*
 * BrGetFilmCountTaskTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 18.11.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.tasks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class BrGetFilmCountTaskTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8589); // No-args constructor defaults to port 8080

    @Test
    public void testGetCorrectGraphQLResponseRight() throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedJSONresult = new String(Files.readAllBytes(Paths.get(classLoader.getResource("de/mediathekview/mserver/crawler/br/tasks/filmCountResultGraphQL.json").toURI()))); 
        
        wireMockRule.stubFor(post(urlEqualTo("/myBrRequets"))
                    .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(expectedJSONresult)));
        
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Collection<MessageListener> nachrichten = new ArrayList<>() ;
        MessageListener nachricht = new MessageListener() {
            
            @Override
            public void consumeMessage(Message arg0, Object... arg1) {
                // TODO Auto-generated method stub
                
            }
        }; 
        nachrichten.add(nachricht);
        
        Collection<SenderProgressListener> fortschritte = new ArrayList<>();
        SenderProgressListener fortschritt = new SenderProgressListener() {
            
            @Override
            public void updateProgess(Sender aSender, Progress aCrawlerProgress) {
                // TODO Auto-generated method stub
                
            }
        }; 
        fortschritte.add(fortschritt);
        
        MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
        
        BrCrawler crawler = new BrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
        
        BrGetFilmCountTask filmCount = new BrGetFilmCountTask(crawler);
        ExecutorService lassLaufen = Executors.newSingleThreadExecutor();
        Integer graphqlJsonResult = lassLaufen.submit(filmCount).get();
        
        assertEquals(Integer.valueOf(19801), graphqlJsonResult);
    }
    
}
