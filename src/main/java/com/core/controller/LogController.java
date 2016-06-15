package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.LogService;
import com.core.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Created by sunpeng
 * <p/>
 * NR == NeedRight
 */
@RightCheck(depict = "日志管理者")
@Controller
@RequestMapping("/admin")
public class LogController extends BaseController {

    @Autowired
    private LogService logService;

    @AsRight(id = 1, depict = "日志列表页面访问")
    @RequestMapping("/logList")
    public String showPageNR() {

        return getView("log");
    }

    @AsRight(id = 2, depict = "日志列表数据查询")
    @ResponseBody
    @RequestMapping(value = "/log/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR(int pageSize, int page) {

        return logService.listAll(pageSize, page).toString();
    }

}
