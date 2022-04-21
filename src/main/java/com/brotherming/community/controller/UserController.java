package com.brotherming.community.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brotherming.community.annotation.LoginRequired;
import com.brotherming.community.entity.Comment;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.*;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private CommentService commentService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        //上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0,null));
        //生成上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    //更新头像路径
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1,"文件名不能为空!");
        }
        String url = headerBucketUrl + "/" + fileName;
        User user = hostHolder.getUser();
        user.setHeaderUrl(url);
        userService.updateById(user);
        userService.clearCache(user.getId());
        return CommunityUtil.getJSONString(0,null);
    }

    //废弃
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (ObjectUtil.isEmpty(headerImage)) {
            model.addAttribute("error","您还没有选择图片!");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StrUtil.isBlank(suffix)) {
            model.addAttribute("error","文件的格式不正确!");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常!",e);
        }

        //更新当前用户的头像的路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        user.setHeaderUrl(headerUrl);
        userService.updateById(user);
        userService.clearCache(user.getId());
        return "redirect:/index";
    }

    //废弃
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try (
            BufferedInputStream inputStream = FileUtil.getInputStream(fileName)
        ){
            IoUtil.copy(inputStream,response.getOutputStream());
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }
    }

    @PostMapping("/updatepassword")
    public String updatePassword(@CookieValue("ticket") String ticket, Model model,
                                 String oldpassword, String newpassword, String confirmpassword){
        Map<String, Object> map = userService.updatePassword(oldpassword,newpassword,
                confirmpassword,hostHolder.getUser());
        if (CollUtil.isEmpty(map)) {
            userService.logout(ticket);
            return "redirect:/login";
        }else {
            model.addAttribute("oldpassword",map.get("old"));
            model.addAttribute("newpassword",map.get("new"));
            model.addAttribute("confirmpassword",map.get("confirm"));
            return "/site/setting";
        }
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (ObjectUtil.isEmpty(user)) {
            throw new RuntimeException("该用户不存在!");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        //是否关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

    @GetMapping("/mypost/{userId}")
    public String getMyPost(Model model, PageInfo pageInfo,@PathVariable("userId") int userId) {
        Page<DiscussPost> page = new Page<>(pageInfo.getCurrent(),pageInfo.getLimit());
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussPost::getUserId,userId).ne(DiscussPost::getStatus,2).orderByDesc(DiscussPost::getCreateTime);
        Page<DiscussPost> discussPostPage = discussPostService.page(page, wrapper);
        List<Map<String,Object>> list = new ArrayList<>();
        Optional.ofNullable(discussPostPage.getRecords()).ifPresent(discussPostList -> {
            for (DiscussPost post : discussPostList) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                list.add(map);
            }
        });
        long total = page.getTotal();
        pageInfo.setPath("/user/mypost/" + userId);
        pageInfo.setRows((int) total);
        model.addAttribute("postList",list);
        model.addAttribute("total",total);
        model.addAttribute("user",userService.getById(userId));
        return "/site/my-post";
    }

    @GetMapping("/myreply/{userId}")
    public String getMyReply(Model model, PageInfo pageInfo, @PathVariable("userId") int userId) {
        int count = commentService.findCommentCountByUserId(userId);
        pageInfo.setPath("/user/myreply/" + userId);
        pageInfo.setRows(count);
        List<Comment> commentList = commentService.findNewCommentByUserId(pageInfo.getOffset(),pageInfo.getLimit(),userId);
        List<Map<String,Object>> comments = new ArrayList<>();
        commentList.forEach(comment -> {
            Map<String,Object> map = new HashMap<>();
            map.put("comment",comment);
            DiscussPost post = discussPostService.getById(comment.getEntityId());
            map.put("post",post);
            comments.add(map);
        });
        model.addAttribute("comments",comments);
        model.addAttribute("count",count);
        model.addAttribute("user",userService.getById(userId));
        return "/site/my-reply";
    }
}

