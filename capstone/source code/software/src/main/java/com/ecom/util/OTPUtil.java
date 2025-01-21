package com.ecom.util;
import java.util.Random;

public class OTPUtil {
    private static final int OTP_LENGTH = 6;

    public static String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));  // Generates a digit between 0-9
        }

        return otp.toString();
    }
}
