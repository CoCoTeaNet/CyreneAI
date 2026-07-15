package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AiAuditLogPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "查询参数为空")
    private Query aiAuditLog;

    @Data
    public static class Query {
        private BigInteger userId;
        private BigInteger apiKeyId;
        private BigInteger modelId;
        private String endpoint;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
