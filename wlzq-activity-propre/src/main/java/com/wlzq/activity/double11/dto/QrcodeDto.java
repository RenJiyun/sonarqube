package com.wlzq.activity.double11.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class QrcodeDto {
    private String qrCode;
    private String mobile;
    private String qwUserid;
}
