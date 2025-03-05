package com.example.signupflow.service;

import com.example.signupflow.entity.OtpRecord;
import com.example.signupflow.repo.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class OTPService {

    @Autowired
    private OtpRepository otpRepository;
    private static final SecureRandom random = new SecureRandom();


    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final int BLOCK_TIME_MINUTES = 10;
    private static final int RESEND_COOLDOWN_MINUTES = 1;

    public String generateOtp(String email, String mobile) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isPresent()) {
            OtpRecord record = existingRecord.get();
            if (record.getBlockTime() != null && record.getBlockTime().isAfter(Instant.now())) {
                return "User is blocked. Try after " + formatInstant(record.getBlockTime()) + " minutes.";
            }
            otpRepository.delete(record);
        }

        OtpRecord newRecord = new OtpRecord();
        newRecord.setEmail(email);
        newRecord.setMobileNumber(mobile);
        newRecord.setOtpCode(generateOtp());
        newRecord.setCreatedAt(Instant.now());
        newRecord.setExpiredAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES)));

        otpRepository.save(newRecord);
        return "OTP sent successfully.";
    }

    public String verifyOtp(String email, String inputOtp) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isEmpty()) {
            return "OTP expired or not found.";
        }

        OtpRecord record = existingRecord.get();

        if (record.getBlockTime() != null && record.getBlockTime().isAfter(Instant.now())) {
            return "User is blocked. Try after " + formatInstant(record.getBlockTime()) + " minutes.";
        }

        if (record.getExpiredAt().isBefore(Instant.now())) {
            otpRepository.delete(record);
            return "OTP expired.";
        }

        if (record.getOtpCode().equals(inputOtp)) {
            otpRepository.delete(record);
            return "OTP verified successfully.";
        } else {
            record.setRetryAttempt(record.getRetryAttempt() + 1);

            if (record.getRetryAttempt() >= MAX_ATTEMPTS) {
                Instant blockEndTime = Instant.now().plus(Duration.ofMinutes(BLOCK_TIME_MINUTES));
                record.setBlockTime(blockEndTime);
                otpRepository.save(record);
                return "Too many failed attempts. User blocked until " + formatInstant(blockEndTime) + ".";
            }

            otpRepository.save(record);
            return "Invalid OTP. Attempts left: " + (MAX_ATTEMPTS - record.getRetryAttempt());
        }
    }

    public String resendOtp(String email, String mobile) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isPresent()) {
            OtpRecord record = existingRecord.get();

            if (record.getBlockTime() != null && record.getBlockTime().isAfter(Instant.now())) {
                    return "User is blocked. Try after " + formatInstant(record.getBlockTime()) ;
            }

            if (record.getCreatedAt().plus(Duration.ofMinutes(RESEND_COOLDOWN_MINUTES)).isAfter(Instant.now())) {
                Instant availableTime = record.getCreatedAt().plus(Duration.ofMinutes(RESEND_COOLDOWN_MINUTES));
                return "You can resend OTP after " + formatInstant(availableTime) + ".";
            }

            if (record.getResendAttempt() >= MAX_RESEND_ATTEMPTS) {
                Instant blockEndTime = Instant.now().plus(Duration.ofMinutes(BLOCK_TIME_MINUTES));
                record.setBlockTime(blockEndTime);
                otpRepository.save(record);
                return "Maximum resend attempts reached. User blocked until " + formatInstant(blockEndTime) ;
            }

            record.setOtpCode(generateOtp());
            record.setCreatedAt(Instant.now());
            record.setExpiredAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES)));
            record.setRetryAttempt(0);
            record.setBlockTime(null);
            record.setResendAttempt(record.getResendAttempt() + 1);

            otpRepository.save(record);
            return "OTP resent successfully.";
        }

        return generateOtp(email, mobile);
    }

    public static String generateOtp() {
        int otp = 1000 + random.nextInt(9000); // 4-digit OTP
        return String.valueOf(otp);
    }

    private String formatInstant(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }
}
