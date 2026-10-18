package com.homektv.repo;

import com.homektv.domain.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link Setting} 实体对应的数据访问仓库接口，操作 setting 配置表。
 *
 * Data access repository for the {@link Setting} entity, operating on the setting configuration table.
 */
public interface SettingRepository extends JpaRepository<Setting, String> {
}
