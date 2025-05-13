package com.threadqa.lms.service.auth;

import com.threadqa.lms.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(User user) {
        try {
            // Генерация токена подтверждения
            String token = UUID.randomUUID().toString();
            
            // Здесь должна быть логика сохранения токена в базе данных или другом хранилище
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Подтверждение регистрации в ThreadQA LMS");
            message.setText(
                "Здравствуйте, " + user.getFirstName() + " " + user.getLastName() + "!\n\n" +
                "Благодарим вас за регистрацию в ThreadQA LMS.\n\n" +
                "Для подтверждения вашего email адреса, пожалуйста, перейдите по следующей ссылке:\n" +
                "https://lms.threadqa.com/verify-email?token=" + token + "\n\n" +
                "Если вы не регистрировались в нашей системе, просто проигнорируйте это письмо.\n\n" +
                "С уважением,\n" +
                "Команда ThreadQA"
            );
            
            mailSender.send(message);
            
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Сброс пароля в ThreadQA LMS");
            message.setText(
                "Здравствуйте, " + user.getFirstName() + " " + user.getLastName() + "!\n\n" +
                "Мы получили запрос на сброс пароля для вашей учетной записи в ThreadQA LMS.\n\n" +
                "Для сброса пароля, пожалуйста, перейдите по следующей ссылке:\n" +
                "https://lms.threadqa.com/reset-password?token=" + token + "\n\n" +
                "Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.\n\n" +
                "С уважением,\n" +
                "Команда ThreadQA"
            );
            
            mailSender.send(message);
            
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Async
    public void sendCourseEnrollmentConfirmation(User user, String courseName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Подтверждение записи на курс в ThreadQA LMS");
            message.setText(
                "Здравствуйте, " + user.getFirstName() + " " + user.getLastName() + "!\n\n" +
                "Поздравляем! Вы успешно записались на курс \"" + courseName + "\" в ThreadQA LMS.\n\n" +
                "Вы можете начать обучение прямо сейчас, перейдя в свой личный кабинет:\n" +
                "https://lms.threadqa.com/dashboard\n\n" +
                "Если у вас возникнут вопросы, не стесняйтесь обращаться к нам.\n\n" +
                "С уважением,\n" +
                "Команда ThreadQA"
            );
            
            mailSender.send(message);
            
            log.info("Course enrollment confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send course enrollment confirmation email to: {}", user.getEmail(), e);
        }
    }
}
