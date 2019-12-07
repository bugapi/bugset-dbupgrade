package org.bugapi.bugset.component.dbupgrade.strategy;

import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.component.dbupgrade.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.exception.DatabaseUpgradeException;

/**
 * 单节点数据库升级策略
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class SingleUpgradeStrategy extends AbstractUpgradeStrategy {


  public SingleUpgradeStrategy(DataSource dataSource,
      DatabaseOperation databaseOperation) {
    super(dataSource, databaseOperation);
  }

  @Override
  public void doUpgrade(List<DatabaseUpgradeVersion> upgradeVersions) throws DatabaseUpgradeException {
    // 进行ddl数据升级
    ddlScriptUpgrade(upgradeVersions);

    // 进行dml数据升级
    dmlScriptUpgrade(upgradeVersions);
  }


}
