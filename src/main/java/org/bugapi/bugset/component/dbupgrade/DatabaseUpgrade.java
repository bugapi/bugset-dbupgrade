package org.bugapi.bugset.component.dbupgrade;

import static org.bugapi.bugset.component.dbupgrade.constants.DatabaseUpgradeConfigConstants.DEFAULT_SCRIPT_ROOT_DIRECTORY;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.base.util.string.StringUtil;
import org.bugapi.bugset.component.dbupgrade.exception.DatabaseUpgradeException;
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

	/**
	 * 数据源
	 */
	private DataSource dataSource;

	/**
	 * 升级配置解析器
	 */
	private UpgradeConfigParser parser;

	/**
	 * 脚本文件目录
	 */
	private String scriptDirectory;


	public DatabaseUpgrade(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setScriptDirectory(String scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}

	public void setParser(UpgradeConfigParser parser) {
		this.parser = parser;
	}

	/**
	 * 数据脚本升级的入口方法
	 * @param environment 环境类型{@link EnvironmentEnum}
	 * @param enableAutoUpgrade 是否自动升级
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	public void execute(EnvironmentEnum environment, Boolean enableAutoUpgrade)
			throws DatabaseUpgradeException {
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
		if (StringUtil.isBlank(scriptDirectory)) {
			scriptDirectory = DEFAULT_SCRIPT_ROOT_DIRECTORY;
		}
		DatabaseUpgradeExecutor executor = new DatabaseUpgradeExecutor(environment, dataSource, parser, scriptDirectory);
		executor.upgrade();
		log.info("数据库升级完成...");
	}
}
