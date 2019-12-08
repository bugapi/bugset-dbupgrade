package org.bugapi.bugset.component.dbupgrade.core.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bugapi.bugset.base.util.collection.CollectionUtil;
import org.bugapi.bugset.base.util.xml.JaxbXmlUtil;
import org.bugapi.bugset.component.dbupgrade.core.domain.UpgradeConfig;

/**
 * 默认数据库升级配置解析器
 *
 * @author gust
 * @since 0.0.1
 */
@Slf4j
public class DefaultUpgradeConfigParser implements UpgradeConfigParser {

	/**
	 * 解析配置文件获取升级配置信息列表
	 * @return List<UpgradeConfigInfo> 升级的配置信息列表
	 * @param scriptRootDirectory 脚本根目录
	 */
	@Override
	public List<UpgradeConfig> parseUpgradeConfigs(String scriptRootDirectory) {
		URL upgrade = ClassLoader.getSystemResource(scriptRootDirectory);
		URI uri;
		try {
			uri = upgrade.toURI();
		} catch (URISyntaxException e) {
			log.error("升级配置文件读取失败", e);
			return Collections.emptyList();
		}
		List<UpgradeConfig> upgradeConfigs;
		try {
			List<Path> configFilePaths = Files
					.find(Paths.get(uri), 2, (path, fileAttributes) -> path.toString().endsWith("db-upgrade.xml"),
							FileVisitOption.FOLLOW_LINKS).collect(Collectors.toList());
			if (CollectionUtil.isEmpty(configFilePaths)) {
				return Collections.emptyList();
			}
			upgradeConfigs = new ArrayList<>(configFilePaths.size());
			UpgradeConfig upgradeConfig;
			for (Path path : configFilePaths) {
				upgradeConfig = JaxbXmlUtil
						.convertToJavaBean(path.toAbsolutePath().toString(), UpgradeConfig.class);
				upgradeConfigs.add(upgradeConfig);
			}
		} catch (IOException e) {
			log.error("升级配置文件读取失败", e);
			return Collections.emptyList();
		}
		return upgradeConfigs;
	}

}
