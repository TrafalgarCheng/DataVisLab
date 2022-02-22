package com.ssm.demo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 13
 * @date 2018-07-18
 */
public class Picture implements Serializable {

    //主键
    private Integer id;
    //图片地址
    private String path;
    //备注
    private String remark;
    //添加时间
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
