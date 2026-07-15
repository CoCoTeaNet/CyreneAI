package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiSensitiveWordUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID 不能为空")
    private BigInteger id;

    private String word;

    private String category;

    private String strategy;

    private String replacement;

    private String target;

    private Integer enableStatus;

    private Integer sort;
}
