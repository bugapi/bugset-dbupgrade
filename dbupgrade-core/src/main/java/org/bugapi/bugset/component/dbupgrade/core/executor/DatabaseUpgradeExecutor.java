package org.bugapi.bugset.component.dbupgrade.core.executor;

import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DDL;
import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.DML;
import static org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeConstants.UPGRADE_TABLE_NAME;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.collection.CollectionUtil;
import org.bugapi.bugset.base.util.sql.MetaDataUtil;
import org.bugapi.bugset.base.util.string.StringUtil;
import org.bugapi.bugset.component.dbupgrade.core.constants.DatabaseUpgradeModeEnum;
import org.bugapi.bugset.component.dbupgrade.core.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.core.database.DatabaseOperationFactory;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.core.domain.DatabaseVersion;
import org.bugapi.bugset.component.dbupgrade.core.domain.UpgradeConfig;
import org.bugapi.bugset.component.dbupgrade.core.exception.DatabaseUpgradeException;
import org.bugapi.bugset.component.dbupgrade.core.parser.UpgradeConfigParser;
import org.bugapi.bugset.component.dbupgrade.core.strategy.ClusterUpgradeStrategy;
import org.bugapi.bugset.component.dbupgrade.core.strategy.SingleUpgradeStrategy;
import org.bugapi.bugset.component.dbupgrade.core.strategy.UpgradeStrategy;
import org.bugapi.bugset.component.dbupgrade.core.util.DatabaseUpgradeUtil;

