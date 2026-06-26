package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiKnowledgeBasePageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Query knowledgeBase;

    @Data
    public static class Query {
        private String name;
        private Integer enableStatus;
    }
}
