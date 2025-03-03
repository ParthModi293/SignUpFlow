package com.example.signupflow.service;

import com.example.signupflow.entity.OtpHistory;
import com.example.signupflow.repo.OTPHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OTPService {

    @Autowired
    private OTPHistoryRepository otpHistoryRepository;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    public String generateOTP(String email, String mobileNumber) {
        String otpCode = String.format("%04d", new Random().nextInt(10000)); // Generate 4-digit OTP

        OtpHistory otp = otpHistoryRepository.findByEmail(email)
                .orElse(new OtpHistory());

        otp.setEmail(email);
        otp.setMobileNumber(mobileNumber);
        otp.setOtpCode(otpCode);
        otp.setAttempts(0);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        otpHistoryRepository.save(otp);
        return otpCode;
    }

    public boolean verifyOTP(String email, String otpCode) {
        OtpHistory otp = otpHistoryRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found!"));

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            otpHistoryRepository.delete(otp);
            throw new RuntimeException("OTP expired!");
        }

        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new RuntimeException("Maximum attempts exceeded!");
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpHistoryRepository.save(otp);
            throw new RuntimeException("Incorrect OTP!");
        }

        otpHistoryRepository.delete(otp); // OTP verified, remove entry
        return true;
    }
}
