package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.WebResource;

import java.util.concurrent.RecursiveTask;
import mSearch.tool.Log;

/**
 * Searches all information required for a film
 */
public class ZDFEntryTask extends RecursiveTask<VideoDTO> {

    private static final long serialVersionUID = 1L;
    
    private final ZDFClient client;
    private final ZDFEntryDTO zdfEntryDTO;
    private final Gson gson;
    
    public ZDFEntryTask(ZDFEntryDTO aEntryDto) {
        client = new ZDFClient();
        zdfEntryDTO = aEntryDto;                
        gson = new GsonBuilder()
                .registerTypeAdapter(VideoDTO.class, new ZDFVideoDTODeserializer())
                .registerTypeAdapter(DownloadDTO.class, new ZDFDownloadDTODeserializer())
                .create();
    }
    
    @Override
    protected VideoDTO compute() {
        VideoDTO dto = null;
        try {
            // read film details
            String infoUrl = zdfEntryDTO.getEntryGeneralInformationUrl();
            WebResource webResourceInfo = client.createResource(infoUrl);
            JsonObject baseObjectInfo = client.execute(webResourceInfo);
            if(baseObjectInfo != null) {
                dto = gson.fromJson(baseObjectInfo, VideoDTO.class);

                // read download details
                String downloadUrl = zdfEntryDTO.getEntryDownloadInformationUrl();
                WebResource webResourceDownload = client.createResource(downloadUrl);
                JsonObject baseObjectDownload = client.execute(webResourceDownload);
                if(baseObjectDownload != null) {
                    DownloadDTO downloadDto = gson.fromJson(baseObjectDownload, DownloadDTO.class);
                    dto.setDownloadDto(downloadDto);
                }
            }
        } catch (JsonSyntaxException ex) {
            Log.errorLog(496583202, ex, "Exception parsing " + zdfEntryDTO.getEntryGeneralInformationUrl());
            dto = null;
        }
        
        return dto;
    }    
}
