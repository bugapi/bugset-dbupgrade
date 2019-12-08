package org.bugapi.bugset.component.dbupgrade.core.constants;

/**
 * 数据库升级模式枚举
 *
 * @author gust
 * @since 0.0.1
 **/
public enum DatabaseUpgradeModeEnum {

  /**
   * 单节点
   */
  SINGLE,

  /**
   * 集群
   */
  CLUSTER;

  public static DatabaseUpgradeModeEnum getUpgradeMode(String mode) {
    for (DatabaseUpgradeModeEnum e : DatabaseUpgradeModeEnum.values()) {
      if (e.name().equals(mode)) {
        return e;
      }
    }
    return null;
  }

}
