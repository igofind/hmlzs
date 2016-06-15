package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.RightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "权限管理者")
@RequestMapping("/admin")
@Controller
public class RightController extends BaseController {

    @Autowired
    private RightService rightService;

    @AsRight(id = 1, depict = "权限页面访问")
    @RequestMapping("/rightList")
    public String showPageNR() {

        return getView("right");
    }

    @AsRight(id = 2, depict = "权限列表数据")
    @ResponseBody
    @RequestMapping(value = "/right/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR(int pageSize, int page) {

        return rightService.listAll(pageSize, page).toString();
    }

    @AsRight(id = 4, depict = "权限更新")
    @ResponseBody
    @RequestMapping(value = "/right/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateRightNR(String data) {

        return rightService.update(data).toString();
    }

    @AsRight(id = 5, depict = "权限启用")
    @ResponseBody
    @RequestMapping(value = "/right/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptRightNR(String data) {

        return rightService.updateAccept(data).toString();
    }

    @AsRight(id = 6, depict = "权限废弃")
    @ResponseBody
    @RequestMapping(value = "/right/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectRightNR(String data) {

        return rightService.updateReject(data).toString();
    }

    @RequestMapping(value = "/noRight", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    public String noRight(){
        return getViewRedirect("noRight");
    }

}
