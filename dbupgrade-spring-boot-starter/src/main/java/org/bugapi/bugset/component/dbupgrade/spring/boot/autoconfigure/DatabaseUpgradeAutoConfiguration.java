package org.bugapi.bugset.component.dbupgrade.spring.boot.autoconfigure;

import javax.annotation.Resource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.component.dbupgrade.core.DatabaseUpgrade;
import org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeModeEnum;
import org.bugapi.bugset.component.dbupgrade.core.parser.UpgradeConfigParser;
import org.bugapi.bugset.component.dbupgrade.spring.parser.SpringUpgradeConfigParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springboot 自动装配
 *
 * @author gust
 * @since 0.0.1
 */
@Configuration
@EnableConfigurationProperties(DatabaseUpgradeProperties.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class })
@Slf4j
public class DatabaseUpgradeAutoConfiguration implements InitializingBean {

  @Resource
  private DatabaseUpgradeProperties properties;

  private DatabaseUpgrade databaseUpgrade;

  @Bean
  public UpgradeConfigParser getSpringUpgradeConfigParser() {
    return new SpringUpgradeConfigParser();
  }

  @Bean
  public DatabaseUpgrade getDatabaseUpgrade(DataSource dataSource, UpgradeConfigParser upgradeConfigParser) {
    databaseUpgrade = new DatabaseUpgrade(dataSource,
        DatabaseUpgradeModeEnum.getUpgradeMode(properties.getMode()));
    databaseUpgrade.setParser(upgradeConfigParser);
    databaseUpgrade.setGlobalSchema(properties.getSchema());
    databaseUpgrade.setScriptDirectory(properties.getDirectory());
    return databaseUpgrade;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (properties.isEnable()) {
      databaseUpgrade.execute();
    } else {
      log.info("数据库升级未开启，升级取消");
    }
  }
}
