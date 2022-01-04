package com.example.linkedinlearningdownloader.source.linkedin;

import com.example.linkedinlearningdownloader.source.CrawlService;
import com.example.linkedinlearningdownloader.source.MaterialSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkedinService implements MaterialSource {

    private final CrawlService crawlService;

    private final static String EMAIL_INPUT_FIELD = "auth-id-input";

    private final static String PASS_INPUT_FIELD = "password";

    private final static String BUTTON_CONTINUE = "auth-id-button";

    private final static String BUTTON_CONTINUE_TEXT = "Continue";

    private final static String EPISODE_TITLE = "classroom-toc-item__title";

    private final static String EPISODES = "classroom-toc-item__content";

    private final static String COLLAPSED_SECTION_HEADER_COLLAPSED = "classroom-toc-section--collapsed";

    private final static String CONTENT_POPUP_BUTTON = "//button[@aria-label=\"Show all exercise files\"]";

    private final static String CONTENT_POPUP_DOWNLOAD_BUTTON = "classroom-exercise-files-modal__exercise-file-download";

    private final static String EXERCISE_FILES = "ExerciseFiles/";

    private final static String DASH = "-";

    private static final String SLASH = "/";

    private final static String EXTENSION = ".mp4";

    @Value("${linkedin.site.url}")
    private String LOGIN_URL;

    @Value("${linkedin.credentials.user}")
    private String user;

    @Value("${linkedin.credentials.pass}")
    private String pass;

    @Override
    @SneakyThrows
    public void fillUserName() {
        crawlService.crawl(LOGIN_URL);

        Thread.sleep(3000L);
        String actualLoginUrl = ((WebElement) crawlService.getLinkByPartialText("Sign in").get(0)).getAttribute("href");

        crawlService.crawl(actualLoginUrl);
        crawlService.fillField(EMAIL_INPUT_FIELD, user);
        crawlService.getAndClickById(BUTTON_CONTINUE);
    }

    @Override
    public void fillPassword() {
        crawlService.fillField(PASS_INPUT_FIELD, pass);
    }

    @Override
    public void submitCredentials() {
        crawlService.getAndClickButtonByText(BUTTON_CONTINUE_TEXT);
    }

    @Override
    public void download(String url) throws IOException {

        crawlService.crawl(url);
        expandAllContent();
        List<WebElement> episodes = crawlService.getElementsByClassName(EPISODES);

        final String materialHeader = getMaterialHeader();
        crawlService.prepareDownloadLocation(materialHeader);
        log.info("materialHeader --->> " + materialHeader);

        for (int counter = 0;counter<episodes.size();counter++) {
            try {

                WebElement episode = episodes.get(counter);

                final String episodeName = episode.findElement(By.className(EPISODE_TITLE)).getText();
                final String fileName = generateFileName(materialHeader, counter + 1, episodeName);

                log.info("episodeName --->> " + episodeName);
                log.info("fileName --->> " + fileName);
                episode.click();
                Thread.sleep(2000L);
                List<WebElement> availableVideos = crawlService.getElementsByTagName("video");

                if(Objects.isNull(availableVideos) || availableVideos.size() == 0) {
                    crawlService.crawl(url);
                    expandAllContent();
                    episodes = crawlService.getElementsByClassName(EPISODES);
                    continue;
                }

                final String videoLink  = availableVideos.get(0).getAttribute("src");
                log.info("videoLink --->> " + videoLink);

                Thread.sleep(4000L);

                crawlService.downloadContent(fileName, videoLink);
            } catch (StaleElementReferenceException e) {
                crawlService.crawl(url);
                expandAllContent();
                episodes = crawlService.getElementsByClassName(EPISODES);
                continue;
            }

            catch (Exception e) {
                log.error(e.getMessage());
            }

        }
    }

    private void expandAllContent() {

        crawlService.getElementsByClassName(COLLAPSED_SECTION_HEADER_COLLAPSED).forEach(item -> {
            item.click();
        });

    }

    private String getMaterialHeader() {
        return crawlService.getElementsByTagName("h1").get(0).getText().trim();
    }

    @Override
    public void downloadResources(String url) {

        try {
            crawlService.crawl(url);
            crawlService.getAndClickByXpath(CONTENT_POPUP_BUTTON);
            String exerciseFilesUrl = crawlService.getUrlByClass(CONTENT_POPUP_DOWNLOAD_BUTTON);

            final String exerciseFile = generateExFileName(getMaterialHeader());
            crawlService.downloadWithoutFileName(exerciseFilesUrl, exerciseFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateFileName(String materialHeader, Integer counter, String episodeName) {

        final StringBuilder sb = new StringBuilder();

        final String fileName = episodeName
                .replace("/","-")
                .replace("(Viewed)","")
                .replace("(In Progress)",".")
                .replace("(In progress)",".")
                .replace("\n",".")
                .replace("  "," ");

        sb.append(materialHeader)
                .append(SLASH)
                .append(StringUtils.leftPad(counter.toString() + DASH, 3, "0"))
                .append(fileName)
                .append(EXTENSION);

        return sb.toString();
    }

    private String generateExFileName(String materialHeader) {

        final StringBuilder sb = new StringBuilder();

        final String fileName = materialHeader
                .replace("/","-")
                .replace("(Viewed)","")
                .replace("(In Progress)",".")
                .replace("\n",".")
                .replace("  "," ");

        sb.append(materialHeader)
                .append(SLASH)
                .append(EXERCISE_FILES);

        return sb.toString();
    }

}