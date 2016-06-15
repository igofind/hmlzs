package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.HomepageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "首页内容管理者")
@Controller
@RequestMapping("/admin")
public class HomepageController extends BaseController {

    @Autowired
    private HomepageService homepageService;

    @Autowired
    private HttpServletRequest request;

    @AsRight(id = 1, depict = "首页信息更新页面")
    @RequestMapping(value = "/navbanner", method = RequestMethod.GET)
    public String showPageNR() {

        request.setAttribute("data", homepageService.getHomePageData());
        return getView("homepage");
    }

    @AsRight(id = 2, depict = "首页导航信息更新")
    @ResponseBody
    @RequestMapping(value = "/homepage/updateNav", method = RequestMethod.POST)
    public String updateNavNR(String navMain, String navSecond) {

        return homepageService.updateNav(navMain, navSecond).toString();
    }

    @AsRight(id = 3, depict = "首页banner信息更新")
    @ResponseBody
    @RequestMapping(value = "/homepage/updateBanner", method = RequestMethod.POST)
    public String updateBannerNR(String bannerStr) {

        return homepageService.updateBanner(bannerStr).toString();
    }

    @AsRight(id = 4, depict = "首页静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticize", method = RequestMethod.POST)
    public String staticizeNR() {

        return homepageService.staticizeAll().toString();
    }

    @AsRight(id = 6, depict = "首页广种福田更新")
    @ResponseBody
    @RequestMapping(value = "/homepage/updateFutian", method = RequestMethod.POST)
    public String updateFutianNR(String content, String usage, String bank, String account, String number) {

        return homepageService.updateFutian(content, usage, bank, account, number).toString();
    }

    @AsRight(id = 7, depict = "首页导航静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticizeNav", method = RequestMethod.POST)
    public String staticizeNavNR() {

        return homepageService.staticizeNav().toString();
    }

    @AsRight(id = 8, depict = "首页banner静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticizeBanner", method = RequestMethod.POST)
    public String staticizeBannerNR() {

        return homepageService.staticizeBanner().toString();
    }

    @AsRight(id = 9, depict = "首页广种福田联、联系我们静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticizeFutian", method = RequestMethod.POST)
    public String staticizeFutianNR() {

        return homepageService.staticizeFutian().toString();
    }

    @AsRight(id = 10, depict = "首页紫云法务静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticizeFawu", method = RequestMethod.POST)
    public String staticizeFawuNR() {

        return homepageService.staticizeFawu().toString();
    }

    @AsRight(id = 11, depict = "首页活动通知静态化")
    @ResponseBody
    @RequestMapping(value = "/homepage/staticizeNotice", method = RequestMethod.POST)
    public String staticizeNoticeNR() {

        return homepageService.staticizeNotice().toString();
    }

}
