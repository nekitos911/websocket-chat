package ru.hw.websocketchat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.hw.websocketchat.model.PGClass;
import ru.hw.websocketchat.model.SaltKey;
import ru.hw.websocketchat.service.SecurityService;
import ru.hw.websocketchat.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SecurityController {
    private final UserService userService;
    private final SecurityService securityService;

//    @GetMapping("/salt")
//    public ResponseEntity<String> getSalt(@RequestParam String username, HttpServletRequest request) {
//        var salt = RandomStringUtils.randomAscii(1, 512).replace("\t", "").replace("\r", "").replace("\n", "");
//        userService.saveSalt(request.getRemoteAddr() + ":" + request.getRemotePort(), salt);
//        return ResponseEntity.ok(salt);
//    }

    @GetMapping("/salt")
    public ResponseEntity<String> getSalt() {
        var salt = RandomStringUtils.randomAscii(1, 512).replace("\t", "").replace("\r", "").replace("\n", "");
        userService.saveSalt(salt);
        return ResponseEntity.ok(salt);
    }

    @GetMapping("/pg")
    public ResponseEntity<PGClass> getPAndG() {
        var pg = securityService.getPAndG();
        return ResponseEntity.ok(new PGClass(pg.getLeft(), pg.getRight()));
    }

    @GetMapping("/openKey")
    public ResponseEntity<SaltKey> getOpenKey(@RequestParam String clientKey) {
        var salt = UUID.randomUUID().toString();
        var res = securityService.getOpenKey();
        var key = securityService.generateKey(clientKey);
        userService.saveSalt(salt, key);

        return ResponseEntity.ok(new SaltKey(salt, res.toString()));
    }
}
