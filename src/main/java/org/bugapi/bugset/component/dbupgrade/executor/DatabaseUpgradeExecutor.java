package org.bugapi.bugset.component.dbupgrade.executor;

import static org.bugapi.bugset.component.dbupgrade.constants.DatabaseUpgradeConfigConstants.UPGRADE_TABLE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.base.util.collection.CollectionUtil;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.base.util.sql.MetaDataUtil;
import org.bugapi.bugset.component.dbupgrade.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.database.MySqlOperationDelegate;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseVersion;
import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfig;
import org.bugapi.bugset.component.dbupgrade.exception.DatabaseUpgradeException;
import org.bugapi.bugset.component.dbupgrade.parser.UpgradeConfigParser;

/**
 * 数据库升级执行器
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DatabaseUpgradeExecutor {

	/**
	 * 环境
	 */
	private EnvironmentEnum environment;

	/**
	 * 数据源
	 */
	private DataSource dataSource;

	/**
	 * 升级配置解析器
	 */
	private UpgradeConfigParser parser;

	/**
	 * 数据库操作
	 */
	private DatabaseOperation databaseOperation;

	public DatabaseUpgradeExecutor(EnvironmentEnum environment, DataSource dataSource, UpgradeConfigParser upgradeConfigParser) {
		this.environment = environment;
		this.dataSource = dataSource;
		this.parser = upgradeConfigParser;
	}

	/**
	 * 数据库升级的入口
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	public void upgrade() throws DatabaseUpgradeException {
		// 获取升级配置信息
		List<UpgradeConfig> upgradeConfigs = parser.parseUpgradeConfigs();
		if (CollectionUtil.isEmpty(upgradeConfigs)) {
			log.error("没有获取到任何需要升级的配置信息");
			return;
		}
		upgradeConfigs.sort(Comparator.comparingInt(UpgradeConfig::getSeq));

		//TODO 根据数据库类型使用不同的数据库操作代理
		databaseOperation = new MySqlOperationDelegate(dataSource);
		if (MetaDataUtil.existTable(dataSource, UPGRADE_TABLE_NAME)) {
			compareAndInitVersionTable(upgradeConfigs);
		} else {
			log.info("数据库升级表不存在，初始化中...");
			initDatabaseVersionTable(upgradeConfigs);
		}

		List<DatabaseUpgradeVersion> upgradeVersions = createUpgradeConfigs(upgradeConfigs);
		//TODO 版本和文件的校验

		// 进行ddl数据升级
		ddlScriptUpgrade(upgradeVersions);

		// 进行dml数据升级
		dmlScriptUpgrade(upgradeVersions);
	}

	/**
	 * 数据库中不存在升级配置表，进行初始化
	 * @param upgradeConfigs 升级配置
	 */
	private void initDatabaseVersionTable(List<UpgradeConfig> upgradeConfigs)
			throws DatabaseUpgradeException {
		try {
			this.databaseOperation.initDatabaseVersionTable();
		} catch (SQLException e) {
			throw new DatabaseUpgradeException("数据库升级表初始化失败", e);
		}
		initDatabaseVersionConfigs(upgradeConfigs);
	}

	/**
	 * 初始化数据库配置
	 * @param upgradeConfigs 数据库配置
	 */
	private void initDatabaseVersionConfigs(List<UpgradeConfig> upgradeConfigs)
			throws DatabaseUpgradeException {
		List<DatabaseVersion> initConfigs = upgradeConfigs.stream().map(config -> {
			DatabaseVersion databaseVersion = new DatabaseVersion();
			databaseVersion.setBusiness(config.getBusiness());
			databaseVersion.setDescription(config.getDescription());
			databaseVersion.setEnvironment(this.environment.getType());
			databaseVersion.setDdlVersion(0);
			databaseVersion.setDmlVersion(0);
			Date date = new Date();
			databaseVersion.setDdlUpgradeDate(date);
			databaseVersion.setDmlUpgradeDate(date);
			return databaseVersion;
		}).collect(Collectors.toList());
		try {
			this.databaseOperation.initDatabaseVersionConfigs(initConfigs);
		} catch (SQLException e) {
			throw new DatabaseUpgradeException("数据库升级表配置初始化失败", e);
		}
	}

	/**
	 * 数据库中已存在升级配置表，进行比较并初始化新增的业务
	 * @param upgradeConfigs 升级配置
	 */
	private void compareAndInitVersionTable(List<UpgradeConfig> upgradeConfigs)
			throws DatabaseUpgradeException {
		List<DatabaseVersion> databaseVersions = this.databaseOperation.listDatabaseVersions();
		Set<String> existBusinesses = databaseVersions.stream().map(DatabaseVersion::getBusiness)
				.collect(Collectors.toSet());
		List<UpgradeConfig> initConfigs = upgradeConfigs.stream().filter(upgradeConfig -> !existBusinesses
				.contains(upgradeConfig.getBusiness())).collect(Collectors.toList());
		try {
			initDatabaseVersionConfigs(initConfigs);
		} catch (DatabaseUpgradeException e) {
			throw new DatabaseUpgradeException("数据库升级表配置初始化失败", e);
		}
	}

	/**
	 * 填充升级版本管理的字段信息，并返回一个版本管理的集合
	 * @param newVersions 升级配置集合
	 * @return List<VersionInfo> 版本管理的集合
	 */
	private List<DatabaseUpgradeVersion> createUpgradeConfigs(List<UpgradeConfig> newVersions){
		List<DatabaseVersion> oldVersions = this.databaseOperation.listDatabaseVersions();
		Map<String, List<DatabaseVersion>> oldVersionsMap = oldVersions.stream()
				.collect(Collectors.groupingBy(DatabaseVersion::getBusiness));
		return newVersions.stream().map(newVersion -> {
			DatabaseUpgradeVersion upgradeVersion = new DatabaseUpgradeVersion();
			DatabaseVersion oldVersion = oldVersionsMap.get(newVersion.getBusiness()).get(0);
			upgradeVersion.setBusiness(newVersion.getBusiness());
			upgradeVersion.setDdlCurrentVersion(oldVersion.getDdlVersion());
			upgradeVersion.setDmlCurrentVersion(oldVersion.getDdlVersion());
			upgradeVersion.setDdlTargetVersion(newVersion.getDdlVersion());
			upgradeVersion.setDmlTargetVersion(newVersion.getDmlVersion());
			upgradeVersion.setDdlFilePath(newVersion.getDdlFilePath());
			upgradeVersion.setDmlFilePath(newVersion.getDmlFilePath());
			upgradeVersion.setDdlFilePrefix(newVersion.getDdlFilePrefix());
			upgradeVersion.setDmlFilePrefix(newVersion.getDmlFilePrefix());
			return upgradeVersion;
		}).collect(Collectors.toList());
	}

	/**
	 * 执行数据库定义语言的升级脚本
	 * @param upgradeVersions 待升级的版本
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void ddlScriptUpgrade(List<DatabaseUpgradeVersion> upgradeVersions)
			throws DatabaseUpgradeException {
		for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
			// 批量执行升级的sql语句【无全局事务控制】
			batchExecuteUpgradeSql(upgradeVersion.getBusiness(), upgradeVersion.getDdlCurrentVersion(),
					upgradeVersion.getDdlTargetVersion(), upgradeVersion.getDdlFilePath(),
					upgradeVersion.getDdlFilePrefix(), "ddl");
		}
	}

	/**
	 * 执行数据库操作语言的升级脚本
	 * @param upgradeVersions 待升级的版本
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void dmlScriptUpgrade(List<DatabaseUpgradeVersion> upgradeVersions)
			throws DatabaseUpgradeException {
		for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
			// 批量执行升级的sql语句【有全局事务控制】
			batchExecuteUpgradeSql(upgradeVersion.getBusiness(), upgradeVersion.getDmlCurrentVersion(),
					upgradeVersion.getDmlTargetVersion(),
					upgradeVersion.getDmlFilePath(), upgradeVersion.getDmlFilePrefix(), "dml");
		}
	}

	/**
	 * 批量
	 * @param business 模块名称
	 * @param currentVersion 升级前的当前版本号
	 * @param targetVersion 要升级到的目标版本号
	 * @param filePath 脚本所在文件路径
	 * @param filePrefix 脚本文件的前缀
	 * @param languageType 语言类型
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void batchExecuteUpgradeSql(String business, int currentVersion, int targetVersion,
			String filePath, String filePrefix, String languageType) throws DatabaseUpgradeException {
		// 读取脚本文件获取到的字符缓冲输入流
		List<String> sqlList;
		Path path;
		while (targetVersion > currentVersion) {
			// 获取文件路径
			path = getScriptFilePath(filePath, filePrefix, ++currentVersion);
			// 获取sql文件的输入流
			try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
				sqlList = DataBaseUtil.readSql(inputStream);
				if ("ddl".equals(languageType)) {
					DataBaseUtil.batchExecuteSqlWithTransaction(sqlList, dataSource);
				} else {
					DataBaseUtil.batchExecuteSql(sqlList, dataSource);
				}
			} catch (IOException | SQLException e) {
				throw new DatabaseUpgradeException(
						business + languageType + (currentVersion - 1) + "-" + currentVersion + "失败", e);
			}
		}
		updateVersionInfo(business, currentVersion, filePrefix, languageType);
	}

	/**
	 * 升级完成后修改数据库中版本升级版本表中的版本信息
	 * @param business 模块名称
	 * @param currentVersion 升级后的当前版本号
	 * @param filePrefix 文件前缀
	 * @param languageType 语言类型
	 */
	private void updateVersionInfo(String business, int currentVersion, String filePrefix, String languageType)
			throws DatabaseUpgradeException {
		try {
			this.databaseOperation.updateVersionByBusiness(business, languageType, currentVersion);
		} catch (SQLException e) {
			throw new DatabaseUpgradeException("更新版本号失败", e);
		}
		log.info("--->> " + business + "Upgrade database from [ "
				+ filePrefix + "_" + (currentVersion - 1) + " ] to [ " + filePrefix + "_" + currentVersion + "] success.");
	}

	/**
	 * 获取升级脚本的路径
	 * @param filePath 解析到的文件路径
	 * @param filePrefix 解析到文件名前缀
	 * @param targetVersion 要执行的脚本的版本号
	 * @return String 完整的脚本文件路径
	 */
	private Path getScriptFilePath(String filePath, String filePrefix, int targetVersion){
		// 替换路径分隔符为统一的"/", trim头尾空格, 如果路径不以"/"结尾, 则追加"/"
		filePath = filePath.trim().replace("/", File.separator);
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}
		// 拼接完整的路径
		Path scriptFilePath = Paths.get(filePath, filePrefix + "_" + targetVersion + ".sql");
		log.info("尝试从如下路径查找升级脚本:" + scriptFilePath.toString());
		return scriptFilePath;
	}
}
