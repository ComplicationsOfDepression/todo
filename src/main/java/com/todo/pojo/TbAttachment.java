package com.todo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 附件表
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 路径
     */
    private String url;

    /**
     * 创建人
     */
    private String createUsername;

    /**
     * 修改人
     */
    private String updateUsername;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识
     */
    private Integer isDelete;


}
