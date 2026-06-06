package com.bodymatch.membership.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "iam-service")
public interface IamClient {

    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);
}
