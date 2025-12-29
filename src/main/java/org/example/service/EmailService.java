package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    // Đưa vào database.properties hoặc env cũng được
    private final String smtpUser = "18longnguyen@gmail.com";
    private final String smtpAppPassword = "moskpvbxqwhjxluy"; // 16 ký tự

    public void sendOtp(String toEmail, String otp) {
        if (toEmail == null || toEmail.isBlank())
            throw new RuntimeException("Email người nhận trống.");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpAppPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(smtpUser));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("[QLSV] Ma OTP doi mat khau");
            msg.setText("Ma OTP cua ban la: " + otp + "\nHieu luc trong 5 phut.");
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
}
