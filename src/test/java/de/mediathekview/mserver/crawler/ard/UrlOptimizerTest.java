package de.mediathekview.mserver.crawler.ard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class UrlOptimizerTest {
  
  @Mock
  private ArdCrawler crawler;
  
  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    
  }
  
  @ParameterizedTest(name = "[{index}] adaptive={0}")
  @MethodSource("dataM3UToUrls")
  void testM3UToUrls(String adaptive, String sampleUrl, List<String> expected) {
    crawler = createCrawler();
    crawler = Mockito.mock(ArdCrawler.class);
    when(crawler.requestUrlExists(anyString())).thenReturn(true);
    UrlOptimizer urlOptimizer = new UrlOptimizer(crawler);
    
    Map<Integer, String> actual = urlOptimizer.buildUrlsFromPlaylist(adaptive, sampleUrl);
    assertThat(actual.values()).containsAll(expected);
  }
  
  @Test
  void singleTest() {
    // https://manifest-arte.akamaized.net/api/manifest/v1/Generate/f620eafe-7d6d-4965-95cd-b11aea6e65d3/VOA-STA/XQ/129139-000-A.m3u8
    // https://manifest-arte.akamaized.net/api/manifest/v1/Generate/f620eafe-7d6d-4965-95cd-b11aea6e65d3/VOA-STA/XQ/129139-000-A.m3u8
    String adaptive = "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/f620eafe-7d6d-4965-95cd-b11aea6e65d3/VOA-STA/XQ/129139-000-A.m3u8";
    String sampleUrl = "https://hrardmediathek-a.akamaihd.net/video/as/allgemein/2021_06/hrLogo_210619151025_0215626_512x288-25p-500kbit.mp4";
    crawler = createCrawler();
    //crawler = Mockito.mock(ArdCrawler.class);
    //when(crawler.requestUrlExists(anyString())).thenReturn(true);
    UrlOptimizer urlOptimizer = new UrlOptimizer(crawler);
    Map<Resolution, String> actual = urlOptimizer.buildFilmUrlFromAdaptive(adaptive, sampleUrl);
    System.out.println(urlOptimizer.printMap(actual));
  }
  
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }


  static Stream<Arguments> dataM3UToUrls() {
    return Stream.of(
        // ───────────────────────────────────────────────────────────────
        // 1. Das Erste
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://universal-vod.daserste.de/i/int/2025/04/30/DEGSM156010/,DEGSM156010_3287187_sendeton_640x360-50p-1200kbit,DEGSM156010_3287187_sendeton_480x270-50p-700kbit,DEGSM156010_3287187_sendeton_960x540-50p-1600kbit,DEGSM156010_3287187_sendeton_1280x720-50p-3200kbit,DEGSM156010_3287187_sendeton_1920x1080-50p-5000kbit,.mp4.csmil/master.m3u8",
            "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_480x270-50p-700kbit.mp4",
            List.of(
                "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_480x270-50p-700kbit.mp4",
                "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_640x360-50p-1200kbit.mp4",
                "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_960x540-50p-1600kbit.mp4",
                "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_1280x720-50p-3200kbit.mp4",
                "https://pd-videos.daserste.de/int/2025/04/30/DEGSM156010/DEGSM156010_3287187_sendeton_1920x1080-50p-5000kbit.mp4"
            )
        ),
        Arguments.of(
            "https://universal-vod.daserste.de/i/int/staging/int/2026/01/05/SWRSM206242/,SWRSM206242_3638331_sendeton_480x270-50p-700kbit.mp4,SWRSM206242_3638331_sendeton_640x360-50p-1200kbit.mp4,SWRSM206242_3638331_sendeton_960x540-50p-1600kbit.mp4,SWRSM206242_3638331_sendeton_1280x720-50p-3200kbit.mp4,SWRSM206242_3638331_sendeton_1920x1080-50p-5000kbit.mp4,.csmil/master.m3u8",
            "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_1920x1080-50p-5000kbit.mp4",
            List.of(
                "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_480x270-50p-700kbit.mp4",
                "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_640x360-50p-1200kbit.mp4",
                "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_960x540-50p-1600kbit.mp4",
                "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_1280x720-50p-3200kbit.mp4",
                "https://ctv-videos.daserste.de/int/staging/int/2026/01/05/SWRSM206242/SWRSM206242_3638331_sendeton_1920x1080-50p-5000kbit.mp4"
            )
        ),
        // ───────────────────────────────────────────────────────────────
        // 2. BR – Buchstaben-Kürzel (A,X,C,HD,E)
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://br-i.akamaihd.net/i/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_,A,X,C,HD,E,.mp4.csmil/master.m3u8",
            "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_A.mp4",
            List.of(
                "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_A.mp4",
                "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_X.mp4", // 1280
                "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_C.mp4", // 960
                "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_E.mp4", // 640
                "https://cdn-storage.br.de/b7/2026-01/09/cfb3cd10-ed78-11f0-a101-02420a000526_HD.mp4" // 1920
            )
        ),
        Arguments.of(
            "https://br-i.akamaihd.net/i/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_,A,X,C,HD,E,.mp4.csmil/master.m3u8",
            "https://cdn-storage.br.de/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_HD.mp4",
            List.of(
                "https://cdn-storage.br.de/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_E.mp4",   // 640
                "https://cdn-storage.br.de/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_C.mp4",   // 960
                "https://cdn-storage.br.de/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_X.mp4",   // 1280
                "https://cdn-storage.br.de/geo/b7/2026-01/05/8f5bb760-ea89-11f0-a101-02420a000526_HD.mp4"   // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 3. MDR
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://mdronline-vh.akamaihd.net/i/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-,c3f46785fa07,a33182bdaf26,3c67be5cd760,557cadc3dbd7,ef73d58f2f02,_b3.mp4.csmil/master.m3u8",
            "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-c3f46785fa07_b3.mp4",
            List.of(
                "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-c3f46785fa07_b3.mp4", // 640
                "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-a33182bdaf26_b3.mp4", // 960
                "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-3c67be5cd760_b3.mp4", // 1280
                "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-557cadc3dbd7_b3.mp4", // 1920
                "https://odmdr-a.akamaihd.net/mp4dyn2/b/FCMS-b306f4e4-3057-4f9a-9de3-587119512396-ef73d58f2f02_b3.mp4"
            )
        ),
        Arguments.of(
            "https://mdronline-vh.akamaihd.net/i/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-,c3f46785fa07,a33182bdaf26,3c67be5cd760,557cadc3dbd7,ef73d58f2f02,_fa.mp4.csmil/master.m3u8",
            "https://odmdr-a.akamaihd.net/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-557cadc3dbd7_fa.mp4",
            List.of(
                "https://odmdr-a.akamaihd.net/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-c3f46785fa07_fa.mp4",   // 640
                "https://odmdr-a.akamaihd.net/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-a33182bdaf26_fa.mp4",   // 960
                "https://odmdr-a.akamaihd.net/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-3c67be5cd760_fa.mp4",   // 1280
                "https://odmdr-a.akamaihd.net/mp4dyn2/f/FCMS-fa64aa93-0416-459a-bdf3-fdbf803cccdc-557cadc3dbd7_fa.mp4"    // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 4. HR
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://hrardmediathek-vh.akamaihd.net/i/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_,480x270-50p-700,1920x1080-50p-5000,1280x720-50p-3200,960x540-50p-1600,640x360-50p-1200,kbit.mp4/master.m3u8",
            "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_480x270-50p-700kbit.mp4",
            List.of(
                "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_480x270-50p-700kbit.mp4",
                "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_960x540-50p-1600kbit.mp4",
                "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_640x360-50p-1200kbit.mp4",
                "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_1280x720-50p-3200kbit.mp4",
                "https://hrardmediathek-a.akamaihd.net/odinson/geoavailability_DACH/grzimeks-vermaechtnis-wie-weit-darf-naturschutz-gehen/SVID-638971AA-EB4E-4480-BD97-42470163FDE1/ad2c8976-9701-4dd1-b415-1dec7c959c11/0239333_sendeton_1920x1080-50p-5000kbit.mp4"
            )
        ),
        Arguments.of(
            "https://hrardmediathek-vh.akamaihd.net/i/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_,640x360-50p-1200,1920x1080-50p-5000,1280x720-50p-3200,960x540-50p-1600,480x270-50p-700,kbit.mp4/master.m3u8",
            "https://hrardmediathek-a.akamaihd.net/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_1920x1080-50p-5000kbit.mp4",
            List.of(
                "https://hrardmediathek-a.akamaihd.net/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_640x360-50p-1200kbit.mp4",   // 640
                "https://hrardmediathek-a.akamaihd.net/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_960x540-50p-1600kbit.mp4",    // 960
                "https://hrardmediathek-a.akamaihd.net/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_1280x720-50p-3200kbit.mp4",   // 1280
                "https://hrardmediathek-a.akamaihd.net/odinson/show-and-unterhaltung/SVID-AD59C600-61C6-42C5-BC7F-E34AB911469E/f155cbc9-33b7-4fa1-90ac-631dfcf95709/0229913_sendeton_1920x1080-50p-5000kbit.mp4"   // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 5. SWR – Mischung aus sm/ml/xl + .l / .sm etc.
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://hlsodswr-vh.akamaihd.net/i/swrfernsehen/die-scheune/1534517,.sm,.ml,.l,.xl,.xxl,.mp4.csmil/master.m3u8",
            "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.l.mp4",
            List.of(
                "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.l.mp4",
                "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.sm.mp4",
                "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.ml.mp4",
                "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.xl.mp4",
                "https://pdodswr-a.akamaihd.net/swrfernsehen/die-scheune/1534517.xxl.mp4"
            )
        ),
        Arguments.of(
            "https://av-adaptive.swr.de/i/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter,.sm,.ml,.l,.xl,.xxl,.mp4.csmil/master.m3u8",
            "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.l.mp4",
            List.of(
                "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.l.mp4",
                "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.sm.mp4",
                "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.ml.mp4",
                "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.xl.mp4",
                "https://avdlswr-a.akamaihd.net/planet-schule/nie-wieder-keine-ahnung-malerei-der-betrachter.xxl.mp4"
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 6. Sportschau – AVC-Präfix
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://sportschau-vod.ard-mcdn.de/i/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_,AVC-360,AVC-1080,AVC-720,AVC-540,AVC-270,.mp4.csmil/master.m3u8",
            "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-360.mp4",
            List.of(
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-360.mp4", // 640
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-720.mp4", // 1280
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-540.mp4", // 960
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-270.mp4",
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/09/7e2abbd3-4d64-438c-ab13-b6d1563bec37/7e2abbd3-4d64-438c-ab13-b6d1563bec37_AVC-1080.mp4" // 1920
            )
        ),
        Arguments.of(
            "https://sportschau-vod.ard-mcdn.de/i/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_,AVC-360,AVC-1080,AVC-720,AVC-540,AVC-270,.mp4.csmil/master.m3u8",
            "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_AVC-1080.mp4",
            List.of(
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_AVC-360.mp4",   // 640
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_AVC-540.mp4",    // 960
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_AVC-720.mp4",   // 1280
                "https://sportschau-progressive.ard-mcdn.de/de/nfsk/2026/01/06/49f0de7b-ab0f-425c-8723-734cc3559848/49f0de7b-ab0f-425c-8723-734cc3559848_AVC-1080.mp4"   // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 7. RBB
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://rbbvod.akamaized.net/i/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_,hd1080-avc360,hd1080-avc270,hd1080-avc540,hd1080-avc720,hd1080-avc1080,.mp4.csmil/master.m3u8",
            "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc360.mp4",
            List.of(
                "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc360.mp4", // 640
                "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc270.mp4",
                "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc540.mp4", //960
                "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc720.mp4", // 1280
                "https://rbbmediapmdp-a.akamaihd.net/content/14/fc/14fc82d8-07ff-4d74-9584-62043970aaec/d829dcd0-eb36-11f0-9132-02420a00032c_hd1080-avc1080.mp4" // 1920
            )
        ),
        Arguments.of(
            "https://rbbvod.akamaized.net/i/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_,hd1080-avc360,hd1080-avc270,hd1080-avc540,hd1080-avc720,hd1080-avc1080,.mp4.csmil/master.m3u8",
            "https://rbbmediapmdp-a.akamaihd.net/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_hd1080-avc1080.mp4",
            List.of(
                "https://rbbmediapmdp-a.akamaihd.net/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_hd1080-avc360.mp4",  // 640
                "https://rbbmediapmdp-a.akamaihd.net/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_hd1080-avc540.mp4",  // 960
                "https://rbbmediapmdp-a.akamaihd.net/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_hd1080-avc720.mp4",  // 1280
                "https://rbbmediapmdp-a.akamaihd.net/content/42/83/428320c9-51a6-4aa0-9329-5b81be1ec5f9/8ac3982b-09be-4f0c-ad87-09948c9a0c3a_hd1080-avc1080.mp4" // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 8. WDR – Nummern-basierte Varianten
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/341/3417125/,3417125_65497916,3417125_65497917,3417125_65497915,3417125_65497918,3417125_65497914,.mp4.csmil/master.m3u8",
            "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497916.mp4",
            List.of(
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497916.mp4", // 640
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497917.mp4", //960
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497915.mp4",
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497918.mp4", //1280
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/weltweit/fsk0/341/3417125/3417125_65497914.mp4" //1920
            )
        ),
        Arguments.of(
            "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/322/3220429/,3220429_60325869,3220429_60325870,3220429_60325868,3220429_60325871,3220429_60325867,.mp4.csmil/master.m3u8",
            "https://wdr-progressive.ard-mcdn.de/medp/ondemand/de/fsk0/322/3220429/3220429_60325867.mp4",
            List.of(
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/de/fsk0/322/3220429/3220429_60325869.mp4",   // 640
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/de/fsk0/322/3220429/3220429_60325870.mp4",   // 960
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/de/fsk0/322/3220429/3220429_60325871.mp4",   // 1280
                "https://wdr-progressive.ard-mcdn.de/medp/ondemand/de/fsk0/322/3220429/3220429_60325867.mp4"    // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 9. SR – N/L/P/H Kürzel
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://srod-vh.akamaihd.net/i/media/FS/SRINFO/srinfo_18_20260110_185001_,N,L,P,H,.mp4.csmil/master.m3u8",
            "https://srstorage01-a.akamaihd.net/Video/FS/SRINFO/srinfo_18_20260110_185001_N.mp4",
            List.of(
                "https://srstorage01-a.akamaihd.net/Video/FS/SRINFO/srinfo_18_20260110_185001_N.mp4", //640
                "https://srstorage01-a.akamaihd.net/Video/FS/SRINFO/srinfo_18_20260110_185001_L.mp4", //960
                "https://srstorage01-a.akamaihd.net/Video/FS/SRINFO/srinfo_18_20260110_185001_P.mp4", //1280
                "https://srstorage01-a.akamaihd.net/Video/FS/SRINFO/srinfo_18_20260110_185001_H.mp4" //1920
            )
        ),
        Arguments.of(
            "https://srod-vh.akamaihd.net/i/media/FS/HUHMO/HUHMO-241217-115348_,N,L,P,H,.mp4.csmil/master.m3u8",
            "https://srstorage01-a.akamaihd.net/Video/FS/HUHMO/HUHMO-241217-115348_H.mp4",
            List.of(
                "https://srstorage01-a.akamaihd.net/Video/FS/HUHMO/HUHMO-241217-115348_N.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/HUHMO/HUHMO-241217-115348_L.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/HUHMO/HUHMO-241217-115348_P.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/HUHMO/HUHMO-241217-115348_H.mp4"
            )
        ),
        Arguments.of(
            "https://srod-vh.akamaihd.net/i/media/FS/MHAH/MHAH-251229-123635_,N,L,P,H,.mp4.csmil/master.m3u8",
            "https://srstorage01-a.akamaihd.net/Video/FS/MHAH/MHAH-251229-123635_H.mp4",
            List.of(
                "https://srstorage01-a.akamaihd.net/Video/FS/MHAH/MHAH-251229-123635_N.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/MHAH/MHAH-251229-123635_L.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/MHAH/MHAH-251229-123635_P.mp4",
                "https://srstorage01-a.akamaihd.net/Video/FS/MHAH/MHAH-251229-123635_H.mp4"
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 10. NDR – ln/hd/hq/mn/1080
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://adaptive.ndr.de/i/ndr/2024/1001/TV-20241001-2009-2200.,ln,1080,hd,hq,mn,.mp4.csmil/master.m3u8",
            "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.ln.mp4",
            List.of(
                "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.ln.mp4", //640
                "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.hd.mp4", //1280
                "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.hq.mp4", //960
                "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.mn.mp4", // 480
                "https://ndr-progressive.ard-mcdn.de/progressive/2024/1001/TV-20241001-2009-2200.1080.mp4" //1920
            )
        ),
        Arguments.of(
            "https://adaptive.ndr.de/i/geo/2024/1128/TV-20241128-1322-3100.,ln,1080,hd,hq,mn,.mp4.csmil/master.m3u8",
            "https://ndr-progressive.ard-mcdn.de/progressive_geo/2024/1128/TV-20241128-1322-3100.1080.mp4",
            List.of(
                "https://ndr-progressive.ard-mcdn.de/progressive_geo/2024/1128/TV-20241128-1322-3100.ln.mp4",   // 640
                "https://ndr-progressive.ard-mcdn.de/progressive_geo/2024/1128/TV-20241128-1322-3100.hq.mp4",   // 960
                "https://ndr-progressive.ard-mcdn.de/progressive_geo/2024/1128/TV-20241128-1322-3100.hd.mp4",   // 1280
                "https://ndr-progressive.ard-mcdn.de/progressive_geo/2024/1128/TV-20241128-1322-3100.1080.mp4"  // 1920
            )
        ),

        // ───────────────────────────────────────────────────────────────
        // 11. SWR aktuell – avc-Präfix mit 270/360/540/720/1080
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://av-adaptive.swr.de/i/swr/swraktuell/bw/tv/gesamtsendung/2290935,.avc-270,.avc-360,.avc-540,.avc-720,.avc-1080,.mp4.csmil/master.m3u8",
            "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-270.mp4",
            List.of(
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-270.mp4",
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-360.mp4", //640
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-540.mp4", //960
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-720.mp4", //1280
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2290935.avc-1080.mp4" //1920
            )
        ),
        Arguments.of(
            "https://av-adaptive.swr.de/i/swr/swraktuell/bw/tv/gesamtsendung/2292127,.avc-270,.avc-360,.avc-540,.avc-720,.avc-1080,.mp4.csmil/master.m3u8",
            "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2292127.avc-1080.mp4",
            List.of(
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2292127.avc-360.mp4",   // 640
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2292127.avc-540.mp4",   // 960
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2292127.avc-720.mp4",   // 1280
                "https://pdodswr-a.akamaihd.net/swr/swraktuell/bw/tv/gesamtsendung/2292127.avc-1080.mp4"   // 1920
            )
        ),
        
       // ───────────────────────────────────────────────────────────────
       // 12. RB
       // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://rbhlsod-vh.akamaihd.net/i/,clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF1920x1080-50p.mp4,clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF1280x720-50p.mp4,clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF960x540-50p.mp4,clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF640x360-50p.mp4,.csmil/master.m3u8",
            "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF1920x1080-50p.mp4",
            List.of(
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF640x360-50p.mp4", //640
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF960x540-50p.mp4", //960
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF1280x720-50p.mp4", //1280
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/pz/PZ4x6OtreF/PZ4x6OtreF1920x1080-50p.mp4" //1920
            )
        ),
        
        Arguments.of(
            "https://rbhlsod-vh.akamaihd.net/i/,clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I1920x1080-50p.mp4,clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I1280x720-50p.mp4,clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I960x540-50p.mp4,clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I640x360-50p.mp4,.csmil/master.m3u8",
            "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I1920x1080-50p.mp4",
            List.of(
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I640x360-50p.mp4",   // 640
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I960x540-50p.mp4",    // 960
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I1280x720-50p.mp4",   // 1280
                "https://rbprogressivedl-a.akamaihd.net/clips/zt/welt/zs/zShs2LSY8I/zShs2LSY8I1920x1080-50p.mp4"   // 1920
            )
        ),
        
        // ───────────────────────────────────────────────────────────────
        // 13. DRA Deutsches Rundfunkarchive
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://dra-dd.akamaized.net/video/152870/349904/HLS/17Tagungder4VolkskammerderDDR-17Tagungder4VolkskammerderDDR_152870_349904_master.m3u8",
            "",
            List.of(
                "https://dra-dd.akamaized.net/video/152870/349904/mp4/17Tagungder4VolkskammerderDDR-17Tagungder4VolkskammerderDDR_152870_349904_vod.360.MP4",
                "https://dra-dd.akamaized.net/video/152870/349904/mp4/17Tagungder4VolkskammerderDDR-17Tagungder4VolkskammerderDDR_152870_349904_vod.540.MP4",
                "https://dra-dd.akamaized.net/video/152870/349904/mp4/17Tagungder4VolkskammerderDDR-17Tagungder4VolkskammerderDDR_152870_349904_vod.720.MP4",
                "https://dra-dd.akamaized.net/video/152870/349904/mp4/17Tagungder4VolkskammerderDDR-17Tagungder4VolkskammerderDDR_152870_349904_vod.1080.MP4"
            )
        ),
        
        // ───────────────────────────────────────────────────────────────
        // 14. ZDF
        // ───────────────────────────────────────────────────────────────
        Arguments.of(
            "https://zdfvod.akamaized.net/i/mp4/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud,_508k_p9,_808k_p11,_1628k_p13,_3328k_p15,_6628k_p61,v17.mp4.csmil/master.m3u8",
            "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_508k_p9v17.mp4",
            List.of(
                "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_508k_p9v17.mp4", // 270 low
                "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_808k_p11v17.mp4", // 360 high
                "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_1628k_p13v17.mp4", // 540 veryhigh
                "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_3328k_p15v17.mp4", // 720 hd
                "https://nrodlzdf-a.akamaihd.net/none/zdf/25/12/251218_trailer_kudamm77_hero_kud/1/251218_trailer_kudamm77_hero_kud_6628k_p61v17.mp4" // 1080 fhd
            )
        )
 

    );
  }
}
