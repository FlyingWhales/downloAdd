package com.example.linkedinlearningdownloader.source;

import java.io.IOException;

public interface MaterialSource {

    default void login() {
        fillUserName();
        fillPassword();
        submitCredentials();
    }

    public void fillUserName();

    public void fillPassword();

    public void submitCredentials();

    public void download(String url) throws IOException;

    public void downloadResources(String url);

}

