package org.bugapi.bugset.component.dbupgrade.spring.boot.autoconfigure.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.component.dbupgrade.core.DatabaseUpgrade;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;
import org.bugapi.bugset.component.dbupgrade.spring.boot.autoconfigure.DatabaseUpgradeProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 数据库升级监听
 *
 * @author gust
 * @since 0.0.1
 */
@AllArgsConstructor
@Slf4j
public class DatabaseUpgradeListener implements ApplicationListener<ContextRefreshedEvent> {

  private DatabaseUpgradeProperties properties;

  private DatabaseUpgrade databaseUpgrade;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (properties.isEnable()) {
      try {
        databaseUpgrade.execute();
      } catch (DatabaseUpgradeException e) {
        log.error(e.getMessage(), e);
      }
    } else {
      log.info("数据库升级未开启，升级取消");
    }
  }
}
