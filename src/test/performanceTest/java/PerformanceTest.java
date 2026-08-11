import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;
import mServer.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * A test to test the performance of the crawler.
 */
public class PerformanceTest
{
    private static final String TEST_REOSURCES_FOLDERPATH = "/";
    private EtmMonitor performanceMonitor;
    private Path testConfigPath;

    @BeforeEach
    public void setUp() throws URISyntaxException
    {
        BasicEtmConfigurator.configure();
        performanceMonitor = EtmManager.getEtmMonitor();
        performanceMonitor.start();

        testConfigPath = Path.of(getClass().getResource(TEST_REOSURCES_FOLDERPATH).toURI());
    }

    @AfterEach
    public void tearDown()
    {
        performanceMonitor.stop();
    }

    @Test
    public void testCrawlerPerformance()
    {
        Main.main(new String[]{testConfigPath.toAbsolutePath().toString()});
        performanceMonitor.render(new SimpleTextRenderer());
    }
}
