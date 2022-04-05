package com.brotherming.community;

import com.brotherming.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SensitiveFilterTest {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Test
    void test() {
        String filter = sensitiveFilter.filter("这里可以♥赌♥博♥，可以♥嫖♥娼♥，可以♥嫖♥娼♥、可以♥开♥票♥哈哈哈、♥操♥你♥大♥爷♥艹♥");
        System.out.println(filter);
    }
}
