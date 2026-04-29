package com.juviai.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juviai.user.config.JuviAICoinsProperties;
import com.juviai.user.domain.CoinCategory;
import com.juviai.user.domain.UserCoinCategoryProgress;
import com.juviai.user.domain.UserCoinLedger;
import com.juviai.user.domain.UserCoins;
import com.juviai.user.repo.UserCoinCategoryProgressRepository;
import com.juviai.user.repo.UserCoinLedgerRepository;
import com.juviai.user.repo.UserCoinsRepository;

@Service
public class CoinService {
    private final JuviAICoinsProperties cfg;
    private final UserCoinsRepository coinsRepo;
    private final UserCoinLedgerRepository ledgerRepo;
    private final UserCoinCategoryProgressRepository progressRepo;

    public CoinService(JuviAICoinsProperties cfg,
                       UserCoinsRepository coinsRepo,
                       UserCoinLedgerRepository ledgerRepo,
                       UserCoinCategoryProgressRepository progressRepo) {
        this.cfg = cfg;
        this.coinsRepo = coinsRepo;
        this.ledgerRepo = ledgerRepo;
        this.progressRepo = progressRepo;
    }

    private JuviAICoinsProperties.Category categoryConfig(CoinCategory category) {
        return switch (category) {
            case EDUCATION -> cfg.getEducation();
            case PROJECT -> cfg.getProject();
            case INTERNSHIP -> cfg.getInternship();
            case SKILL -> cfg.getSkill();
            case TITLE -> cfg.getTitle();
        };
    }

    @Transactional
    public int award(UUID userId, CoinCategory category, String reason, UUID relatedId) {
        JuviAICoinsProperties.Category c = categoryConfig(category);
        int perAction = c.getPerAction();
        int cap = c.getMaxPerCategory();
        if (perAction <= 0 || cap <= 0) return 0;

        UserCoinCategoryProgress progress = progressRepo.findByUserIdAndCategory(userId, category)
                .orElseGet(() -> {
                    UserCoinCategoryProgress p = new UserCoinCategoryProgress();
                    p.setUserId(userId);
                    p.setCategory(category);
                    p.setEarned(0);
                    p.setActions(0);
                    return progressRepo.save(p);
                });
        int remaining = Math.max(0, cap - progress.getEarned());
        if (remaining <= 0) return 0;
        int credit = Math.min(perAction, remaining);

        UserCoins wallet = coinsRepo.findByUserId(userId).orElseGet(() -> {
            UserCoins uc = new UserCoins();
            uc.setUserId(userId);
            uc.setBalance(0);
            return coinsRepo.save(uc);
        });
        wallet.setBalance(wallet.getBalance() + credit);
        coinsRepo.save(wallet);

        UserCoinLedger entry = new UserCoinLedger();
        entry.setUserId(userId);
        entry.setCategory(category);
        entry.setDelta(credit);
        entry.setReason(reason);
        entry.setRelatedId(relatedId);
        ledgerRepo.save(entry);

        progress.setEarned(progress.getEarned() + credit);
        progress.setActions(progress.getActions() + 1);
        progressRepo.save(progress);
        return credit;
    }
}
