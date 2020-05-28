package ru.hw.websocketchat.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.hw.websocketchat.blowfish.Blowfish;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class SecurityServiceImpl implements SecurityService {
    private static final int BITS_LENGTH = 1 << 8;
    private static final SecureRandom rnd = getSecureRandom();
    private BigInteger p = generateP();
    private BigInteger g;
    private BigInteger privateKey = BigInteger.valueOf(rnd.nextLong());



    @SneakyThrows
    private static SecureRandom getSecureRandom() {
        return SecureRandom.getInstanceStrong();
    }

    @Override
    public Pair<BigInteger, BigInteger> getPAndG() {
        return Pair.of(p, g);
    }

    @Override
    public BigInteger getOpenKey() {
        return g.modPow(privateKey, p);
    }

    @Override
    public byte[] generateKey(String open) {
        return new BigInteger(open).modPow(privateKey, p).toByteArray();
    }

    private BigInteger generateP() {
        var p = BigInteger.probablePrime(BITS_LENGTH, rnd);

        while (!p.subtract(BigInteger.ONE).divide(BigInteger.TWO).isProbablePrime(99)) {
            p = p.nextProbablePrime();
        }

        this.g = BigInteger.probablePrime(BITS_LENGTH - 1, rnd);

        while (!g.modPow(p.subtract(BigInteger.ONE), p).equals(BigInteger.ONE)) {
            g = g.nextProbablePrime();
        }
        return p;
    }
}
