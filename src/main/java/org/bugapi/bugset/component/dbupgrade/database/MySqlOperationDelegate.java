package org.bugapi.bugset.component.dbupgrade.database;

import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;

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
   */
  @Override
  public void initDatabaseVersionTable() {
    //创建表
    DataBaseUtil.update(dataSource, "CREATE TABLE DATABASE_VERSION ("
        + "id bigint(10) unsigned auto_increment primary key comment 'id',"
        + "business varchar(100) not null comment '业务名称',"
        + "description varchar(1000) comment '业务描述',"
        + "environment varchar(10) comment '环境 dev（开发）test（测试）pro（生产）',"
        + "ddl_version varchar(100) comment '开发环境ddl版本号',"
        + "dml_version varchar(100) comment '开发环境dml版本号',"
        + "ddl_upgrade_date timestamp default CURRENT_TIMESTAMP not null comment '最近的ddl升级时间',"
        + "dml_upgrade_date timestamp default CURRENT_TIMESTAMP not null comment '最近的dml升级时间') "
        + "comment '数据库升级版本表' charset=utf8");
  }
}
