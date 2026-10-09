package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tokens_recuperacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacao {


    @Id
    private String id;
    @Indexed(unique = true)
    private String tokenHash;

    private String usuarioId;

    @Indexed(expireAfter = "0s")
    private Instant expiraEm;
}
