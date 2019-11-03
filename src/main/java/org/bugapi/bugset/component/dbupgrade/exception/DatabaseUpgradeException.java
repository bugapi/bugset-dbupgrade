package org.bugapi.bugset.component.dbupgrade.exception;

import org.bugapi.bugset.base.exception.BugSetException;

/**
 * 数据库升级受检查异常
 *
 * @author gust
 * @since 0.0.1
 */
public class DatabaseUpgradeException extends BugSetException {

  public DatabaseUpgradeException(String message, Throwable cause) {
    super(message, cause);
  }
}
