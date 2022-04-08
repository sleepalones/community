package com.brotherming.community;

import com.brotherming.community.dao.MessageMapper;
import com.brotherming.community.entity.Message;
import com.brotherming.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class MailClientTest {

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private MessageMapper messageMapper;

    @Test
    public void testTextMail() {
        mailClient.sendMail("2720034026@qq.com","Test","Welcome wym");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username","sunday");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("2720034026@qq.com","HTML",content);
    }

    @Test
    public void test(){

        System.out.println(messageMapper.selectConversationCount(111));
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

    }
}
