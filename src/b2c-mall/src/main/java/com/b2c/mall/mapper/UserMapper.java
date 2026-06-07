package com.b2c.mall.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper 接口
import com.b2c.mall.entity.User; // 用户实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记注解

/**
 * 用户 Mapper 接口 — 操作 t_user 表
 *
 * 继承 MyBatis-Plus 的 BaseMapper<User>，自动获得通用的 CRUD 方法：
 * - insert(User): 新增用户
 * - updateById(User): 按主键更新用户
 * - selectById(Long): 按主键查询用户
 * - selectList(Wrapper): 按条件查询用户列表
 * - deleteById(Long): 按主键删除用户（由于 @TableLogic，实际为逻辑删除）
 * - selectPage(Page, Wrapper): 分页查询用户
 *
 * 如果有复杂的自定义查询（如多表关联、自定义统计等），
 * 可以在此接口中声明方法，在对应的 XML 文件中编写 SQL
 *
 * @Mapper: MyBatis 注解，标记此接口为 Mapper，Spring 会自动创建代理实现
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已提供常用 CRUD 方法
    // 如需自定义 SQL，在此声明方法签名，在 XML 中实现
}
