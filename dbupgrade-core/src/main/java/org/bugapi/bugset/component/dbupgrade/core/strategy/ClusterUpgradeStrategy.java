package org.bugapi.bugset.component.dbupgrade.core.strategy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.component.dbupgrade.core.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;

/**
 * 多节点数据库升级策略
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class ClusterUpgradeStrategy extends AbstractUpgradeStrategy {


  public ClusterUpgradeStrategy(DataSource dataSource,
      DatabaseOperation databaseOperation) {
    super(dataSource, databaseOperation);
  }

  @Override
  public void doUpgrade(List<DatabaseUpgradeVersion> upgradeVersions)
      throws DatabaseUpgradeException {
    try {
      Connection connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      int databaseUpgradeLock = databaseOperation.getDatabaseUpgradeLock();
      if (databaseUpgradeLock != 1) {
        throw new DatabaseUpgradeException("数据库锁表数据异常");
      }

      // 进行ddl数据升级
      ddlScriptUpgrade(upgradeVersions);

      // 进行dml数据升级
      dmlScriptUpgrade(upgradeVersions);
      connection.commit();
    } catch (SQLException e) {
      throw new DatabaseUpgradeException("获取数据库锁失败", e);
    }
  }
}
