package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiEmbeddingModelPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Query embeddingModel;

    @Data
    public static class Query {
        private String providerType;
        private String modelName;
        private Integer enableStatus;
    }
}
