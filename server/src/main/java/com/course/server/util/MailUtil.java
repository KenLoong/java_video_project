package com.course.server.util;

import com.course.server.domain.User;
import com.course.server.dto.SmsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MailUtil.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private String serverId = "localhost";

    /**
     *
     * @param to：邮件接受者
     * @param subject：邮件主题
     * @param content:邮件内容
     */
    private void sendMail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        System.out.println(from);
        helper.setFrom(from);
        helper.setTo(to);
        //设置主题
        helper.setSubject(subject);
        helper.setText(content, true);
        LOG.info("发送邮件内容："+content);
        mailSender.send(helper.getMimeMessage());
    }


    public void send(SmsDto smsDto) throws MessagingException {
        sendMail(smsDto.getMobile(),"邮件验证","您的验证码为："+smsDto.getCode());
    }

}
