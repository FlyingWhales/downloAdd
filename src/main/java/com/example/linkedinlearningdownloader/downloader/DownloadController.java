package com.example.linkedinlearningdownloader.downloader;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/downloader")
@RequiredArgsConstructor
public class DownloadController {

    //TODO: Add global exception handler
    private final DownloadService downloadService;

    @GetMapping(value = "/download")
    public ResponseEntity<String> download(@RequestBody List<String> urlList) {

        downloadService.download(urlList);
        return ResponseEntity.ok().build();
    }

}
