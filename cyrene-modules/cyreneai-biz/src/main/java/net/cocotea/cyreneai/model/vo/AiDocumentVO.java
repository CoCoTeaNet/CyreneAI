package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiDocumentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String name;
    private String type;
    private Long size;
    private String filePath;
    private Integer status;
    private Integer chunkCount;
    private String chunkStrategy;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String errorMsg;
    private BigInteger kbId;
    private String kbName;
    private BigInteger createBy;
    private LocalDateTime createTime;
    private BigInteger updateBy;
    private LocalDateTime updateTime;
}
