package mServer.crawler;

import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddToFilmlistTest {

    private final ListeFilme list;
    private final ListeFilme listToAdd;
    
    // TODO HTTPConnection mocken für online-Prüfung...
    
    public AddToFilmlistTest() {
        listToAdd = new ListeFilme();
        list = new ListeFilme();
        list.add(new DatenFilm("BR", "Topic 1", "urL", "Title 1", "http://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc571S/_AJS/_ArP_28p9U1S/bbb9193b-c81b-4f09-901a-14a44c93d0aa_C.mp4", "", "", "", 12, ""));
        list.add(new DatenFilm("BR", "Topic 2", "urL", "Title 1", "http://cdn-storage.br.de/iLCpbHJGNLT6NK9HsLo6s61luK4C_2rc5H1S/_-iS/_Ary_ygd_U1S/83ef216b-5a03-4d87-b043-6a58dcdfdd5e_C.mp4", "", "", "", 12, ""));
    }
    
    @Test
    public void testAddOldListDifferentSenderAndUrlAdded() {
        listToAdd.add(new DatenFilm("ARD", "Topic 1", "urL", "Title 1", "http://mediastorage01.sr-online.de/Video/UD/RDS/rds2015_FANFRAGEN_KIZ_2_20150807_143001_L.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(3, list.size());
    }
    
    @Test
    public void testAddOldListDifferentTopicAndUrlAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 3", "urL", "Title 1", "http://mediastorage01.sr-online.de/Video/UD/RDS/rds2015_FANFRAGEN_KIZ_2_20150807_143001_L.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(3, list.size());
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 2", "http://mediastorage01.sr-online.de/Video/UD/RDS/rds2015_FANFRAGEN_KIZ_2_20150807_143001_L.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(3, list.size());
    }
    
    @Test
    public void testAddOldListDifferentUrlNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 1", "http://mediastorage01.sr-online.de/Video/UD/RDS/rds2015_FANFRAGEN_KIZ_2_20150807_143001_L.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(2, list.size());
    }
    
    @Test
    public void testAddOldListDifferentTitleNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 3", "http://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc571S/_AJS/_ArP_28p9U1S/bbb9193b-c81b-4f09-901a-14a44c93d0aa_C.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(2, list.size());
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 2", "http://mediastorage01.sr-online.de/Video/UD/RDS/rds2015_FANFRAGEN_KIZ_2_20150807_143001_L1.mp4", "", "", "", 12, ""));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertEquals(2, list.size());
    }
}