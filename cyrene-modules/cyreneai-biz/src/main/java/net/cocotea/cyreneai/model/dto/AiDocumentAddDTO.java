package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiDocumentAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private Long size;
    private String filePath;
    private BigInteger kbId;
    private String chunkStrategy;
    private Integer chunkSize;
    private Integer chunkOverlap;
}
