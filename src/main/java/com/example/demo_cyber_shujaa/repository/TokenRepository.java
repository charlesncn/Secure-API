package com.example.demo_cyber_shujaa.repository;

import com.example.demo_cyber_shujaa.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
            select t from Token t inner join SystemUser u\s
            on t.user.id = u.id\s
            where u.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    Optional<List<Token>> findAllValidTokenByUser(Long id);

    Optional<Token> findByAccessToken(String token);
}