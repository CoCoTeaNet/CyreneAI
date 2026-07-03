package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class TtsSynthesizeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger modelId;
    private String text;
    private String voice = "alloy";
    private Double speed = 1.0;
}
