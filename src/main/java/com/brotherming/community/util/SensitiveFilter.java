package com.brotherming.community.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ){
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败:" + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode temp = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode trieNode = temp.subNodes.get(c);
            if (ObjectUtil.isEmpty(trieNode)) {
                trieNode = new TrieNode();
                temp.subNodes.put(c,trieNode);
            }
            temp = trieNode;
            if (i == keyword.length() - 1) {
                temp.isKeywordEnd = true;
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤
     * @return 过滤之后
     */
    public String filter(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        //指针1
        TrieNode temp = root;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            if (isSymbol(c)) {
                if (temp == root) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            temp = temp.subNodes.get(c);
            if (temp == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                temp = root;
            }else if (temp.isKeywordEnd){
                sb.append(REPLACEMENT);
                begin = ++position;
                temp = root;
            }else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private static class TrieNode {
        boolean isKeywordEnd;
        Map<Character,TrieNode> subNodes = new HashMap<>();
    }

}
