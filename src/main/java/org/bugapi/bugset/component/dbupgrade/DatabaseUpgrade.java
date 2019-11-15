package org.bugapi.bugset.component.dbupgrade;

import static org.bugapi.bugset.component.dbupgrade.constants.DatabaseUpgradeConstants.DEFAULT_SCRIPT_ROOT_DIRECTORY;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.string.StringUtil;
import org.bugapi.bugset.component.dbupgrade.constants.DatabaseUpgradeModeEnum;
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

	/**
	 * 升级模式
	 */
	private DatabaseUpgradeModeEnum upgradeMode;


	public DatabaseUpgrade(DataSource dataSource) {
		this(dataSource, DatabaseUpgradeModeEnum.SINGLE);
	}

	public DatabaseUpgrade(DataSource dataSource,
			DatabaseUpgradeModeEnum upgradeMode) {
		this.dataSource = dataSource;
		this.upgradeMode = upgradeMode;
	}

	public void setScriptDirectory(String scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}

	public void setParser(UpgradeConfigParser parser) {
		this.parser = parser;
	}

	public void setUpgradeMode(
			DatabaseUpgradeModeEnum upgradeMode) {
		this.upgradeMode = upgradeMode;
	}

	/**
	 * 数据脚本升级的入口方法
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	public void execute() throws DatabaseUpgradeException {
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
		DatabaseUpgradeExecutor executor = new DatabaseUpgradeExecutor(dataSource, parser,
				scriptDirectory, upgradeMode);
		executor.upgrade();
		log.info("数据库升级完成...");
	}
}
