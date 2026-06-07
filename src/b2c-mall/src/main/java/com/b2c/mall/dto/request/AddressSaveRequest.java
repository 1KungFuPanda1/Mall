package com.b2c.mall.dto.request; // 请求 DTO 包

import jakarta.validation.constraints.NotBlank; // 非空校验注解
import lombok.Data; // Lombok 数据类

/**
 * 收货地址保存请求 DTO（新增和修改共用）
 *
 * 字段说明：省/市/区为三级行政区域，详情地址为用户填写的具体地址
 */
@Data
public class AddressSaveRequest {

    /** 收货人真实姓名，不能为空 */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /** 收货人手机号码，不能为空 */
    @NotBlank(message = "收货人手机号不能为空")
    private String receiverPhone;

    /** 省份（如"广东省"），不能为空 */
    @NotBlank(message = "省份不能为空")
    private String province;

    /** 城市（如"深圳市"），不能为空 */
    @NotBlank(message = "城市不能为空")
    private String city;

    /** 区/县（如"南山区"），不能为空 */
    @NotBlank(message = "区/县不能为空")
    private String district;

    /** 详细地址（街道、小区名、门牌号等），不能为空 */
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否设为默认地址：1 = 默认, 0 = 非默认 */
    private Integer isDefault;
}
