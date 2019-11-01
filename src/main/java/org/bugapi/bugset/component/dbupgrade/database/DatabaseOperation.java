package org.bugapi.bugset.component.dbupgrade.database;

/**
 * 数据库操作接口
 *
 * @author gust
 * @since 0.0.1
 */
public interface DatabaseOperation {

  /**
   * 创建版本表
   */
  void initDatabaseVersionTable();
}
