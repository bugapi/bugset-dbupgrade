package org.bugapi.bugset.component.dbupgrade.core.database;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseVersion;

/**
 * Oracle数据库操作接口
 *
 * @author gust
 * @since 0.0.1
 */
@AllArgsConstructor
@Slf4j
public class OracleOperationDelegate implements DatabaseOperation {

  /**
   * 数据源
   */
  private DataSource dataSource;



  /**
   * 创建版本表
   * @throws SQLException SQL执行异常
   */
  @Override
  public void initDatabaseVersionTable() throws SQLException {

    // 如果升级表不存在就创建表
    DataBaseUtil.update(dataSource, "CREATE TABLE VERSIONINFO (" +
        "domain VARCHAR(100) NOT NULL," +
        "comments VARCHAR(1000)," +
        "DDLVersion VARCHAR(100) ," +
        "DMLVersion VARCHAR(100) ," +
        "DDLUpgradedate DATE NOT NULL," +
        "DMLUpgradedate DATE NOT NULL," +
        "DDLPUBLISHVERSION VARCHAR(100) ," +
        "DMLPUBLISHVERSION VARCHAR(100) ," +
        "PRIMARY KEY (DOMAIN))");
  }

  /**
   * 初始化配置
   * @param initConfigs 数据库配置
   * @throws SQLException SQL执行异常
   */
  @Override
  public void initDatabaseVersionConfigs(List<DatabaseVersion> initConfigs) throws SQLException {

  }

  /**
   * 根据业务类型更新版本号
   * @param business 业务
   * @param languageType 语言类型（ddl或dml）
   * @param version 版本号
   * @throws SQLException SQL执行异常
   */
  @Override
  public void updateVersionByBusiness(String business, String languageType, int version) throws SQLException {

  }

  /**
   * 获取数据库升级锁
   *
   * @return 数据库升级锁
   * @throws SQLException SQL执行异常
   */
  @Override
  public int getDatabaseUpgradeLock() throws SQLException {
    return 0;
  }

  /**
   * 返回数据库中已经存在的所有升级配置
   *
   * @return 数据库升级配置
   */
  @Override
  public List<DatabaseVersion> listDatabaseVersions() {
    return null;
  }
}
