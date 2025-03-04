package com.example.signupflow.service;

import com.example.signupflow.entity.OtpRecord;
import com.example.signupflow.repo.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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

    public String generateOtp(String email, String mobile) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isPresent()) {
            OtpRecord record = existingRecord.get();
            if (record.getBlockTime() != null && record.getBlockTime().isAfter(LocalDateTime.now())) {
                return "User is blocked. Try after " + BLOCK_TIME_MINUTES + " minutes.";
            }
            otpRepository.delete(record);
        }

        OtpRecord newRecord = new OtpRecord();
        newRecord.setEmail(email);
        newRecord.setMobileNumber(mobile);
        newRecord.setOtpCode(generateOtp());
        newRecord.setCreatedAt(LocalDateTime.now());
        newRecord.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));

        otpRepository.save(newRecord);
        return "OTP sent successfully.";
    }

    public String verifyOtp(String email, String inputOtp) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isEmpty()) {
            return "OTP expired or not found.";
        }

        OtpRecord record = existingRecord.get();

        if (record.getBlockTime() != null && record.getBlockTime().isAfter(LocalDateTime.now())) {
            return "User is blocked. Try after " + BLOCK_TIME_MINUTES + " minutes.";
        }

        if (record.getExpiredAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(record);
            return "OTP expired.";
        }

        if (record.getOtpCode().equals(inputOtp)) {
            otpRepository.delete(record);
            return "OTP verified successfully.";
        } else {
            record.setRetryAttempt(record.getRetryAttempt() + 1);

            if (record.getRetryAttempt() >= MAX_ATTEMPTS) {
                record.setBlockTime(LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
                otpRepository.save(record);
                return "Too many failed attempts. User blocked for " + BLOCK_TIME_MINUTES + " minutes.";
            }

            otpRepository.save(record);
            return "Invalid OTP. Attempts left: " + (MAX_ATTEMPTS - record.getRetryAttempt());
        }
    }

    public String resendOtp(String email, String mobile) {
        Optional<OtpRecord> existingRecord = otpRepository.findByEmail(email);

        if (existingRecord.isPresent()) {
            OtpRecord record = existingRecord.get();

            if (record.getBlockTime() != null && record.getBlockTime().isAfter(LocalDateTime.now())) {
                return "User is blocked. Try after " + BLOCK_TIME_MINUTES + " minutes.";
            }

            if (record.getResendAttempt() >= MAX_RESEND_ATTEMPTS) {
                record.setBlockTime(LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
                otpRepository.save(record);
                return "Maximum resend attempts reached. User blocked for " + BLOCK_TIME_MINUTES + " minutes.";
            }

            record.setOtpCode(generateOtp());
            record.setCreatedAt(LocalDateTime.now());
            record.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
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
}
