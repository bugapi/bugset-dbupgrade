package org.bugapi.bugset.component.dbupgrade.core.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * 升级配置实体
 *
 * @author gust
 * @since 0.0.1
 */
@Data
public class UpgradeConfig implements Serializable {
    
    /** serialVersionUID */
    private static final long serialVersionUID = 4105326189854599216L;

    /**
     * 业务
     */
    private String business;

    /**
     * 业务描述
     */
    private String description;

    /**
     * ddl升级版本号
     */
    private int ddlVersion;

    /**
     * dml升级版本号
     */
    private int dmlVersion;

    /**
     * ddl升级脚本相对于脚本根目录的目录
     */
    private String ddlFileDirectory;

    /**
     * dml升级脚本相对于脚本根目录的目录
     */
    private String dmlFileDirectory;

    /**
     * ddl升级脚本前缀
     */
    private String ddlFilePrefix;

    /**
     * ddl升级脚本前缀
     */
    private String dmlFilePrefix;

    /**
     * 执行顺序
     */
    private int seq;

}