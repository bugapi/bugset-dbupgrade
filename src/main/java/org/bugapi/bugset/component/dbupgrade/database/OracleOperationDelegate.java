package org.bugapi.bugset.component.dbupgrade.database;

import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;

/**
 * @Author gust
 * @Description
 * @Date 2019/11/1
 **/
@AllArgsConstructor
@Slf4j
public class OracleOperationDelegate implements DatabaseOperation {

  /**
   * 数据源
   */
  private DataSource dataSource;



  /**
   * 创建版本表
   */
  @Override
  public void initDatabaseVersionTable() {

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
}
