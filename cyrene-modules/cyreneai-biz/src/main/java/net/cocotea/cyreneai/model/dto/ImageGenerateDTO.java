package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class ImageGenerateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger modelId;
    private String prompt;
    private String size = "1024x1024";
    private String style;
    private Integer n = 1;
}
