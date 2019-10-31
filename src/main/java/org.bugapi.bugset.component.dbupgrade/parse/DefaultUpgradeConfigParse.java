package org.bugapi.bugset.component.dbupgrade.parse;

import org.bugapi.bugset.component.dbupgrade.domain.UpgradeConfigInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: DefaultUpgradeConfigParse
 * @Description: 默认的系统升级配置解析类
 * @author: zhangxw
 * @date: 2019/6/17
 */
@Slf4j
public class DefaultUpgradeConfigParse extends BaseUpgradeConfigParse {

	/**
	 * 解析配置文件获取升级配置信息列表
	 * @Title: getUpgradeConfigInfos
	 * @return List<UpgradeConfigInfo> 升级的配置信息列表
	 */
	@Override
	public List<UpgradeConfigInfo> getUpgradeConfigInfos() {
		try {
			//读取upgrade目录下的所有资源文件
//			Resource[] resources = SpringContextUtil.getResources("classpath:*/*db-upgrade.xml");
			UpgradeConfigInfo upgradeConfigInfo;
			List<UpgradeConfigInfo> upgradeConfigInfos = new ArrayList<>();
//			for (Resource resource : resources) {
//				if (resource.exists()) {
//					upgradeConfigInfo = new UpgradeConfigInfo();
//					upgradeConfigInfo.setDomain(XmlUtil.read(resource.getInputStream(), "domain"));
//					upgradeConfigInfo.setComments(XmlUtil.read(resource.getInputStream(), "comments"));
//					upgradeConfigInfo.setDdlVersion(Integer.parseInt((String) Objects.requireNonNull(XmlUtil.read(resource.getInputStream(), "ddl-version"))));
//					upgradeConfigInfo.setDmlVersion(Integer.parseInt((String)Objects.requireNonNull(XmlUtil.read(resource.getInputStream(), "dml-version"))));
//					upgradeConfigInfo.setDdlPublishVersion(Integer.parseInt((String)Objects.requireNonNull(XmlUtil.read(resource.getInputStream(), "ddl-publish-Version"))));
//					upgradeConfigInfo.setDmlPublishVersion(Integer.parseInt((String)Objects.requireNonNull(XmlUtil.read(resource.getInputStream(), "dml-publish-version"))));
//					upgradeConfigInfo.setDdlFilePath(XmlUtil.read(resource.getInputStream(), "ddl-file-path"));
//					upgradeConfigInfo.setDmlFilePath(XmlUtil.read(resource.getInputStream(), "dml-file-path"));
//					upgradeConfigInfo.setDdlFilePrefix(XmlUtil.read(resource.getInputStream(), "ddl-file-prefix"));
//					upgradeConfigInfo.setDmlFilePrefix(XmlUtil.read(resource.getInputStream(), "dml-file-prefix"));
//					upgradeConfigInfos.add(upgradeConfigInfo);
//				}
//			}
			return upgradeConfigInfos;
		} catch (Exception e) {
			log.error("Loadding upgrade failure", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析classpath下的文件获取数据流
	 * @Title: getFileBufferReader
	 * @param filePath 文件路径
	 * @return InputStream 数据流
	 */
	@Override
	public InputStream getFileBufferReader(String filePath) {
		try {
			InputStream inputStream = null;
//			Resource[] resource = SpringContextUtil.getResources("classpath:/" + filePath);
//			if (resource != null && resource[0] != null) {
//				inputStream = resource[0].getInputStream();
//			}
			return inputStream;
		} catch (Exception e) {
			log.error("获取升级脚本reader失败:" + filePath, e);
			throw new RuntimeException("无法从如下路径获取升级脚本:" + filePath, e);
		}
	}
}
