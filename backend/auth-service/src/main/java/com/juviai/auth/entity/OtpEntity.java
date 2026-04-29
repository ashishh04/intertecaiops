package com.juviai.auth.entity;

import com.juviai.common.crypto.EncryptedStringConverter;
import com.juviai.common.crypto.SearchableHashConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "otp_verification",
        indexes = {
                @Index(name = "idx_otp_user", columnList = "user_id"),
                @Index(name = "idx_otp_expiry", columnList = "expiry_time")
        }
)
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = true)
    private UUID userId;

    @Column(name = "otp_hash", nullable = false, length = 100)
    private String otpHash;

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    /**
     * Phone number for OTP flows initiated via primaryContact (phone-based OTP login).
     * Nullable — only populated when OTP is associated with a phone number,
     * not a pre-existing userId (e.g. during phone-OTP login auto-registration).
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone_number", length = 512)
    private String phoneNumber;

    /**
     * Searchable hash (HMAC blind index) of the phone number for secure lookups
     * without decryption. Used for finding OTP records by phone number.
     */
    @Convert(converter = SearchableHashConverter.class)
    @Column(name = "phone_hash", length = 64)
    private String phoneHash;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getResendCount() {
        return resendCount;
    }

    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }
}
