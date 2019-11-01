package org.bugapi.bugset.component.dbupgrade.executor;

import static org.bugapi.bugset.component.dbupgrade.constants.DatabaseUpgradeConfigConstants.UPGRADE_TABLE_NAME;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.base.util.collection.CollectionUtil;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.base.util.sql.MetaDataUtil;
import org.bugapi.bugset.base.util.string.StringUtil;
import org.bugapi.bugset.component.dbupgrade.database.DatabaseOperation;
import org.bugapi.bugset.component.dbupgrade.database.MySqlOperationDelegate;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseUpgradeVersion;
import org.bugapi.bugset.component.dbupgrade.domain.DatabaseVersion;
import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfig;
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
	 */
	public void upgrade() {
		// 获取升级配置信息
		List<UpgradeConfig> upgradeConfigs = parser.parseUpgradeConfigs();
		if (CollectionUtil.isEmpty(upgradeConfigs)) {
			log.error("没有获取到任何需要升级的配置信息");
			return;
		}
		upgradeConfigs.sort();

		//TODO 根据数据库类型使用不同的数据库操作代理
		databaseOperation = new MySqlOperationDelegate(dataSource);
		initDatabaseVersionTable(upgradeConfigs);

		// 遍历升级配置信息，完善VersionInfo中的信息，为后边dml和ddl分开升级做准备
		List<DatabaseVersion> versionInfoList = initDatabaseVersion(upgradeConfigs);

		// 进行ddl数据升级
		ddlScriptupgrade(versionInfoList);
		// 进行dml数据升级
		dmlScriptupgrade(versionInfoList);
	}

	/**
	 * 判断数据库升级配置表是否存在
	 * 不存在则初始化
	 */
	private void initDatabaseVersionTable(List<UpgradeConfig> upgradeConfigs) {
		if (!MetaDataUtil.existTable(dataSource, UPGRADE_TABLE_NAME)) {
			// 如果升级表不存在就创建表
			this.databaseOperation.initDatabaseVersionTable();
			upgradeConfigs.stream().map(config -> {
				DatabaseVersion databaseVersion = new DatabaseVersion();
				databaseVersion.setBusiness(config.getBusiness());
			})
		}
	}

	/**
	 * 填充升级版本管理的字段信息，并返回一个版本管理的集合
	 * @param upgradeConfigInfoList 升级配置集合
	 * @return List<VersionInfo> 版本管理的集合
	 */
	private List<DatabaseUpgradeVersion> initDatabaseVersion(List<UpgradeConfig> upgradeConfigInfoList){
		//根据模块名获取数据库版本信息
		DatabaseUpgradeVersion upgradeVersion;
		List<DatabaseUpgradeVersion> versionInfoList = new ArrayList<>();
		for (UpgradeConfig upgradeConfigInfo : upgradeConfigInfoList) {
			//根据模块名获取数据库版本信息
			versionInfo = getVersionInfo(this.dataSource, upgradeConfigInfo);
			// 以下信息是真实升级过程中用到的
			versionInfo.setDdlFilePath(upgradeConfigInfo.getDdlFilePath());
			versionInfo.setDdlFilePrefix(upgradeConfigInfo.getDdlFilePrefix());
			versionInfo.setDmlFilePath(upgradeConfigInfo.getDmlFilePath());
			versionInfo.setDmlFilePrefix(upgradeConfigInfo.getDmlFilePrefix());
			// 如果是发布环境，则读取发布的版本号
			if (EnvironmentEnum.PRO.equals(this.environment)) {
				versionInfo.setDdlTargetVersion(upgradeConfigInfo.getDdlPublishVersion());
				versionInfo.setDmlTargetVersion(upgradeConfigInfo.getDmlPublishVersion());
				versionInfo.setDdlCurrentVersion(versionInfo.getDdlPublishVersion() == null ? 0 :
						Integer.parseInt(versionInfo.getDdlPublishVersion()));
				versionInfo.setDmlCurrentVersion(versionInfo.getDmlPublishVersion() == null ? 0 :
						Integer.parseInt(versionInfo.getDmlPublishVersion()));
			}else{
				versionInfo.setDdlTargetVersion(upgradeConfigInfo.getDdlVersion());
				versionInfo.setDmlTargetVersion(upgradeConfigInfo.getDmlVersion());
				versionInfo.setDdlCurrentVersion(versionInfo.getDdlVersion() == null ? 0 :
						Integer.parseInt(versionInfo.getDdlVersion()));
				versionInfo.setDmlCurrentVersion(versionInfo.getDmlVersion() == null ? 0 :
						Integer.parseInt(versionInfo.getDmlVersion()));
			}
			versionInfoList.add(versionInfo);
		}
		return versionInfoList;
	}

	/**
	 * 获取升级的版本信息
	 * @param dataSource 数据源
	 * @param upgradeConfigInfo 升级配置信息
	 * @return VersionInfo 版本信息
	 */
	private DatabaseVersion getVersionInfo(DataSource dataSource, UpgradeConfig upgradeConfigInfo) {
		// 从数据库获取数据库升级的版本信息
		DatabaseVersion versionInfo = getVersionInfoFromDataBase(dataSource, upgradeConfigInfo);
		// 数据库获取不到信息，就创建一个基本信息，并保存到数据库中。
		if(versionInfo == null){
			versionInfo = new DatabaseVersion();
			versionInfo.setDomain(upgradeConfigInfo.getDomain());
			versionInfo.setDdlVersion("0");
			versionInfo.setDmlVersion("0");
			versionInfo.setDdlPublishVersion("0");
			versionInfo.setDmlPublishVersion("0");
			versionInfo.setComments(upgradeConfigInfo.getComment());
			versionInfo.setDdlUpgradeDate(new Date());
			versionInfo.setDmlUpgradeDate(new Date());

			// 初始化第一条默认信息
			DataBaseUtil.update(dataSource,
					"insert into versionInfo (DOMAIN,comments,DDLVersion,DMLVersion,DDLUpgradedate,DMLUpgradedate," +
							"ddlPublishVersion,dmlPublishVersion) values (?, ?, ?, ?, ?, ?,?,?)",
					upgradeConfigInfo.getDomain(),
					upgradeConfigInfo.getComment(),
					"0","0",
					new java.sql.Date(versionInfo.getDdlUpgradeDate().getTime()),
					new java.sql.Date(versionInfo.getDmlUpgradeDate().getTime()),
					"0","0");
		}
		return versionInfo;
	}

	/**
	 * 从数据库获取数据库升级的版本信息
	 * @param dataSource 数据库的数据源
	 * @param upgradeConfigInfo 升级配置信息
	 * @return VersionInfo 版本信息
	 */
	private DatabaseVersion getVersionInfoFromDataBase(DataSource dataSource, UpgradeConfig upgradeConfigInfo){
		QueryRunner queryRunner = new QueryRunner(dataSource);
		String sql = "select * from versionInfo where domain = ?";
		try {
			return queryRunner.query(sql, new BeanHandler<>(DatabaseVersion.class),
					upgradeConfigInfo.getDomain());
		} catch (SQLException e) {
			throw new RuntimeException("从数据库查询版本信息失败");
		}
	}

	/**
	 * 执行数据库定义语言的升级脚本
	 * @param versionInfoList 系统模块的版本管理列表
	 */
	private void ddlScriptupgrade(List<DatabaseVersion> versionInfoList) {
		for (DatabaseVersion versionInfo : versionInfoList) {
			if (null == versionInfo) {
				continue;
			}
			// 批量执行升级的sql语句【无全局事务控制】
			batchExecuteUpgradeSql(versionInfo.getDomain(), versionInfo.getDdlCurrentVersion(),
					versionInfo.getDdlTargetVersion(),
					versionInfo.getDdlFilePath(), versionInfo.getDdlFilePrefix(), false);
		}
	}

	/**
	 * 执行数据库操作语言的升级脚本
	 * @param versionInfoList 系统模块的版本管理列表
	 */
	private void dmlScriptupgrade(List<DatabaseVersion> versionInfoList) {
		for (DatabaseVersion versionInfo : versionInfoList) {
			if (null == versionInfo) {
				continue;
			}
			// 批量执行升级的sql语句【有全局事务控制】
			batchExecuteUpgradeSql(versionInfo.getDomain(), versionInfo.getDmlCurrentVersion(), versionInfo.getDmlTargetVersion(),
					versionInfo.getDmlFilePath(), versionInfo.getDmlFilePrefix(), true);
		}
	}

	/**
	 * 批量
	 * @param domain 模块名称
	 * @param currentVersion 升级前的当前版本号
	 * @param targetVersion 要升级到的目标版本号
	 * @param filePath 脚本所在文件路径
	 * @param filePrefix 脚本文件的前缀
	 * @param flag 是否涉及到事务【批量执行dml语句的时候需要设置为true，有事务处理】
	 */
	private void batchExecuteUpgradeSql(String domain, int currentVersion, int targetVersion, String filePath,
										String filePrefix, boolean flag) {
		// 读取脚本文件获取到的字符缓冲输入流
		InputStream inputStream;
		List<String> sqlList = null;
		String sql;
		while (targetVersion > currentVersion) {
			// 获取文件路径
			filePath = getScriptFilePath(filePath, filePrefix, ++currentVersion);
			// 获取sql文件的输入流
//			inputStream = parser.getFileBufferReader(filePath);
//			sqlList = SqlUtil.readSql(inputStream);
			// flag=true 有事务控制
			if (flag) {
				batchExecuteSqlWithTransaction(domain, sqlList, currentVersion, filePrefix, flag);
			} else {
				batchExecuteSql(domain, sqlList, currentVersion, filePrefix, flag);
			}
		}
	}

	/**
	 * 不带事务控制的批量sql执行
	 * @param domain 模块名称
	 * @param sqlList sql语句集合
	 * @param currentVersion 升级前数据库中保存的上次升级的版本信息
	 * @param filePrefix 升级脚本文件的文件前缀
	 * @param flag true:dml类型的sql执行，false:ddl类型的sql执行
	 */
	private void batchExecuteSql(String domain, List<String> sqlList, int currentVersion, String filePrefix, boolean flag){
		if (null == dataSource){
			throw new RuntimeException("批量执行sql语句时，数据源为空");
		}
		String sqlTmp = "";
		try (Connection con = dataSource.getConnection();
			Statement statement = con.createStatement()) {
			for (String sql : sqlList){
				if(StringUtil.isNotEmpty(sql)){
					sqlTmp = sql;
					statement.execute(sql);
				}
			}
		}catch (SQLException e){
			log.error("--->> " + domain + "Upgrade database from [ "
					+ filePrefix+"_" + (currentVersion-1) + " ] to [ " + filePrefix+"_"+ currentVersion + "] failed: the SQL scripts is: "
					+ sqlTmp);
			throw new RuntimeException("批量执行sql语句报错", e);
		}
		// 升级完成后修改数据库中版本升级版本表中的版本信息
		updateVersionInfo(domain, currentVersion, filePrefix, flag);
	}

	/**
	 * 带事务控制的批量sql执行
	 * @param domain 模块名称
	 * @param sqlList sql语句集合
	 * @param currentVersion 升级前数据库中保存的上次升级的版本信息
	 * @param filePrefix 升级脚本文件的文件前缀
	 * @param flag true:dml类型的sql执行，false:ddl类型的sql执行
	 */
	private void batchExecuteSqlWithTransaction(String domain, List<String> sqlList, int currentVersion, String filePrefix, boolean flag) {
		if (null == dataSource) {
			throw new RuntimeException("批量执行sql语句时，数据源为空");
		}
		Connection con = null;
		Statement statement = null;
		String sqlTmp = "";
		try {
			con = dataSource.getConnection();
			// 开启事务不自动提交
			con.setAutoCommit(false);
			statement = con.createStatement();
			for (String sql : sqlList) {
				if (StringUtil.isNotEmpty(sql)) {
					sqlTmp = sql;
					statement.execute(sql);
				}
			}
			con.commit();
		} catch (SQLException e) {
			log.error("--->> " + domain + "Upgrade database from [ "
					+ filePrefix + "_" + (currentVersion - 1) + " ] to [ " + filePrefix + "_" + currentVersion + "] failed: the SQL scripts is: "
					+ sqlTmp);
			if (null != con) {
				try {
					// sql执行报错事务回滚
					con.rollback();
				} catch (SQLException ex) {
					throw new RuntimeException("批量执行sql语句时，事务回滚异常");
				}
			}
		} finally {
			if (null != statement) {
				try {
					statement.close();
				} catch (SQLException ex) {
					throw new RuntimeException("批量执行sql语句时，关闭statement报错");
				}
			}

			if (null != con) {
				try {
					con.close();
				} catch (SQLException ex) {
					throw new RuntimeException("批量执行sql语句时，关闭数据库连接报错");
				}
			}
		}
		// 升级完成后修改数据库中版本升级版本表中的版本信息
		updateVersionInfo(domain, currentVersion, filePrefix, flag);
	}

	/**
	 * 升级完成后修改数据库中版本升级版本表中的版本信息
	 * @param domain 模块名称
	 * @param currentVersion 升级后的当前版本号
	 * @param filePrefix 文件前缀
	 * @param flag 有事务控制的是dml脚本、无事务控制的是ddl脚本
	 */
	private void updateVersionInfo(String domain, int currentVersion, String filePrefix, boolean flag) {
		String updateSql;
		// flag=true 有事务控制
		if (flag) {
			// 生产环境的dml脚本升级
			if (EnvironmentEnum.PRO == this.environment) {
				updateSql = "update versionInfo set dmlUpgradeDate = ?, dmlPublishVersion = ? where domain = ?";
			} else {
				updateSql = "update versionInfo set dmlUpgradeDate = ?, dmlVersion = ? where domain = ?";
			}
		} else {
			// 生产环境的ddl脚本升级
			if (EnvironmentEnum.PRO == this.environment) {
				updateSql = "update versionInfo set ddlUpgradeDate = ?, ddlPublishVersion = ? where domain = ?";
			} else {
				updateSql = "update versionInfo set ddlUpgradeDate = ?, ddlVersion = ? where domain = ?";
			}
		}
		// 更新升级版本表中的数据信息
		DataBaseUtil.update(dataSource, updateSql, new java.sql.Date(System.currentTimeMillis()), currentVersion, domain);
		log.error("--->> " + domain + "Upgrade database from [ "
				+ filePrefix + "_" + (currentVersion - 1) + " ] to [ " + filePrefix + "_" + currentVersion + "] success.");
	}

	/**
	 * 获取升级脚本的路径
	 * @param filePath 解析到的文件路径
	 * @param filePrefix 解析到文件名前缀
	 * @param targetVersion 要执行的脚本的版本号
	 * @return String 完整的脚本文件路径
	 */
	private String getScriptFilePath(String filePath, String filePrefix, int targetVersion){
		// 替换路径分隔符为统一的"/", trim头尾空格, 如果路径不以"/"结尾, 则追加"/"
		filePath = filePath.trim().replace("/", File.separator);
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}
		// 拼接完整的路径
		String scriptFilePath = filePath + filePrefix + "_" + targetVersion + ".sql";
		log.error("尝试从如下路径查找升级脚本:" + scriptFilePath);
		return scriptFilePath;
	}
}
