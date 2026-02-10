package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;



import org.example.chocostyle_datn.entity.ResetAccountType;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByAccountTypeAndAccountId(
            ResetAccountType accountType,
            Integer accountId
    );
}