package com.homektv.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 键值配置，对应 settings 表（详设§10）。value 为 JSONB。
 *
 * Key-value configuration, corresponding to the settings table (detailed design §10). The value is JSONB.
 */
@Entity
@Table(name = "settings")
public class Setting {

    /** 配置键。 */
    // English: Configuration key.
    @Id
    @Column(name = "key")
    private String key;

    /** 配置值，JSONB 格式。 */
    // English: Configuration value, in JSONB format.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb", nullable = false)
    private String value;

    /** 无参构造方法。 */
    // English: No-arg constructor.
    public Setting() {}

    /** 带参构造方法。 */
    // English: Parameterized constructor.
    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // ---- getters / setters ----
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
