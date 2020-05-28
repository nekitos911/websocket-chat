package ru.hw.websocketchat.service;

import java.util.Set;

public interface ActiveUsersCountService {
    void addUser(String username);
    void removeUser(String username);
    Set<String> getActiveUserNames();
}
