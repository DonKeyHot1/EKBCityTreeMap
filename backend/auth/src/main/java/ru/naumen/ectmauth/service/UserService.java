package ru.naumen.ectmauth.service;

import ru.naumen.ectmauth.entity.User;

import java.util.Optional;

public interface UserService {
    User save(User user);

    User findByEmail(String email);

    User findByVk_id(String vk_id);

    User findByFb_id(String fb_id);

    Optional<User> findById(Long id);

}