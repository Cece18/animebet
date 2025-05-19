package com.crunchybet.betapp.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

//    public void sendResetCode(String to, String code) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Password Reset Code");
//        message.setText("Your password reset code is: " + code + "\nThis code will expire in 15 minutes.");
//        mailSender.send(message);
//    }
public void sendResetCode(String to, String code) {
    try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");

        String body = "Your password reset code is: " + code + "This code will expire in 15 minutes.";

        helper.setTo(to);
        helper.setSubject("Password Reset Code");

        // This line is the key:
        helper.setFrom("charlenejadyn@gmail.com", "AnimeBets");

        helper.setText(body, false);

        mailSender.send(mimeMessage);
    } catch (MessagingException e) {
        // Log the error or throw custom exception
        e.printStackTrace();
    } catch (jakarta.mail.MessagingException e) {
        throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
}
}
