package com.feishu.container;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TokenContainer {
    String userAccessToken;
    String code;
    String refreshToken;
    String appAccessToken;
    String TenantAccessToken;
}
