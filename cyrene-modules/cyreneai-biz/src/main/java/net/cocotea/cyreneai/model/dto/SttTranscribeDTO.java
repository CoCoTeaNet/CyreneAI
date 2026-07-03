package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class SttTranscribeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger modelId;
    private String audioUrl;
}
