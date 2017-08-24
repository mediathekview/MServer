package mServer.developTest.crawler.sender.zdf;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;

import de.mediathekview.mlib.Config;
import mServer.crawler.sender.zdf.VideoDTO;
import mServer.crawler.sender.zdf.ZDFEntryDTO;
import mServer.crawler.sender.zdf.ZDFEntryTask;
import mServer.developTest.test.ZDFClientMock;

public class ZDFEntryTaskTest
{

    @After
    public void cleanUp()
    {
        Config.setStop(false);
    }

    @Test
    public void testComputeReturnVideoDTO()
    {

        final String generalUrl = "https://api.zdf.de/generalUrl";
        final String downloadUrl = "https://api.zdf.de/downloadUrl";

        final ZDFClientMock mockZdfClient = new ZDFClientMock();
        mockZdfClient.setUp(generalUrl, "/zdf/zdf_entry_general_sample.json");
        mockZdfClient.setUp(downloadUrl, "/zdf/zdf_entry_download_sample.json");

        final ZDFEntryDTO dto = new ZDFEntryDTO(generalUrl, downloadUrl);
        final ZDFEntryTask target = new ZDFEntryTask(dto, mockZdfClient.get());
        final VideoDTO actual = target.invoke();

        assertThat(actual, notNullValue());
        assertThat(actual.getDownloadDto(), notNullValue());
    }

    @Test
    public void testComputeReturnsNullIfEntryDTOIsNull()
    {
        final ZDFEntryTask target = new ZDFEntryTask(null);
        final VideoDTO actual = target.invoke();

        assertThat(actual, nullValue());
    }

    @Test
    public void testComputeReturnsNullIfStopped()
    {
        Config.setStop(true);

        final ZDFEntryDTO dto = new ZDFEntryDTO("", "");
        final ZDFEntryTask target = new ZDFEntryTask(dto);
        final VideoDTO actual = target.invoke();

        assertThat(actual, nullValue());
    }
}
