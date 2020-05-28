package ru.hw.websocketchat.service;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;

public interface SecurityService {
    Pair<BigInteger, BigInteger> getPAndG();
    BigInteger getOpenKey();
    byte[] generateKey(String open);
}
