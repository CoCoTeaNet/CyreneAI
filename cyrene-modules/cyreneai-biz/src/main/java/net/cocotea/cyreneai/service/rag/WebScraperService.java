package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.util.SafeHttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.noear.solon.annotation.Component;

import java.io.IOException;

@Component
public class WebScraperService {

    public ScrapedResult scrape(String url) throws IOException {
        // SSRF 防护：仅允许 http/https 且禁止内网/保留地址
        SafeHttpUtils.validateUrl(url);
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .followRedirects(false)
                .maxBodySize((int) SafeHttpUtils.DEFAULT_MAX_BYTES)
                .get();

        String title = doc.title();

        // Remove unwanted elements
        removeElements(doc, "script, style, nav, footer, header, aside, iframe, " +
                ".ad, .ads, .advertisement, .sidebar, .menu, .nav, .footer, .header, " +
                ".cookie, .popup, .modal, .social-share, .comments, .related-posts");

        Element main = doc.selectFirst("main, article, .content, .post, .article, #content, #main");
        String text;
        if (main != null) {
            text = main.text();
        } else {
            text = doc.body() != null ? doc.body().text() : doc.text();
        }

        // Clean up whitespace
        text = text.replaceAll("\\s+", " ").strip();
        if (text.length() > 10000) {
            text = text.substring(0, 10000);
        }

        return new ScrapedResult(title, text, url);
    }

    private void removeElements(Document doc, String cssQuery) {
        Elements elements = doc.select(cssQuery);
        for (Element el : elements) {
            el.remove();
        }
    }

    public record ScrapedResult(String title, String text, String sourceUrl) {}
}
