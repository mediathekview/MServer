package mServer.tool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

public class UrlServiceTest {
    
    private HttpURLConnection mockConnection;
    private UrlService target;
    
    public UrlServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws MalformedURLException, IOException {
        mockConnection = Mockito.mock(HttpURLConnection.class);
        UrlBuilder mockUrlBuilder = Mockito.mock(UrlBuilder.class);
        when(mockUrlBuilder.openConnection(anyString())).thenReturn(mockConnection);  
        
        target = new UrlService(mockUrlBuilder);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testLaengeLongWithContentSizeOfAFilm() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLengthLong()).thenReturn(269000000L);
        
        long actual = target.laengeLong("http://myurl.de/film");                
        
        assertThat(actual, equalTo(269L));
    }

    @Test
    public void testLaengeLongWithContentSizeOfAPlaylist() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLengthLong()).thenReturn(785000L);
        
        long actual = target.laengeLong("http://myurl.de/playlist");                
        
        assertThat(actual, equalTo(0L));       
    }
    
    @Test
    public void testLaengeUrlNotFound() throws IOException {
        when(mockConnection.getResponseCode()).thenReturn(404);
        
        long actual = target.laengeLong("http://myurl.de/notfound");                
        
        assertThat(actual, equalTo(0L));       
    }
}
