package com.example.linkedinlearningdownloader.downloader;

import com.example.linkedinlearningdownloader.source.CrawlService;
import com.example.linkedinlearningdownloader.source.MaterialSource;
import com.example.linkedinlearningdownloader.source.MaterialSourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final MaterialSourceFactory materialSourceFactory;

    private final CrawlService crawlService;

    //TODO: Can be async
    @SneakyThrows
    @Synchronized
    public void download(List<String> urlList) {

        crawlService.setup();

        int i = 0;
        for(String url: urlList) {

            try{
                MaterialSource materialSource = locateDownloader(url);
                if (i == 0) {
                    materialSource.login();
                }
                materialSource.download(url);
                materialSource.downloadResources(url);
            } catch (Exception e) {
                System.out.println("--->" + url);
                Thread.sleep(3000L);
                if (e instanceof FileAlreadyExistsException) {
                    continue;
                }
                e.printStackTrace();
            }
            i++;
        }

        crawlService.tearDown();
    }

    private MaterialSource locateDownloader(String url) {
        return materialSourceFactory.getInstance(url);
    }

}
