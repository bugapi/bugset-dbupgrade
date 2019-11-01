package org.bugapi.bugset.component.dbupgrade;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.component.dbupgrade.executor.DatabaseUpgradeExecutor;
import org.bugapi.bugset.component.dbupgrade.parser.DefaultUpgradeConfigParser;
import org.bugapi.bugset.component.dbupgrade.parser.UpgradeConfigParser;

/**
 * 数据库升级
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DatabaseUpgrade {

	private DataSource dataSource;

	private UpgradeConfigParser parser;

	public DatabaseUpgrade(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DatabaseUpgrade(DataSource dataSource, UpgradeConfigParser parser) {
		this.dataSource = dataSource;
		this.parser = parser;
	}

	/**
	 * 数据脚本升级的入口方法
	 * @param environment 环境类型{@link EnvironmentEnum}
	 * @param enableAutoUpgrade 是否自动升级
	 */
	public void execute(EnvironmentEnum environment, Boolean enableAutoUpgrade) {
		try {
			if (!enableAutoUpgrade) {
				log.info("数据库自动化升级未开启，取消升级...");
				return;
			}
			if (dataSource == null) {
				log.info("无法获取数据源，取消升级...");
				return;
			}
			log.info("数据库升级开始...");
			if (parser == null) {
				parser = new DefaultUpgradeConfigParser();
			}
			DatabaseUpgradeExecutor executor = new DatabaseUpgradeExecutor(environment, dataSource, parser);
			executor.upgrade();
			log.info("数据库升级完成...");
		} catch (Exception e) {
			log.error("数据库升级失败，失败原因：{}", e.getMessage());
		}
	}
}
