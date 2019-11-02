package org.bugapi.bugset.component.dbupgrade.domain;

import lombok.Data;

/**
 * 数据库升级版本表
 *
 * @author gust
 * @since 0.0.1
 */
@Data
public class DatabaseUpgradeVersion {

    /**
     * 业务
     */
    private String business;

    /** ddl当前版本号【本次升级前的版本号】 */
    private int ddlCurrentVersion;

    /** dml当前版本号【本次升级前的版本号】 */
    private int dmlCurrentVersion;

    /** ddl目标版本号【本次要升级到的版本号】 */
    private int ddlTargetVersion;

    /** dml目标版本号【本次要升级到的版本号】 */
    private int dmlTargetVersion;

    /** 升级脚本相对于根目录的ddl路径 */
    private String ddlFilePath;

    /** 升级脚本相对于根目录的dml路径 */
    private String dmlFilePath;

    /** 数据定义语言脚本文件名前缀 */
    private String ddlFilePrefix;

    /** 数据定义语言脚本文件名前缀 */
    private String dmlFilePrefix;

}
