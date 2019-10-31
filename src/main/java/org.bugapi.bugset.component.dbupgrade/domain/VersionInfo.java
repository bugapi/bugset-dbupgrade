package org.bugapi.bugset.component.dbupgrade.domain;

import java.io.Serializable;
import java.util.Date;

/** 升级版本实体类
 * @ClassName: VersionInfo
 * @author: cbb
 * @date: 2018/11/7
 */
public class VersionInfo implements Serializable {
    
    /** serialVersionUID */
    private static final long serialVersionUID = -5140130812690754214L;

    /** 对应插件或模块名称 */
    private String domain;

    /** 描述信息 */
    private String comments;

    /** 数据定义语言升级版本号【非生产环境】 */
    private String ddlVersion;

    /** 数据操作语言升级版本号【非生产环境】 */
    private String dmlVersion;

    /** 数据定义语言发布升级版本号【生产环境】 */
    private String ddlPublishVersion;

    /** 数据操作语言发布升级版本号【生产环境】 */
    private String dmlPublishVersion;

    /** ddl升级时间 */
    private Date ddlUpgradeDate;

    /** dml升级时间 */
    private Date dmlUpgradeDate;

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
    

    public VersionInfo() {
    }
    
    @Override
    public String toString() {
        return "VersionInfo{" +
                "  domain=" + domain +
                ", comments=" + comments +
                ", ddlVersion=" + ddlVersion +
                ", dmlVersion=" + dmlVersion +
                ", ddlPublishVersion=" + ddlPublishVersion +
                ", dmlPublishVersion=" + dmlPublishVersion +
                ", ddlUpgradeDate=" + ddlUpgradeDate +
                ", dmlUpgradeDate=" + dmlUpgradeDate +
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

    public String getDdlVersion() {
        return ddlVersion;
    }

    public void setDdlVersion(String ddlVersion) {
        this.ddlVersion = ddlVersion;
    }

    public String getDmlVersion() {
        return dmlVersion;
    }

    public void setDmlVersion(String dmlVersion) {
        this.dmlVersion = dmlVersion;
    }

    public String getDdlPublishVersion() {
        return ddlPublishVersion;
    }

    public void setDdlPublishVersion(String ddlPublishVersion) {
        this.ddlPublishVersion = ddlPublishVersion;
    }

    public String getDmlPublishVersion() {
        return dmlPublishVersion;
    }

    public void setDmlPublishVersion(String dmlPublishVersion) {
        this.dmlPublishVersion = dmlPublishVersion;
    }

    public Date getDdlUpgradeDate() {
        return ddlUpgradeDate;
    }

    public void setDdlUpgradeDate(Date ddlUpgradeDate) {
        this.ddlUpgradeDate = ddlUpgradeDate;
    }

    public Date getDmlUpgradeDate() {
        return dmlUpgradeDate;
    }

    public void setDmlUpgradeDate(Date dmlUpgradeDate) {
        this.dmlUpgradeDate = dmlUpgradeDate;
    }

    public int getDdlCurrentVersion() {
        return ddlCurrentVersion;
    }

    public void setDdlCurrentVersion(int ddlCurrentVersion) {
        this.ddlCurrentVersion = ddlCurrentVersion;
    }

    public int getDmlCurrentVersion() {
        return dmlCurrentVersion;
    }

    public void setDmlCurrentVersion(int dmlCurrentVersion) {
        this.dmlCurrentVersion = dmlCurrentVersion;
    }

    public int getDdlTargetVersion() {
        return ddlTargetVersion;
    }

    public void setDdlTargetVersion(int ddlTargetVersion) {
        this.ddlTargetVersion = ddlTargetVersion;
    }

    public int getDmlTargetVersion() {
        return dmlTargetVersion;
    }

    public void setDmlTargetVersion(int dmlTargetVersion) {
        this.dmlTargetVersion = dmlTargetVersion;
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
}
