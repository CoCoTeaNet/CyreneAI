package net.cocotea.cyreneai.agent;

import java.util.Map;

public interface ToolExecutor {

    ToolSpecification getSpecification();

    String execute(Map<String, Object> args);

    String getName();
}
