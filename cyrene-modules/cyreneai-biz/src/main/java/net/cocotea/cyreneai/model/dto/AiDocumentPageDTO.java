package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiDocumentPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Query document;

    @Data
    public static class Query {
        private String name;
        private String type;
        private Integer status;
        private BigInteger kbId;
    }
}
