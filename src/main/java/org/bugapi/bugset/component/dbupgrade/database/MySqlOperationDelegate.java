package org.bugapi.bugset.component.dbupgrade.database;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseVersion;

/**
 * MySql数据库操作接口
 *
 * @author gust
 * @since 0.0.1
 */
@AllArgsConstructor
@Slf4j
public class MySqlOperationDelegate implements DatabaseOperation {

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
    //创建表
    DataBaseUtil.update(dataSource, "CREATE TABLE DATABASE_VERSION ("
        + "id bigint(10) unsigned auto_increment primary key comment 'id',"
        + "business varchar(100) not null comment '业务名称',"
        + "description varchar(1000) comment '业务描述',"
        + "ddl_version varchar(100) comment 'ddl版本号',"
        + "dml_version varchar(100) comment 'dml版本号',"
        + "ddl_upgrade_date timestamp default CURRENT_TIMESTAMP not null comment '最近的ddl升级时间',"
        + "dml_upgrade_date timestamp default CURRENT_TIMESTAMP not null comment '最近的dml升级时间') "
        + "comment '数据库升级版本表' charset=utf8");
  }

  /**
   * 初始化配置
   * @param initConfigs 数据库配置
   * @throws SQLException SQL执行异常
   */
  @Override
  public void initDatabaseVersionConfigs(List<DatabaseVersion> initConfigs) throws SQLException {
    List<String> initSQLs = initConfigs.stream().map(initConfig -> String.format(
        "insert into DATABASE_VERSION(business, description, ddl_version, dml_version) values (%s, %s, %s, %d, %d)",
        initConfig.getBusiness(), initConfig.getDescription(), 0, 0))
        .collect(Collectors.toList());
    DataBaseUtil.batchExecuteSqlWithTransaction(initSQLs, dataSource);
  }

  /**
   * 根据业务类型更新版本号
   * @param business 业务
   * @param languageType 语言类型（ddl或dml）
   * @param version 版本号
   * @throws SQLException SQL执行异常
   */
  @Override
  public void updateVersionByBusiness(String business, String languageType, int version)
      throws SQLException {
    String updateSql = "update DATABASE_VERSION set " + languageType + "_upgrade_date = ?, " + languageType + "_version = ? where business = ?";
    updateDataVersionTable(dataSource, updateSql, version, business);
  }

  /**
   * 返回数据库中已经存在的所有升级配置
   *
   * @return 数据库升级配置
   * @throws SQLException SQL执行异常
   */
  @Override
  public List<DatabaseVersion> listDatabaseVersions() throws SQLException {
    return selectDatabaseVersions(dataSource, "select * from DATABASE_VERSION");
  }

  /**
   * 获取数据库升级锁
   * @return 数据库升级锁
   * @throws SQLException SQL执行异常
   */
  @Override
  public int getDatabaseUpgradeLock() throws SQLException {
    return selectDatabaseUpgradeLock(dataSource, "select 1 from DATABASE_LOCK");
  }
}
