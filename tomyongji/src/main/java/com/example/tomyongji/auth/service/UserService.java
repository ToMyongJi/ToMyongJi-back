package com.example.tomyongji.auth.service;

import com.example.tomyongji.auth.entity.User;

public interface UserService {
    Long join(User entity);

    Boolean checkUserIdDuplicate(String userId);
}
