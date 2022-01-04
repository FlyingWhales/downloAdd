package com.example.linkedinlearningdownloader.source;

import com.example.linkedinlearningdownloader.config.SeleniumConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class CrawlService {

    private SeleniumConfig config;

    @Value("${downloads.location}")
    private String downloadLocation;

    public void setup() {
        config = new SeleniumConfig();
    }

    public void tearDown() {
        this.config.getDriver().quit();
    }

    public void crawl(String url) {
        config.getDriver().get(url);
    }

    public List<WebElement> getElementsByTagName(String tagName) {
        List<WebElement> webElementList = this.config.getDriver().findElements(By.tagName(tagName));
        return webElementList;
    }

    public List<WebElement> getElementsByClassName(String className) {
        List<WebElement> webElementList = this.config.getDriver().findElements(By.className(className));
        return webElementList;
    }

    public List getLinkByPartialText(String partialText) {
        List<WebElement> webElementList = this.config.getDriver().findElements(By.partialLinkText(partialText));
        return webElementList;
    }

    public void getAndClickById(String fieldName) {
        this.config.getDriver().findElement(By.id(fieldName)).click();
    }

    public void getAndClickByClass(String className) {
        this.config.getDriver().findElement(By.className(className)).click();
    }

    public void getAndClickButtonByText(String text) {
        this.config.getDriver().findElement(By.xpath("//button[text()=\"" + text + "\"]")).click();
    }

    public void getAndClickByXpath(String xpath) {
        this.config.getDriver().findElement(By.xpath(xpath)).click();
    }

    public String getUrlByClass(String className) {
        return this.config.getDriver().findElement(By.className(className)).getAttribute("href");
    }

    public void fillField(String fieldName, String value) {
        this.config.getDriver().findElement(By.id(fieldName)).sendKeys(value);

    }

    public String getSource() {
        return this.config.getDriver().getPageSource();
    }

    public void downloadContent(String fileLocation, String url) throws IOException {

        final String pathToFile = downloadLocation + fileLocation;
        log.info(">>>>>>" + pathToFile);

        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, Paths.get(pathToFile));
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }

    }

    @SneakyThrows
    public void downloadWithoutFileName(String urlString, String location) {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();

        String fieldValue = con.getHeaderField("Content-Disposition");
        if (fieldValue == null || !fieldValue.contains("filename=\"")) {
            log.error("No filename is available!");
        }
        String filename = fieldValue.substring(fieldValue.indexOf("filename=") + 9, fieldValue.length());

        final String pathToFolder = downloadLocation + location;
        Files.createDirectories(Paths.get(pathToFolder));
        File download = new File(pathToFolder, filename);

        ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
        FileOutputStream fos = new FileOutputStream(download);
        try {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            fos.close();
        }
    }

    public void prepareDownloadLocation(String folderName) {

        final String pathToFolder = downloadLocation + folderName;

        try {
            Files.deleteIfExists(Paths.get(pathToFolder));
            Files.createDirectories(Paths.get(pathToFolder));
        } catch (IOException e) {
            //TODO: Add logging & Exception handling
            e.printStackTrace();
        }
    }


}
