package org.bugapi.bugset.component.dbupgrade.core.strategy;

import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.component.dbupgrade.core.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;
import org.bugapi.bugset.component.dbupgrade.core.parser.UpgradeConfigParser;

/**
 * 单节点数据库升级策略
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class SingleUpgradeStrategy extends AbstractUpgradeStrategy {


  public SingleUpgradeStrategy(DataSource dataSource,
      DatabaseOperation databaseOperation, UpgradeConfigParser parser) {
    super(dataSource, databaseOperation, parser);
  }

  @Override
  public void doUpgrade(List<DatabaseUpgradeVersion> upgradeVersions) throws DatabaseUpgradeException {
    // 进行ddl数据升级
    ddlScriptUpgrade(upgradeVersions);

    // 进行dml数据升级
    dmlScriptUpgrade(upgradeVersions);
  }


}
