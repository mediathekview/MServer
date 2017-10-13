/*
 * BRMainTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 29.09.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.mediathekview.mserver.base.Consts;

public class BRMainTest {

    private static final String QUERY =
            "{\"query\":\"query SeriesIndexRefetchQuery(\\n  $seriesFilter: SeriesFilter\\n) {\\n  viewer {\\n    ...SeriesIndex_viewer_19SNIy\\n    id\\n  }\\n}\\n\\nfragment SeriesIndex_viewer_19SNIy on Viewer {\\n  seriesIndexAllSeries: allSeries(first: 1000, orderBy: TITLE_ASC, filter: $seriesFilter) {\\n    edges {\\n      node {\\n        __typename\\n        id\\n        title\\n        ...SeriesTeaserBox_node\\n        ...TeaserListItem_node\\n      }\\n    }\\n  }\\n}\\n\\nfragment SeriesTeaserBox_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on SeriesInterface {\\n    ...SubscribeAction_series\\n    subscribed\\n    title\\n  }\\n}\\n\\nfragment TeaserListItem_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on ClipInterface {\\n    title\\n  }\\n}\\n\\nfragment TeaserImage_creativeWorkInterface on CreativeWorkInterface {\\n  id\\n  kicker\\n  title\\n   }\\n\\nfragment SubscribeAction_series on SeriesInterface {\\n  id\\n  subscribed\\n}\\n\",\"variables\":{\"seriesFilter\":{\"title\":{\"startsWith\":\"*\"},\"audioOnly\":{\"eq\":false},\"status\":{\"id\":{\"eq\":\"Status:http://ard.de/ontologies/lifeCycle#published\"}}}}}";

    
    public static void main(String[] args) {

        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(Consts.BR_API_URL);
        // TODO Auto-generated method stub

        final String response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(QUERY, MediaType.APPLICATION_JSON_TYPE), String.class);
        
        System.out.println(response);
    }

}
