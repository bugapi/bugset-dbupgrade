package org.bugapi.bugset.component.dbupgrade.core.parser;

import java.nio.file.Path;
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

  /**
   * 获取升级脚本的路径
   * @param filePath 解析到的文件路径
   * @param filePrefix 解析到文件名前缀
   * @param languageType 语言类型
   * @param targetVersion 要执行的脚本的版本号
   * @return String 完整的脚本文件路径
   */
  Path getScriptFilePath(String filePath, String filePrefix, String languageType, int targetVersion);
}
