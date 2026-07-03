package net.cocotea.cyreneai.model.po;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName = "ai_stt_record", comment = "语音转文字记录表", pk_constraint = "PRIMARY")
public class AiSttRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "audio_url", comment = "音频URL", length = 512L, type = java.sql.Types.VARCHAR, nullable = true)
    private String audioUrl;

    @Column(name = "model_name", comment = "模型名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = true)
    private String modelName;

    @Column(name = "transcript", comment = "转写文本", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String transcript;

    @Column(name = "file_size", comment = "文件大小(字节)", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private Long fileSize;

    @Column(name = "duration_seconds", comment = "音频时长(秒)", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer durationSeconds;

    @Column(name = "cost", comment = "消耗金额", length = 10L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal cost;

    @Column(name = "create_by", comment = "创建人", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger createBy;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_by", comment = "更新人", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger updateBy;

    @Column(name = "update_time", comment = "更新时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted", comment = "是否删除", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = false)
    private Integer isDeleted;

    @Column(name = "revision", comment = "乐观锁", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer revision;

    public AiSttRecord() {}

    public AiSttRecord(BigInteger id) {
        this.id = id;
    }
}
