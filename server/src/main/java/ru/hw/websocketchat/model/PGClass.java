package ru.hw.websocketchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Scope("prototype")
public class PGClass {
    private BigInteger p;
    private BigInteger g;
}
