package net.cocotea.cyreneai.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ToolSpecification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private Map<String, Object> parameters;

    public static ToolSpecification of(String name, String description, Map<String, Object> parameters) {
        return new ToolSpecification(name, description, parameters);
    }
}
