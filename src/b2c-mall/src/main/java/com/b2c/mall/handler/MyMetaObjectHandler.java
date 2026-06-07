package com.b2c.mall.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 作用：当实体类字段标注了 @TableField(fill = FieldFill.INSERT) 等注解时，
 * 在 INSERT 或 UPDATE 操作前自动为这些字段赋值，避免手动设置 createTime、updateTime。
 *
 * 覆盖范围：所有实体（User、Address、Category、Product、Order、Banner）
 * 填充字段：createTime → INSERT 时自动设为当前时间
 *           updateTime → INSERT 和 UPDATE 时都自动设为当前时间
 *
 * @Component: 注册为 Spring Bean，MyBatis-Plus 自动发现并调用
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /** 实体中创建时间字段名（与 Java 属性名一致，非数据库列名） */
    private static final String CREATE_TIME = "createTime";

    /** 实体中更新时间字段名 */
    private static final String UPDATE_TIME = "updateTime";

    /**
     * 插入时自动填充
     * 在执行 insert 操作前，自动为 createTime 和 updateTime 赋值为当前时间
     *
     * @param metaObject MyBatis 元对象，可从中获取待插入的实体字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // strictInsertFill：如果字段有 @TableField(fill=INSERT) 且值为 null，则填充
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
    }

    /**
     * 更新时自动填充
     * 在执行 update 操作前，自动为 updateTime 赋值为当前时间
     *
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // strictUpdateFill：如果字段有 @TableField(fill=INSERT_UPDATE) 且值为 null，则填充
        this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
