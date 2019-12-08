package org.bugapi.bugset.component.dbupgrade.core.parser;

import java.util.List;
import org.bugapi.bugset.component.dbupgrade.core.domain.UpgradeConfig;

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
   * @param scriptRootDirectory 脚本根目录
   */
  List<UpgradeConfig> parseUpgradeConfigs(String scriptRootDirectory);

}
