package mServer.crawler;

import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mServer.tool.UrlService;

import org.junit.*;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AddToFilmlistTest {

    private ListeFilme list;
    private ListeFilme listToAdd;
    private UrlService mockUrlService;
    
    @Before
    public void setUp() {
        mockUrlService = Mockito.mock(UrlService.class);

        listToAdd = new ListeFilme();
        list = new ListeFilme();
        list.add(new DatenFilm("BR", "Topic 1", "urL", "Title 1", "http://film1.mp4", "", "", "", 12, ""));
        list.add(new DatenFilm("BR", "Topic 2", "urL", "Title 1", "http://film2.mp4", "", "", "", 12, ""));
    }
    
    private void setUpUrlOnline() {
        when(mockUrlService.laengeLong(anyString())).thenReturn((long)22);
    }

    private void setUpUrlOffline() {
        when(mockUrlService.laengeLong(anyString())).thenReturn((long)-1);
    }

    @Test
    public void testAddOldListDifferentSenderAndUrlAdded() {
        listToAdd.add(new DatenFilm("ARD", "Topic 1", "urL", "Title 1", "http://otherfilm.mp4", "", "", "", 12, ""));
        setUpUrlOnline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTopicAndUrlAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 3", "urL", "Title 1", "http://otherfilm.mp4", "", "", "", 12, ""));
        setUpUrlOnline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 2", "http://otherfilm.mp4", "", "", "", 12, ""));
        setUpUrlOnline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentUrlNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 1", "http://otherfilm.mp4", "", "", "", 12, ""));
        setUpUrlOnline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
    
    @Test
    public void testAddOldListDifferentTitleNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 3", "http://film1.mp4", "", "", "", 12, ""));
        setUpUrlOnline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() {
        listToAdd.add(new DatenFilm("BR", "Topic 1", "urL", "Title 2", "http://otherfilm.mp4", "", "", "", 12, ""));
        setUpUrlOffline();
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd, mockUrlService);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
}