package net.cocotea.cyreneai.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

@Configuration
public class PgVectorConfig {

    @Bean(name = "pgvector")
    public DataSource pgvector(@Inject("${myapp.db2}") HikariDataSource ds) {
        return ds;
    }
}
