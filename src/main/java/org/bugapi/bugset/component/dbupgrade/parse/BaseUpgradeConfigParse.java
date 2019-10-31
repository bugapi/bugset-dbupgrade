package org.bugapi.bugset.component.dbupgrade.parse;

import java.io.InputStream;
import java.util.List;
import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: BaseUpgradeConfigParse
 * @Description: 数据库升级配置文件的基础解析类
 * @author: cbb
 * @date: 2018/11/7
 */
public abstract class BaseUpgradeConfigParse {

	private static Logger logger = LoggerFactory.getLogger(BaseUpgradeConfigParse.class);

	/** domain必须与数据库中UPGRADE_VERSION相对应 */
	public static final String DOMAIN = "domain";
	/** 升级描述信息 */
	public static final String COMMENTS = "comments";
	/**数据定义语言升级版本号*/
	public static final String  DDL_VERSION = "ddl-version";
	/**数据操作语言升级版本号*/
	public static final String DML_VERSION = "dml-version";
	/**发布升级版本号*/
	public static final String DDL_PUBLISH_VERSION = "ddl-publish-Version";
	/**发布升级版本号*/
	public static final String DML_PUBLISH_VERSION = "dml-publish-version";
	/** 升级脚本相对于根目录的ddl路径 */
	public static final String DDL_FILE_PATH = "ddl-file-path";
	/** 升级脚本相对于根目录的dml路径 */
	public static final String DML_FILE_PATH = "dml-file-path";
	/** 数据定义语言脚本文件名前缀 */
	public static final String DDL_FILE_PREFIX = "ddl-file-prefix";
	/** 数据定义语言脚本文件名前缀 */
	public static final String DML_FILE_PREFIX = "dml-file-prefix";

	/**
	 * 1、通过applicationContext.getResources来读取classpath下所有的*db-upgrade.xml文件生成一个spring的Resource数组
	 * 2、循环遍历Resource数组
	 * 		1、通过XmlUtil.read(resource.getInputStream(), BaseUpgradeConfigParse.DOMAIN))来解析节点
	 * 		2、将解析的节点封装到UpgradeConfigInfo实体类中。
	 * 		3、将封装的实体保存到一个List中。
	 * 3、返回List<UpgradeConfigInfo>对象
	 * @Title: getUpgradeConfigInfos
	 * @return List<UpgradeConfigInfo> 升级信息
	 */
	public abstract List<UpgradeConfigInfo> getUpgradeConfigInfos();

	public abstract InputStream getFileBufferReader(String filePath);
}
