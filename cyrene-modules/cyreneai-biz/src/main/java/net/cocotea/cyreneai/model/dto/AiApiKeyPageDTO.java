package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AiApiKeyPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "查询参数为空")
    private Query aiApiKey;

    @Data
    public static class Query {
        private String name;
        private BigInteger userId;
        private Integer enableStatus;
    }
}
