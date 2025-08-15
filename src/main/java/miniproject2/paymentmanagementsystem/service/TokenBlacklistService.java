package miniproject2.paymentmanagementsystem.service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    public void blacklistToken(String token) {
        log.info("Adding token to blacklist");blacklistedTokens.add(token);
    }
    public boolean isTokenBlacklisted(String token) {

        boolean isBlacklisted = blacklistedTokens.contains(token);
        log.debug("Token blacklist check result: {}", isBlacklisted);
        return isBlacklisted;
    }

    public void removeExpiredTokens() {
        log.info("Cleaning up expired tokens from blacklist");
    }
}