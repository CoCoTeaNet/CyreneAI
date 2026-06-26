package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class WebScraperDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String url;

    private BigInteger kbId;

    private String chunkStrategy;

    private Integer chunkSize;

    private Integer chunkOverlap;
}
