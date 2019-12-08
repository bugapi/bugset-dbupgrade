package org.bugapi.bugset.component.dbupgrade.core.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 数据库升级版本表
 *
 * @author gust
 * @since 0.0.1
 */
@Data
public class DatabaseVersion implements Serializable {
    
    /** serialVersionUID */
    private static final long serialVersionUID = -5140130812690754214L;

    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 业务名称
     */
    private String business;

    /**
     * 业务描述
     */
    private String description;

    /**
     * 数据定义语言升级版本号
     */
    private int ddlVersion;

    /**
     * 数据操作语言升级版本号
     */
    private int dmlVersion;

    /**
     * ddl升级时间
     */
    private Date ddlUpgradeDate;

    /**
     * dml升级时间
     */
    private Date dmlUpgradeDate;

}
