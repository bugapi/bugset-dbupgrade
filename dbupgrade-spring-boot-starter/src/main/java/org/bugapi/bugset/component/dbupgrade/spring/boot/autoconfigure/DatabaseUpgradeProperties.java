package org.bugapi.bugset.component.dbupgrade.spring.boot.autoconfigure;

import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DEFAULT_SCHEMA;
import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DEFAULT_SCRIPT_ROOT_DIRECTORY;

import lombok.Data;
import org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * springboot 参数配置
 *
 * @author gust
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = "bugapi.db.upgrade")
@Data
public class DatabaseUpgradeProperties {

  /**
   * 是否开启数据库升级
   */
  private boolean enable = true;

  /**
   * 脚本文件目录
   */
  private String directory = DEFAULT_SCRIPT_ROOT_DIRECTORY;

  /**
   * 升级模式
   */
  private String mode = DatabaseUpgradeModeEnum.SINGLE.name();

  /**
   * 数据库schema
   */
  private String schema = DEFAULT_SCHEMA;

}
