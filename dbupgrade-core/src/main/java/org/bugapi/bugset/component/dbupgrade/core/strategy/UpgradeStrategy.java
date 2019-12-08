package org.bugapi.bugset.component.dbupgrade.core.strategy;

import java.util.List;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;

/**
 * 数据库升级策略
 *
 * @author gust
 * @since 0.0.1
 */
public interface UpgradeStrategy {

  void doUpgrade(List<DatabaseUpgradeVersion> upgradeVersions) throws DatabaseUpgradeException;
}
