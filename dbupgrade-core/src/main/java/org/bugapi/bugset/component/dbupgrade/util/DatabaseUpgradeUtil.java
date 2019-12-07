package org.bugapi.bugset.component.dbupgrade.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库升级工具类
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DatabaseUpgradeUtil {

  /**
   * 信息提示
   * @param business 模块名称
   * @param currentVersion 当前版本号
   * @param targetVersion 目标版本号
   * @param languageType 语言类型
   * @return 信息提示
   */
  public static String getSqlFileInfo(String business, int currentVersion, int targetVersion,
      String languageType) {
    return String.format("业务类型：%s 语言类型：%s 版本：%d -> %d ", business, languageType, currentVersion,
        targetVersion);
  }

  /**
   * 获取升级脚本的路径
   * @param filePath 解析到的文件路径
   * @param filePrefix 解析到文件名前缀
   * @param targetVersion 要执行的脚本的版本号
   * @return String 完整的脚本文件路径
   */
  public static Path getScriptFilePath(String filePath, String filePrefix, int targetVersion){
    // 替换路径分隔符为统一的"/", trim头尾空格, 如果路径不以"/"结尾, 则追加"/"
    filePath = filePath.trim().replace("/", File.separator);
    if (!filePath.endsWith(File.separator)) {
      filePath += File.separator;
    }
    // 拼接完整的路径
    Path scriptFilePath = Paths.get(filePath, filePrefix + "_" + targetVersion + ".sql");
    log.info("尝试从如下路径查找升级脚本:" + scriptFilePath.toString());
    return scriptFilePath;
  }
}
