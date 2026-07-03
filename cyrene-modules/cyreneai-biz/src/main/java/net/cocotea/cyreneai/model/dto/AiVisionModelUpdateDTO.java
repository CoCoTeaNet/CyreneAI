package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiVisionModelUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String providerType;
    private String modelName;
    private String apiKey;
    private String apiBaseUrl;
    private Integer isDefault;
    private Integer enableStatus;
    private Integer sort;
    private String remark;
}
