package org.bugapi.bugset.component.dbupgrade.upgrade;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.bugapi.bugset.base.constant.EnvironmentEnum;
import org.bugapi.bugset.base.util.collection.CollectionUtil;
import org.bugapi.bugset.base.util.sql.DataBaseUtil;
import org.bugapi.bugset.base.util.sql.MetaDataUtil;
import org.bugapi.bugset.base.util.string.StringUtil;
import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfigInfo;
import org.bugapi.bugset.component.dbupgrade.domain.VersionInfo;
import org.bugapi.bugset.component.dbupgrade.parse.BaseUpgradeConfigParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: DefaultUpgrade
 * @Description: 默认数据库升级实现类
 * @author: cbb
 * @date: 2018/11/7
 */
public class DefaultUpgrade {

	private static Logger logger = LoggerFactory.getLogger(DefaultUpgrade.class);

	private static final String UPGRADE_TABLE_NAME = "VERSIONINFO";

	// 基础升级配置解析
	private BaseUpgradeConfigParse baseUpgradeConfigParse;

	public DefaultUpgrade(BaseUpgradeConfigParse baseUpgradeConfigParse) {
		this.baseUpgradeConfigParse = baseUpgradeConfigParse;
	}

	/**
	 * 数据库升级的入口
	 * @Title: upgrade
	 * @param dataSource 数据源
	 * @param environmentType 环境类型{@link EnvironmentEnum}
	 */
	public void upgrade(DataSource dataSource, EnvironmentEnum environmentType) {
		// 获取升级配置信息；
		List<UpgradeConfigInfo> upgradeConfigInfoList = baseUpgradeConfigParse.getUpgradeConfigInfos();
		if (CollectionUtil.isEmpty(upgradeConfigInfoList)) {
			logger.error("没有获取到任何需要升级的配置信息");
			return;
		}
		if (null == dataSource) {
			logger.error("数据源信息为空无法升级");
			return;
		}

		if (!MetaDataUtil.existTable(dataSource, UPGRADE_TABLE_NAME)) {
			// 如果升级表不存在就创建表
			DataBaseUtil.update(dataSource, "CREATE TABLE VERSIONINFO (" +
					"domain VARCHAR(100) NOT NULL," +
					"comments VARCHAR(1000)," +
					"DDLVersion VARCHAR(100) ," +
					"DMLVersion VARCHAR(100) ," +
					"DDLUpgradedate DATE NOT NULL," +
					"DMLUpgradedate DATE NOT NULL," +
					"DDLPUBLISHVERSION VARCHAR(100) ," +
					"DMLPUBLISHVERSION VARCHAR(100) ," +
					"PRIMARY KEY (DOMAIN))");
		}
		// 遍历升级配置信息，完善VersionInfo中的信息，为后边dml和ddl分开升级做准备
		List<VersionInfo> versionInfoList = fillVersionInfo(upgradeConfigInfoList, dataSource, environmentType);

		// 进行ddl数据升级
		ddlScriptupgrade(versionInfoList, dataSource, environmentType);
		// 进行dml数据升级
		dmlScriptupgrade(versionInfoList, dataSource, environmentType);
	}

