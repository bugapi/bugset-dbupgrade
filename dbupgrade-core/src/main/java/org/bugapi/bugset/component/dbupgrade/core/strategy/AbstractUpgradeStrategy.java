package org.bugapi.bugset.component.dbupgrade.core.strategy;

import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DDL;
import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DML;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.component.dbupgrade.core.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;
import org.bugapi.bugset.component.dbupgrade.core.parser.UpgradeConfigParser;
import org.bugapi.bugset.component.dbupgrade.core.util.DatabaseUpgradeUtil;

/**
 * 抽象数据库升级策略
 *
 * @author gust
 * @since 0.0.1
 */
@AllArgsConstructor
@Slf4j
public abstract class AbstractUpgradeStrategy implements UpgradeStrategy {

  /**
   * 数据源
   */
  protected DataSource dataSource;

  /**
   * 数据库操作
   */
  protected DatabaseOperation databaseOperation;

  protected UpgradeConfigParser parser;

  /**
   * 执行数据库定义语言的升级脚本
   * @param upgradeVersions 待升级的版本
   * @throws DatabaseUpgradeException 数据库升级异常
   */
  protected void ddlScriptUpgrade(List<DatabaseUpgradeVersion> upgradeVersions)
      throws DatabaseUpgradeException {
    for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
      // 批量执行升级的sql语句【无全局事务控制】
      batchExecuteUpgradeSql(upgradeVersion.getBusiness(), upgradeVersion.getDdlCurrentVersion(),
          upgradeVersion.getDdlTargetVersion(), upgradeVersion.getDdlFileDirectory(),
          upgradeVersion.getDdlFilePrefix(), DDL);
    }
  }

  /**
   * 执行数据库操作语言的升级脚本
   * @param upgradeVersions 待升级的版本
   * @throws DatabaseUpgradeException 数据库升级异常
   */
  protected void dmlScriptUpgrade(List<DatabaseUpgradeVersion> upgradeVersions)
      throws DatabaseUpgradeException {
    for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
      // 批量执行升级的sql语句【有全局事务控制】
      batchExecuteUpgradeSql(upgradeVersion.getBusiness(), upgradeVersion.getDmlCurrentVersion(),
          upgradeVersion.getDmlTargetVersion(),
          upgradeVersion.getDmlFileDirectory(), upgradeVersion.getDmlFilePrefix(), DML);
    }
  }

  /**
   * 批量执行数据库升级语句
   * @param business 模块名称
   * @param currentVersion 升级前的当前版本号
   * @param targetVersion 要升级到的目标版本号
   * @param filePath 脚本所在文件路径
   * @param filePrefix 脚本文件的前缀
   * @param languageType 语言类型
   * @throws DatabaseUpgradeException 数据库升级异常
   */
  protected void batchExecuteUpgradeSql(String business, int currentVersion, int targetVersion,
      String filePath, String filePrefix, String languageType) throws DatabaseUpgradeException {
    // 读取脚本文件获取到的字符缓冲输入流
    List<String> sqlList;
    while (targetVersion > currentVersion) {
      try {
        sqlList = DataBaseUtil.readSql(parser.getScriptFilePath(filePath, filePrefix, languageType, ++currentVersion));
        if (DDL.equals(languageType)) {
          DataBaseUtil.batchExecuteSqlWithTransaction(sqlList, dataSource);
        } else {
          DataBaseUtil.batchExecuteSql(sqlList, dataSource);
        }
      } catch (IOException | SQLException e) {
        throw new DatabaseUpgradeException(
            DatabaseUpgradeUtil.getSqlFileInfo(business, currentVersion, targetVersion, languageType) + "升级失败", e);
      }
    }
    updateVersionInfo(business, currentVersion, languageType);
  }

  /**
   * 升级完成后修改数据库中版本升级版本表中的版本信息
   * @param business 模块名称
   * @param currentVersion 升级后的版本号
   * @param languageType 语言类型
   */
  private void updateVersionInfo(String business, int currentVersion, String languageType)
      throws DatabaseUpgradeException {
    try {
      this.databaseOperation.updateVersionByBusiness(business, languageType, currentVersion);
    } catch (SQLException e) {
      throw new DatabaseUpgradeException("更新版本号失败", e);
    }
    log.info(DatabaseUpgradeUtil
        .getSqlFileInfo(business, currentVersion - 1, currentVersion, languageType) + "成功");
  }

}
