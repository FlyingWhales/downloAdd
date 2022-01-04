package com.example.linkedinlearningdownloader.source;

import com.example.linkedinlearningdownloader.source.linkedin.LinkedinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaterialSourceFactory {

    private final LinkedinService linkedinService;

    public MaterialSource getInstance(String url) {

        //TODO: There should be a logic to locate related downloader service
        return linkedinService;
    }
}
