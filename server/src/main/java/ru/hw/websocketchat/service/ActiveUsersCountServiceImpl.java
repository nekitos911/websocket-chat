package ru.hw.websocketchat.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveUsersCountServiceImpl implements ActiveUsersCountService {
    private final Set<String> users = ConcurrentHashMap.newKeySet();

    @Override
    public void addUser(String username) {
        users.add(username);
    }

    @Override
    public void removeUser(String username) {
        users.remove(username);
    }

    @Override
    public Set<String> getActiveUserNames() {
        return users;
    }
}
