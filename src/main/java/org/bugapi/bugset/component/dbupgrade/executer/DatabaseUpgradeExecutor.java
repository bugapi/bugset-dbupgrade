package org.bugapi.bugset.component.dbupgrade.executer;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.component.dbupgrade.parse.BaseUpgradeConfigParse;
import org.bugapi.bugset.component.dbupgrade.upgrade.DefaultUpgrade;

/**
 * 项目升级执行器
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DatabaseUpgradeExecutor {

	private BaseUpgradeConfigParse baseUpgradeConfigParse;
	private DataSource dataSource;

	public DatabaseUpgradeExecutor(BaseUpgradeConfigParse baseUpgradeConfigParse, DataSource dataSource) {
		this.baseUpgradeConfigParse = baseUpgradeConfigParse;
		this.dataSource = dataSource;
	}

	/**
	 * 数据脚本升级的入口方法
	 * @Title: execute
	 * @param environmentType 环境类型{@link EnvironmentEnum}
	 * @param isAutoUpgrade 是否自动升级
	 */
	public void execute(EnvironmentEnum environmentType, Boolean isAutoUpgrade) {
		try {
			if(!isAutoUpgrade){
				log.error("没有启动自动化升级");
				return;
			}
			log.error("开始升级数据库...");
			DefaultUpgrade defaultUpgrade = new DefaultUpgrade(baseUpgradeConfigParse);
			defaultUpgrade.upgrade(dataSource, environmentType);
			log.error("数据库升级完成...");
		} catch (Exception e) {
			log.error("数据库升级失败，失败原因：{}", e.getMessage());
		}
	}
}
