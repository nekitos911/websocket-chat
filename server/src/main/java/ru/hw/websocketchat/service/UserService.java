package ru.hw.websocketchat.service;

import ru.hw.websocketchat.model.User;

import java.util.Optional;

public interface UserService {
    boolean isGoodPassword(String username, String password);
    Optional<User> saveUser(User user);
    long usersCount();
//    void saveSalt(String username, String salt);
//    String getSalt(String username);
    void saveSalt(String salt);
    void saveSalt(String salt, byte[] key);
    boolean containsSalt(String salt);
    byte[] getKey(String salt);
}
