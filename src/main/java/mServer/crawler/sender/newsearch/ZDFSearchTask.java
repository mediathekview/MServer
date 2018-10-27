package mServer.crawler.sender.newsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

import com.google.gson.JsonObject;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import mServer.tool.MserverDaten;

public class ZDFSearchTask extends RecursiveTask<Collection<VideoDTO>> {

  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_RESULT = "http://zdf.de/rels/search/results";

  private static final long serialVersionUID = 1L;

  private final Collection<VideoDTO> filmList;
  private final ZDFClient client;

  private int page;
  private final int days;

  public ZDFSearchTask(int aDays) {
    super();

    filmList = new ArrayList<>();
    client = new ZDFClient();
    page = 1;
    days = aDays;
  }

  @Override
  protected Collection<VideoDTO> compute() {
    if (!Config.getStop()) {
      try {
        Collection<ZDFSearchPageTask> subTasks = ConcurrentHashMap.newKeySet();
        JsonObject baseObject;

        do {
          baseObject = client.executeSearch(page, days, 0);

          if (baseObject != null) {
            ZDFSearchPageTask task = new ZDFSearchPageTask(baseObject);
            task.fork();
            subTasks.add(task);
            if (MserverDaten.debug) {
              Log.sysLog("SearchTask " + task.hashCode() + " added.");
            }
          }

          page++;
        } while (!Config.getStop() && hasNextPage(baseObject));
        subTasks.forEach(t -> filmList.addAll(t.join()));
        if (MserverDaten.debug) {
          Log.sysLog("All SearchTasks finished.");
        }

      } catch (Exception ex) {
        Log.errorLog(496583201, ex);
      }
    }
    return filmList;
  }

  private static boolean hasNextPage(final JsonObject baseObject) {
    return baseObject != null
            && baseObject.has(JSON_ELEMENT_NEXT)
            && baseObject.has(JSON_ELEMENT_RESULT)
            && baseObject.getAsJsonArray(JSON_ELEMENT_RESULT).size() > 0;
  }
}
