package com.brotherming.community.controller;

import cn.hutool.core.date.DateUtil;
import com.brotherming.community.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author brotherming
 * @createTime 2022年04月19日 10:50:00
 */
@Controller
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @RequestMapping(path = "/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    public String getStatisticsPage() {
        return "/site/admin/data";
    }

    @PostMapping("/statistics/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        model.addAttribute("uvResult",statisticsService.calculateUV(start,end));
        model.addAttribute("uvStartDate", DateUtil.toLocalDateTime(start));
        model.addAttribute("uvEndDate",DateUtil.toLocalDateTime(end));
        return "forward:/statistics";
    }

    @PostMapping("/statistics/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        model.addAttribute("dauResult",statisticsService.calculateDAU(start,end));
        model.addAttribute("dauStartDate",DateUtil.toLocalDateTime(start));
        model.addAttribute("dauEndDate",DateUtil.toLocalDateTime(end));
        return "forward:/statistics";
    }

}
