package org.bugapi.bugset.component.dbupgrade.spring.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.xml.JaxbXmlUtil;
import org.bugapi.bugset.component.dbupgrade.core.domain.UpgradeConfig;
import org.bugapi.bugset.component.dbupgrade.core.parser.UpgradeConfigParser;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * spring数据库升级配置解析器
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class SpringUpgradeConfigParser implements UpgradeConfigParser, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  /**
   * 解析升级配置文件
   *
   * @param scriptRootDirectory 脚本根目录
   * @return 升级配置集合
   */
  @Override
  public List<UpgradeConfig> parseUpgradeConfigs(String scriptRootDirectory) {
    try {
      Resource[] resources = applicationContext.getResources("classpath:*/*db-upgrade.xml");
      UpgradeConfig upgradeConfig;
      List<UpgradeConfig> upgradeConfigs = new ArrayList<>();
      for (Resource resource : resources) {
        if (resource.exists()) {
          upgradeConfig = JaxbXmlUtil
              .convertToJavaBean(Paths.get(resource.getURI()).toAbsolutePath().toString(), UpgradeConfig.class);
          upgradeConfigs.add(upgradeConfig);
        }
      }
      return upgradeConfigs;
    } catch (IOException e) {
      log.error("升级配置文件读取失败", e);
      return Collections.emptyList();
    }
  }

  /**
   * 获取升级脚本的路径
   * @param filePath 解析到的文件路径
   * @param filePrefix 解析到文件名前缀
   * @param languageType 语言类型
   * @param targetVersion 要执行的脚本的版本号
   * @return String 完整的脚本文件路径
   */
  @Override
  public Path getScriptFilePath(String filePath, String filePrefix, String languageType, int targetVersion) {
    // 替换路径分隔符为统一的"/", trim头尾空格, 如果路径不以"/"结尾, 则追加"/"
    filePath = filePath.trim().replace("/", File.separator);
    if (!filePath.endsWith(File.separator)) {
      filePath += File.separator;
    }
    // 拼接完整的路径
    Path scriptFilePath = Paths.get(filePath, filePrefix + "_" + languageType + "_" + targetVersion + ".sql");

    Resource resource = applicationContext.getResource("classpath:" + Paths
        .get(filePath, filePrefix + "_" + languageType + "_" + targetVersion + ".sql"));
    log.info("尝试从如下路径查找升级脚本:" + scriptFilePath.toString());
    try {
      return Paths.get(resource.getURI());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    applicationContext = context;
  }
}
