package org.bugapi.bugset.component.dbupgrade.domain;

import java.io.Serializable;

/**
 * @ClassName: UpgradeConfigInfo
 * @Description: 升级配置信息
 * @author: cbb
 * @date: 2018/10/23
 */
public class UpgradeConfigInfo implements Serializable {
    
    /** serialVersionUID */
    private static final long serialVersionUID = 4105326189854599216L;

    public UpgradeConfigInfo() {}

    /** domain必须与数据库中UPGRADE_VERSION相对应 */
    private String domain;
    
    /** 升级描述信息 */
    private String comments;

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
    private String dmlFilePath;
    
    /** 数据定义语言脚本文件名前缀 */
    private String ddlFilePrefix;

    /** 数据定义语言脚本文件名前缀 */
    private String dmlFilePrefix;
    

    @Override
    public String toString() {
        return "UpgradeConfigInfo{" +
                "domain=" + domain +
                ", comments=" + comments +
                ", ddlVersion=" + ddlVersion +
                ", dmlVersion=" + dmlVersion +
                ", ddlPublishVersion=" + ddlPublishVersion +
                ", dmlPublishVersion=" + dmlPublishVersion +
                ", ddlFilePath=" + ddlFilePath +
                ", dmlFilePath=" + dmlFilePath +
                ", ddlFilePrefix=" + ddlFilePrefix +
                ", dmlFilePrefix=" + dmlFilePrefix +
                '}';
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getDdlVersion() {
        return ddlVersion;
    }

    public void setDdlVersion(int ddlVersion) {
        this.ddlVersion = ddlVersion;
    }

    public int getDmlVersion() {
        return dmlVersion;
    }

    public void setDmlVersion(int dmlVersion) {
        this.dmlVersion = dmlVersion;
    }

    public String getDdlFilePath() {
        return ddlFilePath;
    }

    public void setDdlFilePath(String ddlFilePath) {
        this.ddlFilePath = ddlFilePath;
    }

    public String getDmlFilePath() {
        return dmlFilePath;
    }

    public void setDmlFilePath(String dmlFilePath) {
        this.dmlFilePath = dmlFilePath;
    }

    public String getDdlFilePrefix() {
        return ddlFilePrefix;
    }

    public void setDdlFilePrefix(String ddlFilePrefix) {
        this.ddlFilePrefix = ddlFilePrefix;
    }

    public String getDmlFilePrefix() {
        return dmlFilePrefix;
    }

    public void setDmlFilePrefix(String dmlFilePrefix) {
        this.dmlFilePrefix = dmlFilePrefix;
    }

    public int getDdlPublishVersion() {
        return ddlPublishVersion;
    }

    public void setDdlPublishVersion(int ddlPublishVersion) {
        this.ddlPublishVersion = ddlPublishVersion;
    }

    public int getDmlPublishVersion() {
        return dmlPublishVersion;
    }

    public void setDmlPublishVersion(int dmlPublishVersion) {
        this.dmlPublishVersion = dmlPublishVersion;
    }
}