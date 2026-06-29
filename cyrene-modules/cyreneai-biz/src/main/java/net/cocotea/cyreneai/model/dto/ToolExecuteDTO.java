package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ToolExecuteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String toolName;

    private Map<String, Object> arguments;
}
