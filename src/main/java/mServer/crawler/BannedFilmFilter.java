package mServer.crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.tool.MserverDaten;
import mServer.tool.MserverKonstanten;

public class BannedFilmFilter {
  private List<String> bannedTitles = new ArrayList<String>();
	
  public BannedFilmFilter() {
	Log.progress("create BannedFilmFilter from " + MserverDaten.system[MserverKonstanten.SYSTEM_BANNEDFILMLIST_NR] );
	bannedTitles = new ArrayList<String>();
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreamFromPath(MserverDaten.system[MserverKonstanten.SYSTEM_BANNEDFILMLIST_NR])))) {
		String line = "";
		while ((line = reader.readLine()) != null) {
        if (line.trim().length() > 0) {
          bannedTitles.add(line.trim());
          Log.progress("add entry to bannedFilmList");
        }
      }
    }catch (FileNotFoundException e) {
        Log.errorLog(-1, e);
    } catch (IOException e) {
    	Log.errorLog(-1, e);
    }
  }
	
  public boolean isBanned(final DatenFilm film) {
  	for (String title : bannedTitles) {
	  	if (film.arr[DatenFilm.FILM_TITEL].toUpperCase().contains(title.toUpperCase())) {
		  	return true;
		  }
	  }
    return false;
  }
  
  public static InputStream getInputStreamFromPath(String path) throws IOException {
	  InputStream is;
	  String protocol = path.replaceFirst("^(\\w+):.+$", "$1").toLowerCase();
	  switch (protocol) {
	    case "http":
	    case "https":
	      HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
	      int code = connection.getResponseCode();
	      if (code >= 400) throw new IOException("Server returned error code #" + code);
	      is = connection.getInputStream();
	      String contentEncoding = connection.getContentEncoding();
	      if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip"))
	        is = new GZIPInputStream(is);
	        break;
	    case "file":
	      is = new URL(path).openStream();
	        break;
	    case "classpath":
	      is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path.replaceFirst("^\\w+:", ""));
	      break;
	    default:
	      throw new IOException("Missed or unsupported protocol in path '" + path + "'");
	  }
	  return is;
	}
}
