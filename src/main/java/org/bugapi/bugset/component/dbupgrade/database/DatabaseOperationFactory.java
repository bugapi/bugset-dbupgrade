package org.bugapi.bugset.component.dbupgrade.database;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.bugapi.bugset.base.constant.DatabaseEnum;
import org.bugapi.bugset.base.util.sql.MetaDataUtil;
import org.bugapi.bugset.component.dbupgrade.exception.DatabaseUpgradeException;

/**
 * 数据操作对象工厂
 *
 * @author gust
 * @since 0.0.1
 **/
public final class DatabaseOperationFactory {

  /**
   * 根据数据源类型获取数据操作对象
   * @param dataSource 数据源
   * @return 数据操作对象
   * @throws DatabaseUpgradeException 数据库升级异常
   */
  public static DatabaseOperation getDatabaseOperation(DataSource dataSource)
      throws DatabaseUpgradeException {
    DatabaseEnum databaseType;
    try {
      databaseType = MetaDataUtil.getDatabaseType(dataSource);
    } catch (SQLException e) {
      throw new DatabaseUpgradeException("获取数据库类型失败", e);
    }
    switch (databaseType) {
      case MYSQL:
        return new MySqlOperationDelegate(dataSource);
      case ORACLE:
        return new OracleOperationDelegate(dataSource);
      case UNKNOWN:
        throw new DatabaseUpgradeException("无法获取数据库类型");
      default:
        throw new DatabaseUpgradeException("暂不支持该类型数据库升级");
    }
  }
}
