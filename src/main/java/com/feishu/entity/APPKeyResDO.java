package com.feishu.entity;

import lombok.Data;

@Data
public class APPKeyResDO {
    private String app_access_token;
    private String expire;
    private String code;
    private String msg;
    private String tenant_access_token;
}
