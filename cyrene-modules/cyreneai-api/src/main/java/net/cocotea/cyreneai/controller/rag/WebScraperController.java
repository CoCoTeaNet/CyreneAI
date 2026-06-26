package net.cocotea.cyreneai.controller.rag;

import net.cocotea.cyreneai.model.dto.WebScraperDTO;
import net.cocotea.cyreneai.service.rag.DocumentService;
import net.cocotea.cyreneai.service.rag.WebScraperService;
import net.cocotea.cyreneadmin.model.ApiResult;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Valid
@Controller
@Mapping("/ai/webScraper")
public class WebScraperController {

    @Inject
    private WebScraperService webScraperService;

    @Inject
    private DocumentService documentService;

    @Post
    @Mapping("/scrape")
    public ApiResult<?> scrape(@Validated @Body WebScraperDTO dto) {
        try {
            WebScraperService.ScrapedResult result = webScraperService.scrape(dto.getUrl());
            if (dto.getKbId() != null) {
                String fileName = result.title().replaceAll("[\\\\/:*?\"<>|]", "_") + ".md";
                byte[] content = (result.text()).getBytes(StandardCharsets.UTF_8);
                documentService.upload(fileName, content, dto.getKbId(),
                        dto.getChunkStrategy(), dto.getChunkSize(), dto.getChunkOverlap());
            }
            return ApiResult.ok(Map.of(
                    "title", result.title(),
                    "textLength", result.text().length(),
                    "sourceUrl", result.sourceUrl()
            ));
        } catch (IOException e) {
            return ApiResult.error("Failed to scrape URL: " + e.getMessage());
        }
    }
}
