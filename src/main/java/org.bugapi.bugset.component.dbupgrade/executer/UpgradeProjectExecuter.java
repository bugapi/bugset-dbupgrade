package org.bugapi.bugset.component.dbupgrade.executer;

import javax.sql.DataSource;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.component.dbupgrade.parse.BaseUpgradeConfigParse;
import org.bugapi.bugset.component.dbupgrade.upgrade.DefaultUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: UpgradeProjectExecuter
 * @Description: 项目升级执行器
 * @author: zhangxw
 * @date: 2019/5/4
 */
public class UpgradeProjectExecuter {
	private static Logger logger = LoggerFactory.getLogger(UpgradeProjectExecuter.class);

	private BaseUpgradeConfigParse baseUpgradeConfigParse;
	private DataSource dataSource;

	public UpgradeProjectExecuter(BaseUpgradeConfigParse baseUpgradeConfigParse, DataSource dataSource) {
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
				logger.error("没有启动自动化升级");
				return;
			}
			logger.error("开始升级数据库...");
			DefaultUpgrade defaultUpgrade = new DefaultUpgrade(baseUpgradeConfigParse);
			defaultUpgrade.upgrade(dataSource, environmentType);
			logger.error("数据库升级完成...");
		} catch (Exception e) {
			logger.error("数据库升级失败...");
		}
	}
}
