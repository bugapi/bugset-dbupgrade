package org.bugapi.bugset.component.dbupgrade.domain;

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

    /** domain必须与数据库中UPGRADE_VERSION相对应 */
    private String business;
    
    /** 升级描述信息 */
    /**
     *
     */
    private String description;

    /**数据定义语言升级版本号*/
    private int ddlVersion;

    /**数据操作语言升级版本号*/
    private int dmlVersion;

    /**发布升级版本号*/
    private int ddlPublishVersion;

    /**发布升级版本号*/
    private int dmlPublishVersion;
    
    /** 升级脚本相对于根目录的ddl路径 */
    private String ddlFilePath;

    /** 升级脚本相对于根目录的dml路径 */
    /**
     * 升级脚本相对于根目录的dml路径
     */
    private String dmlFilePath;

    /**
     * 执行顺序
     */
    private int seq;

}