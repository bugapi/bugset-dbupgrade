package org.bugapi.bugset.component.dbupgrade.parser;

import java.util.List;
import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfig;

/**
 * 升级配置解析器
 *
 * @author gust
 * @since 0.0.1
 */
public interface UpgradeConfigParser {

  /**
   * 解析升级配置文件
   * @return 升级配置集合
   */
  List<UpgradeConfig> parseUpgradeConfigs();

}