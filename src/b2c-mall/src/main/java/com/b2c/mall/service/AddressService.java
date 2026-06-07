package com.b2c.mall.service; // Service 接口包

import com.b2c.mall.dto.request.AddressSaveRequest; // 地址保存请求 DTO
import com.b2c.mall.entity.Address; // 地址实体

import java.util.List; // 列表集合

/** 收货地址服务接口 */
public interface AddressService {

    /** 获取用户所有收货地址 */
    List<Address> listByUserId(Long userId);

    /** 新增收货地址 */
    void save(Long userId, AddressSaveRequest request);

    /** 修改收货地址 */
    void update(Long userId, Long addressId, AddressSaveRequest request);

    /** 删除收货地址 */
    void delete(Long userId, Long addressId);

    /** 设置默认地址 */
    void setDefault(Long userId, Long addressId);
}
