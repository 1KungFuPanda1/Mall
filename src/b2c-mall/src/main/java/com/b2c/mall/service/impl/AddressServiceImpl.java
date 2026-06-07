package com.b2c.mall.service.impl; // Service 实现类包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper; // 更新条件构造器
import com.b2c.mall.dto.request.AddressSaveRequest; // 地址保存请求
import com.b2c.mall.entity.Address; // 地址实体
import com.b2c.mall.exception.BusinessException; // 业务异常
import com.b2c.mall.mapper.AddressMapper; // 地址 Mapper
import com.b2c.mall.service.AddressService; // 地址 Service 接口
import org.springframework.beans.factory.annotation.Autowired; // 自动注入
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务注解

import java.util.List; // 列表

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper; // 地址 Mapper

    @Override
    public List<Address> listByUserId(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>(); // 条件构造器
        wrapper.eq(Address::getUserId, userId); // 按用户ID查询
        wrapper.orderByDesc(Address::getIsDefault); // 默认地址排前面
        wrapper.orderByDesc(Address::getCreateTime); // 最新创建排前面
        return addressMapper.selectList(wrapper); // 查询返回
    }

    @Override
    @Transactional
    public void save(Long userId, AddressSaveRequest request) {
        // 如果设为默认地址，先将该用户的其他地址取消默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId); // 清除其他默认地址
        }
        Address address = new Address(); // 创建实体
        address.setUserId(userId); // 用户ID
        address.setReceiverName(request.getReceiverName()); // 收货人
        address.setReceiverPhone(request.getReceiverPhone()); // 收货人手机
        address.setProvince(request.getProvince()); // 省
        address.setCity(request.getCity()); // 市
        address.setDistrict(request.getDistrict()); // 区
        address.setDetail(request.getDetail()); // 详细地址
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0); // 是否默认
        addressMapper.insert(address); // 插入数据库
    }

    @Override
    @Transactional
    public void update(Long userId, Long addressId, AddressSaveRequest request) {
        Address address = addressMapper.selectById(addressId); // 查询地址
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在"); // 校验权限
        }
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId); // 清除其他默认
        }
        address.setReceiverName(request.getReceiverName()); // 更新收货人
        address.setReceiverPhone(request.getReceiverPhone()); // 更新手机
        address.setProvince(request.getProvince()); // 更新省
        address.setCity(request.getCity()); // 更新市
        address.setDistrict(request.getDistrict()); // 更新区
        address.setDetail(request.getDetail()); // 更新详细地址
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0); // 更新默认标记
        addressMapper.updateById(address); // 更新数据库
    }

    @Override
    public void delete(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId); // 查询地址
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在"); // 校验
        }
        addressMapper.deleteById(addressId); // 逻辑删除
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId); // 查询
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在"); // 校验
        }
        clearDefault(userId); // 清除其他默认
        address.setIsDefault(1); // 设当前为默认
        addressMapper.updateById(address); // 更新
    }

    /** 清除用户的所有默认地址标记（SQL批量更新） */
    private void clearDefault(Long userId) {
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>(); // 更新条件
        wrapper.eq(Address::getUserId, userId); // 按用户
        wrapper.eq(Address::getIsDefault, 1); // 当前是默认的
        wrapper.set(Address::getIsDefault, 0); // 全部改为非默认
        addressMapper.update(null, wrapper); // 批量更新
    }
}