/**
 * 数据库升级执行器
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DatabaseUpgradeExecutor {

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

	/**
	 * 脚本文件目录
	 */
	private String scriptDirectory;

	/**
	 * 全局schema
	 */
	private String globalSchema;

	/**
	 * 升级模式
	 */
	private DatabaseUpgradeModeEnum upgradeMode;

	public DatabaseUpgradeExecutor(DataSource dataSource,
			UpgradeConfigParser upgradeConfigParser, String scriptDirectory, DatabaseUpgradeModeEnum upgradeMode,
			String globalSchema) {
		this.dataSource = dataSource;
		this.parser = upgradeConfigParser;
		this.scriptDirectory = scriptDirectory;
		this.upgradeMode = upgradeMode;
		this.globalSchema = globalSchema;
	}

	/**
	 * 数据库升级的入口
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	public void upgrade() throws DatabaseUpgradeException {
		// 获取升级配置信息
		List<UpgradeConfig> upgradeConfigs = parser.parseUpgradeConfigs(scriptDirectory);
		if (CollectionUtil.isEmpty(upgradeConfigs)) {
			log.error("未查询到任何需要升级的配置信息，升级终止...");
			return;
		}

		//升级配置排序
		upgradeConfigs.sort(Comparator.comparingInt(UpgradeConfig::getSeq));

		//获取数据库操作对象
		databaseOperation = DatabaseOperationFactory.getDatabaseOperation(dataSource, globalSchema);

		//判断数据库升级表是否存在，不存在则创建
		if (MetaDataUtil.existTable(dataSource, UPGRADE_TABLE_NAME)) {
			compareAndInitVersionTable(upgradeConfigs);
		} else {
			log.info("数据库升级表不存在，初始化中...");
			initDatabaseVersionTable(upgradeConfigs);
		}

		List<DatabaseUpgradeVersion> upgradeVersions = createUpgradeVersions(upgradeConfigs);

		// 进行ddl数据校验
		ddlScriptValidate(upgradeVersions);

		// 进行dml数据校验
		dmlScriptValidate(upgradeVersions);

		UpgradeStrategy upgradeStrategy;
		if (DatabaseUpgradeModeEnum.SINGLE == upgradeMode) {
			upgradeStrategy = new SingleUpgradeStrategy(dataSource, databaseOperation);
		} else {
			upgradeStrategy = new ClusterUpgradeStrategy(dataSource, databaseOperation);
		}
		upgradeStrategy.doUpgrade(upgradeVersions);
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
		List<DatabaseVersion> databaseVersions;
		try {
			databaseVersions = this.databaseOperation.listDatabaseVersions();
		} catch (SQLException e) {
			throw new DatabaseUpgradeException("查询最近升级数据库升级表失败", e);
		}
		Set<String> existBusinesses = databaseVersions.stream().map(DatabaseVersion::getBusiness)
				.collect(Collectors.toSet());
		List<UpgradeConfig> initConfigs = upgradeConfigs.stream().filter(upgradeConfig -> !existBusinesses
				.contains(upgradeConfig.getBusiness())).collect(Collectors.toList());
		initDatabaseVersionConfigs(initConfigs);
	}

	/**
	 * 填充升级版本管理的字段信息，并返回一个版本管理的集合
	 * @param newVersions 升级配置集合
	 * @return List<VersionInfo> 版本管理的集合
	 */
	private List<DatabaseUpgradeVersion> createUpgradeVersions(List<UpgradeConfig> newVersions)
			throws DatabaseUpgradeException {
		List<DatabaseVersion> oldVersions;
		try {
			oldVersions = this.databaseOperation.listDatabaseVersions();
		} catch (SQLException e) {
			throw new DatabaseUpgradeException("查询最近升级数据库升级表失败", e);
		}
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
			if (StringUtil.isBlank(newVersion.getDdlFileDirectory())) {
				upgradeVersion.setDdlFileDirectory(newVersion.getBusiness());
			} else {
				upgradeVersion.setDdlFileDirectory(newVersion.getDdlFileDirectory());
			}
			if (StringUtil.isBlank(newVersion.getDmlFileDirectory())) {
				upgradeVersion.setDmlFileDirectory(newVersion.getBusiness());
			} else {
				upgradeVersion.setDmlFileDirectory(newVersion.getDmlFileDirectory());
			}
			if (StringUtil.isBlank(newVersion.getDdlFilePrefix())) {
				upgradeVersion.setDdlFilePrefix(newVersion.getBusiness());
			} else {
				upgradeVersion.setDdlFilePrefix(newVersion.getDdlFilePrefix());
			}
			if (StringUtil.isBlank(newVersion.getDmlFilePrefix())) {
				upgradeVersion.setDmlFilePrefix(newVersion.getBusiness());
			} else {
				upgradeVersion.setDmlFilePrefix(newVersion.getDmlFilePrefix());
			}
			return upgradeVersion;
		}).collect(Collectors.toList());
	}



	/**
	 * 校验数据库定义语言的升级脚本
	 * @param upgradeVersions 待升级的版本
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void ddlScriptValidate(List<DatabaseUpgradeVersion> upgradeVersions)
			throws DatabaseUpgradeException {
		for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
			// 批量执行升级的sql语句【无全局事务控制】
			validateSqlFile(upgradeVersion.getBusiness(), upgradeVersion.getDdlCurrentVersion(),
					upgradeVersion.getDdlTargetVersion(), upgradeVersion.getDdlFileDirectory(),
					upgradeVersion.getDdlFilePrefix(), DDL);
		}
	}

	/**
	 * 执行数据库操作语言的升级脚本
	 * @param upgradeVersions 待升级的版本
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void dmlScriptValidate(List<DatabaseUpgradeVersion> upgradeVersions)
			throws DatabaseUpgradeException {
		for (DatabaseUpgradeVersion upgradeVersion : upgradeVersions) {
			validateSqlFile(upgradeVersion.getBusiness(), upgradeVersion.getDmlCurrentVersion(),
					upgradeVersion.getDmlTargetVersion(),
					upgradeVersion.getDmlFileDirectory(), upgradeVersion.getDmlFilePrefix(), DML);
		}
	}

	/**
	 * 校验sql文件是否存在
	 * @param business 模块名称
	 * @param currentVersion 升级前的当前版本号
	 * @param targetVersion 要升级到的目标版本号
	 * @param filePath 脚本所在文件路径
	 * @param filePrefix 脚本文件的前缀
	 * @param languageType 语言类型
	 * @throws DatabaseUpgradeException 数据库升级异常
	 */
	private void validateSqlFile(String business, int currentVersion, int targetVersion,
			String filePath, String filePrefix, String languageType) throws DatabaseUpgradeException {
		Path scriptFilePath;
		while (targetVersion > currentVersion) {
			scriptFilePath = DatabaseUpgradeUtil
					.getScriptFilePath(filePath, filePrefix, ++currentVersion);
			if (!scriptFilePath.toFile().exists()) {
				throw new DatabaseUpgradeException(
						DatabaseUpgradeUtil
								.getSqlFileInfo(business, currentVersion, targetVersion, languageType) + " 升级文件在路径："
								+ scriptFilePath.toString() + "下不存在");
			}
		}
	}
}