	/**
	 * 填充升级版本管理的字段信息，并返回一个版本管理的集合
	 * @Title: fillVersionInfo
	 * @param upgradeConfigInfoList 升级配置集合
	 * @param dataSource 数据源
	 * @param environmentType 环境类型{@link EnvironmentEnum}
	 * @return List<VersionInfo> 版本管理的集合
	 */
	private List<VersionInfo> fillVersionInfo(List<UpgradeConfigInfo> upgradeConfigInfoList, DataSource dataSource,
											  EnvironmentEnum environmentType){
		//根据模块名获取数据库版本信息
		VersionInfo versionInfo;
		List<VersionInfo> versionInfoList = new ArrayList<>();
		for (UpgradeConfigInfo upgradeConfigInfo : upgradeConfigInfoList) {
			//根据模块名获取数据库版本信息
			versionInfo = getVersionInfo(dataSource, upgradeConfigInfo);
			// 以下信息是真实升级过程中用到的
			versionInfo.setDdlFilePath(upgradeConfigInfo.getDdlFilePath());
			versionInfo.setDdlFilePrefix(upgradeConfigInfo.getDdlFilePrefix());
			versionInfo.setDmlFilePath(upgradeConfigInfo.getDmlFilePath());
			versionInfo.setDmlFilePrefix(upgradeConfigInfo.getDmlFilePrefix());
			// 如果是发布环境，则读取发布的版本号
			if (EnvironmentEnum.PRO.equals(environmentType)) {
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
	 * @Title: getVersionInfo
	 * @param dataSource 数据源
	 * @param upgradeConfigInfo 升级配置信息
	 * @return VersionInfo 版本信息
	 */
	private VersionInfo getVersionInfo(DataSource dataSource, UpgradeConfigInfo upgradeConfigInfo) {
		// 从数据库获取数据库升级的版本信息
		VersionInfo versionInfo = getVersionInfoFromDataBase(dataSource, upgradeConfigInfo);
		// 数据库获取不到信息，就创建一个基本信息，并保存到数据库中。
		if(versionInfo == null){
			versionInfo = new VersionInfo();
			versionInfo.setDomain(upgradeConfigInfo.getDomain());
			versionInfo.setDdlVersion("0");
			versionInfo.setDmlVersion("0");
			versionInfo.setDdlPublishVersion("0");
			versionInfo.setDmlPublishVersion("0");
			versionInfo.setComments(upgradeConfigInfo.getComments());
			versionInfo.setDdlUpgradeDate(new Date());
			versionInfo.setDmlUpgradeDate(new Date());

			// 初始化第一条默认信息
			DataBaseUtil.update(dataSource,
					"insert into versionInfo (DOMAIN,comments,DDLVersion,DMLVersion,DDLUpgradedate,DMLUpgradedate," +
							"ddlPublishVersion,dmlPublishVersion) values (?, ?, ?, ?, ?, ?,?,?)",
					upgradeConfigInfo.getDomain(),
					upgradeConfigInfo.getComments(),
					"0","0",
					new java.sql.Date(versionInfo.getDdlUpgradeDate().getTime()),
					new java.sql.Date(versionInfo.getDmlUpgradeDate().getTime()),
					"0","0");
		}
		return versionInfo;
	}

	/**
	 * 从数据库获取数据库升级的版本信息
	 * @Title: getVersionInfoFromDataBase
	 * @param dataSource 数据库的数据源
	 * @param upgradeConfigInfo 升级配置信息
	 * @return VersionInfo 版本信息
	 */
	private VersionInfo getVersionInfoFromDataBase(DataSource dataSource, UpgradeConfigInfo upgradeConfigInfo){
		QueryRunner queryRunner = new QueryRunner(dataSource);
		String sql = "select * from versionInfo where domain = ?";
		try {
			return queryRunner.query(sql, new BeanHandler<>(VersionInfo.class),
					upgradeConfigInfo.getDomain());
		} catch (SQLException e) {
			throw new RuntimeException("从数据库查询版本信息失败");
		}
	}

	/**
	 * 执行数据库定义语言的升级脚本
	 * @Title: ddlScriptupgrade
	 * @param versionInfoList 系统模块的版本管理列表
	 * @param dataSource 数据源
	 * @param environmentType 环境类型
	 */
	private void ddlScriptupgrade(List<VersionInfo> versionInfoList, DataSource dataSource, EnvironmentEnum environmentType) {
		for (VersionInfo versionInfo : versionInfoList) {
			if (null == versionInfo) {
				continue;
			}
			// 批量执行升级的sql语句【无全局事务控制】
			batchExecuteUpgradeSql(versionInfo.getDomain(), versionInfo.getDdlCurrentVersion(),
					versionInfo.getDdlTargetVersion(),
					versionInfo.getDdlFilePath(), versionInfo.getDdlFilePrefix(), dataSource, false, environmentType);
		}
	}

	/**
	 * 执行数据库操作语言的升级脚本
	 * @Title: dmlScriptupgrade
	 * @param versionInfoList 系统模块的版本管理列表
	 * @param dataSource 数据源
	 * @param environmentType 环境类型
	 */
	private void dmlScriptupgrade(List<VersionInfo> versionInfoList, DataSource dataSource, EnvironmentEnum environmentType) {
		for (VersionInfo versionInfo : versionInfoList) {
			if (null == versionInfo) {
				continue;
			}
			// 批量执行升级的sql语句【有全局事务控制】
			batchExecuteUpgradeSql(versionInfo.getDomain(), versionInfo.getDmlCurrentVersion(), versionInfo.getDmlTargetVersion(),
					versionInfo.getDmlFilePath(), versionInfo.getDmlFilePrefix(), dataSource, true, environmentType);
		}
	}

	/**
	 * 批量
	 * @Title: batchExecuteUpgradeSql
	 * @param domain 模块名称
	 * @param currentVersion 升级前的当前版本号
	 * @param targetVersion 要升级到的目标版本号
	 * @param filePath 脚本所在文件路径
	 * @param filePrefix 脚本文件的前缀
	 * @param dataSource 数据源
	 * @param flag 是否涉及到事务【批量执行dml语句的时候需要设置为true，有事务处理】
	 * @param environmentType 环境类型
	 */
	private void batchExecuteUpgradeSql(String domain, int currentVersion, int targetVersion, String filePath,
										String filePrefix,
										DataSource dataSource, boolean flag, EnvironmentEnum environmentType) {
		// 读取脚本文件获取到的字符缓冲输入流
		InputStream inputStream;
		List<String> sqlList = null;
		String sql;
		while (targetVersion > currentVersion) {
			// 获取文件路径
			filePath = getScriptFilePath(filePath, filePrefix, ++currentVersion);
			// 获取sql文件的输入流
			inputStream = baseUpgradeConfigParse.getFileBufferReader(filePath);
//			sqlList = SqlUtil.readSql(inputStream);
			// flag=true 有事务控制
			if (flag) {
				batchExecuteSqlWithTransaction(domain, sqlList, dataSource, environmentType, currentVersion,
						filePrefix, flag);
			} else {
				batchExecuteSql(domain, sqlList, dataSource, environmentType, currentVersion, filePrefix, flag);
			}
		}
	}

	/**
	 * 不带事务控制的批量sql执行
	 * @Title: batchExecuteSql
	 * @param domain 模块名称
	 * @param sqlList sql语句集合
	 * @param dataSource 数据源
	 * @param environmentType 环境类型
	 * @param currentVersion 升级前数据库中保存的上次升级的版本信息
	 * @param filePrefix 升级脚本文件的文件前缀
	 * @param flag true:dml类型的sql执行，false:ddl类型的sql执行
	 */
	private void batchExecuteSql(String domain, List<String> sqlList, DataSource dataSource,
								 EnvironmentEnum environmentType, int currentVersion, String filePrefix, boolean flag){
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
			logger.error("--->> " + domain + "Upgrade database from [ "
					+ filePrefix+"_" + (currentVersion-1) + " ] to [ " + filePrefix+"_"+ currentVersion + "] failed: the SQL scripts is: "
					+ sqlTmp);
			throw new RuntimeException("批量执行sql语句报错", e);
		}
		// 升级完成后修改数据库中版本升级版本表中的版本信息
		updateVersionInfo(domain, dataSource, environmentType, currentVersion, filePrefix, flag);
	}

	/**
	 * 带事务控制的批量sql执行
	 * @Title: batchExecuteSqlWithTransaction
	 * @param domain 模块名称
	 * @param sqlList sql语句集合
	 * @param dataSource 数据源
	 * @param environmentType 环境类型
	 * @param currentVersion 升级前数据库中保存的上次升级的版本信息
	 * @param filePrefix 升级脚本文件的文件前缀
	 * @param flag true:dml类型的sql执行，false:ddl类型的sql执行
	 */
	private void batchExecuteSqlWithTransaction(String domain, List<String> sqlList, DataSource dataSource,
												EnvironmentEnum environmentType, int currentVersion, String filePrefix, boolean flag) {
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
			logger.error("--->> " + domain + "Upgrade database from [ "
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
		updateVersionInfo(domain, dataSource, environmentType, currentVersion, filePrefix, flag);
	}

	/**
	 * 升级完成后修改数据库中版本升级版本表中的版本信息
	 * @Title: updateVersionInfo
	 * @param domain 模块名称
	 * @param dataSource 数据源
	 * @param environmentType 环境类型
	 * @param currentVersion 升级后的当前版本号
	 * @param filePrefix 文件前缀
	 * @param flag 有事务控制的是dml脚本、无事务控制的是ddl脚本
	 */
	private void updateVersionInfo(String domain, DataSource dataSource,
								  EnvironmentEnum environmentType, int currentVersion, String filePrefix, boolean flag) {
		String updateSql;
		// flag=true 有事务控制
		if (flag) {
			// 生产环境的dml脚本升级
			if (EnvironmentEnum.PRO.equals(environmentType)) {
				updateSql = "update versionInfo set dmlUpgradeDate = ?, dmlPublishVersion = ? where domain = ?";
			} else {
				updateSql = "update versionInfo set dmlUpgradeDate = ?, dmlVersion = ? where domain = ?";
			}
		} else {
			// 生产环境的ddl脚本升级
			if (EnvironmentEnum.PRO.equals(environmentType)) {
				updateSql = "update versionInfo set ddlUpgradeDate = ?, ddlPublishVersion = ? where domain = ?";
			} else {
				updateSql = "update versionInfo set ddlUpgradeDate = ?, ddlVersion = ? where domain = ?";
			}
		}
		// 更新升级版本表中的数据信息
		DataBaseUtil.update(dataSource, updateSql, new java.sql.Date(System.currentTimeMillis()), currentVersion, domain);
		logger.error("--->> " + domain + "Upgrade database from [ "
				+ filePrefix + "_" + (currentVersion - 1) + " ] to [ " + filePrefix + "_" + currentVersion + "] success.");
	}

	/**
	 * 获取升级脚本的路径
	 * @Title: getScriptFilePath
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
		logger.error("尝试从如下路径查找升级脚本:" + scriptFilePath);
		return scriptFilePath;
	}
}
