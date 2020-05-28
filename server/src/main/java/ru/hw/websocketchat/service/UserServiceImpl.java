package ru.hw.websocketchat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hw.md5.MD5;
import ru.hw.websocketchat.model.User;
import ru.hw.websocketchat.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Map<String, byte[]> userSalt = new ConcurrentHashMap<>();
    private final Set<String> salt = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isGoodPassword(String username, String password) {
        return userRepository
                .findByUsername(username)
                .filter(u -> u.getPassword().equals(MD5.getHash(password.getBytes())))
                .isPresent();
    }

    @Override
    public Optional<User> saveUser(User user) {
        return Optional.of(userRepository.save(new User(null, user.getUsername(), MD5.getHash(user.getPassword().getBytes()))));
    }

    @Override
    public long usersCount() {
        return userRepository.count();
    }

//    @Override
//    public void saveSalt(String username, String salt) {
//        if (!userSalt.containsKey(username))
//            userSalt.put(username, salt);
//    }

//    @Override
//    public String getSalt(String username) {
//        var salt = userSalt.get(username);
//        userSalt.remove(username);
//        return salt;
//    }

    @Override
    public void saveSalt(String salt) {
        this.salt.add(salt);
    }

    @Override
    public void saveSalt(String salt, byte[] key) {
        userSalt.put(salt, key);
    }



    @Override
    public boolean containsSalt(String salt) {
        if (this.salt.contains(salt)) {
            this.salt.remove(salt);
            return true;
        }

        return false;
    }

    @Override
    public byte[] getKey(String salt) {
        if (userSalt.containsKey(salt)) {
            var key = userSalt.get(salt);
            userSalt.remove(salt);
            return key;
        }
        return null;
    }

}
