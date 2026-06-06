package com.bodymatch.membership.client;

import feign.FeignException;
import org.springframework.stereotype.Service;

@Service
public class IamGateway {
    private final IamClient iamClient;

    public IamGateway(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    public boolean existsUserById(Long userId) {
        try {
            iamClient.getUserById(userId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    public String fetchEmailByUserId(Long userId) {
        try {
            return iamClient.getUserById(userId).email();
        } catch (FeignException.NotFound e) {
            return "";
        }
    }
}
