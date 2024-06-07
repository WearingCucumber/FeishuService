package com.feishu.entity;

import lombok.Data;

@Data
public class UserAccessTokenResDO {
    private int code;
    private String msg;
    private UserAccessTokenResDOData data;
    public UserAccessTokenResDO(){
        data = new UserAccessTokenResDOData();
    }
    @Data
    public class UserAccessTokenResDOData {
        private String access_token;
        private String refresh_token;
        private String token_type;
        private int expires_in;
        private long refresh_expires_in;
        private String scope;
    }

}


