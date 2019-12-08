package org.bugapi.bugset.component.dbupgrade.core.database;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseVersion;

/**
 * 数据库操作接口
 *
 * @author gust
 * @since 0.0.1
 */
public interface DatabaseOperation {

  /**
   * 创建版本表
   * @throws SQLException SQL执行异常
   */
  void initDatabaseVersionTable() throws SQLException;

  /**
   * 返回数据库中已经存在的所有升级配置
   * @return 数据库升级配置
   * @throws SQLException SQL执行异常
   */
  List<DatabaseVersion> listDatabaseVersions() throws SQLException;

  /**
   * 初始化配置
   * @param initConfigs 数据库配置
   * @throws SQLException SQL执行异常
   */
  void initDatabaseVersionConfigs(List<DatabaseVersion> initConfigs) throws SQLException;

  /**
   * 根据业务类型更新版本号
   * @param business 业务
   * @param languageType 语言类型（ddl或dml）
   * @param version 版本号
   * @throws SQLException SQL执行异常
   */
  void updateVersionByBusiness(String business, String languageType, int version)
      throws SQLException;

  /**
   * 获取数据库升级锁
   * @return 数据库升级锁
   * @throws SQLException SQL执行异常
   */
  int getDatabaseUpgradeLock() throws SQLException;

  /**
   * 更新数据库升级表
   * @param dataSource 数据源
   * @param updateSql 更新语句
   * @param version 版本号
   * @param business 业务
   * @throws SQLException SQL执行异常
   */
  default void updateDataVersionTable(DataSource dataSource, String updateSql, int version,
      String business)
      throws SQLException {
    DataBaseUtil
        .update(dataSource, updateSql, new java.sql.Date(System.currentTimeMillis()), version, business);
  }

  /**
   * 从数据库获取数据库升级的版本信息
   * @param dataSource 数据库的数据源
   * @param sql 查询语句
   * @return VersionInfo 版本信息
   * @throws SQLException SQL执行异常
   */
  default List<DatabaseVersion> selectDatabaseVersions(
      DataSource dataSource, String sql) throws SQLException {
    return DataBaseUtil.select(dataSource, sql, new BeanListHandler<>(DatabaseVersion.class));
  }

  /**
   * 从数据库获取数据库升级锁
   * @param dataSource 数据库的数据源
   * @param sql 查询语句
   * @return 返回数据库锁
   * @throws SQLException SQL执行异常
   */
  default int selectDatabaseUpgradeLock(
      DataSource dataSource, String sql) throws SQLException {
    return DataBaseUtil.selectNumber(dataSource, sql);
  }
}
