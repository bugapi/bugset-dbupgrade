package org.bugapi.bugset.component.dbupgrade.constants;

/**
 * 数据库升级常量类
 *
 * @author gust
 * @since 0.0.1
 */
public class DatabaseUpgradeConfigConstants {

  /**
   * 数据库升级记录表名
   */
  public static final String UPGRADE_TABLE_NAME = "DATABASEVERSION";

  /**
   * 语言类型-ddl
   */
  public static final String DML = "ddl";

  /**
   * 语言类型-dml
   */
  public static final String DDL = "dml";

  /**
   * 默认脚本根目录
   */
  public static final String DEFAULT_SCRIPT_ROOT_DIRECTORY = "upgrade";


  /** domain必须与数据库中UPGRADE_VERSION相对应 */
  public static final String DOMAIN = "domain";

  /** 升级描述信息 */
  public static final String COMMENTS = "comments";

  /**数据定义语言升级版本号*/
  public static final String  DDL_VERSION = "ddl-version";

  /**数据操作语言升级版本号*/
  public static final String DML_VERSION = "dml-version";

  /**发布升级版本号*/
  public static final String DDL_PUBLISH_VERSION = "ddl-publish-Version";

  /**发布升级版本号*/
  public static final String DML_PUBLISH_VERSION = "dml-publish-version";

  /** 升级脚本相对于根目录的ddl路径 */
  public static final String DDL_FILE_PATH = "ddl-file-path";

  /** 升级脚本相对于根目录的dml路径 */
  public static final String DML_FILE_PATH = "dml-file-path";

  /** 数据定义语言脚本文件名前缀 */
  public static final String DDL_FILE_PREFIX = "ddl-file-prefix";

  /** 数据定义语言脚本文件名前缀 */
  public static final String DML_FILE_PREFIX = "dml-file-prefix";

}
