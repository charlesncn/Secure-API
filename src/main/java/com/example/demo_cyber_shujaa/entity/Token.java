package com.example.demo_cyber_shujaa.entity;

import com.example.demo_cyber_shujaa.dto.TokenType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "access_token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token_id")
    public long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public SystemUser user;


    @Column(name = "access_token", columnDefinition = "varchar", unique = true)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "token_type", columnDefinition = "varchar")
    private TokenType tokenType = TokenType.BEARER;
    private boolean revoked;
    private boolean expired;
}
