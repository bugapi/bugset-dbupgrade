package org.bugapi.bugset.component.dbupgrade.spring.parser;

import java.io.IOException;
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

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    applicationContext = context;
  }
}
