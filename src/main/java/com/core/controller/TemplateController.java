package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
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
@RightCheck(depict = "模板管理者")
@Controller
@RequestMapping("/admin")
public class TemplateController extends BaseController {

    @Autowired
    private TemplateService templateService;

    @AsRight(id = 1, depict = "模板页面访问")
    @RequestMapping("/templateList")
    public String showPageNR() {

        return getView("template");
    }

    @AsRight(id = 2, depict = "模板列表数据查询")
    @ResponseBody
    @RequestMapping(value = "/template/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR(int pageSize, int page) {

        return templateService.listAll(pageSize, page).toString();
    }

    @AsRight(id = 3, depict = "模版创建")
    @ResponseBody
    @RequestMapping(value = "/template/create", method = RequestMethod.POST)
    public String createTemplateNR(String name, MultipartHttpServletRequest msr) {

        return templateService.createTemplate(name, msr).toString();
    }

    @AsRight(id = 4, depict = "模板更新(标题)")
    @ResponseBody
    @RequestMapping(value = "/template/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String saveTemplateNR(String data) {

        return templateService.updateTemplateTitle(data).toString();
    }

    @AsRight(id = 5, depict = "模板更新(文件)")
    @ResponseBody
    @RequestMapping(value = "/template/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateTemplateNR(String id, String name, String content) {

        return templateService.updateTemplateFile(id, name, content).toString();
    }

    @AsRight(id = 6, depict = "模板删除")
    @ResponseBody
    @RequestMapping(value = "/template/delete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String deleteTemplateNR(String data) {

        return templateService.deleteTemplate(data).toString();
    }

    @AsRight(id = 7, depict = "模板审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/template/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptTemplateNR(String data) {

        return templateService.updateAcceptTemplate(data).toString();
    }

    @AsRight(id = 8, depict = "模板审核(废弃)")
    @ResponseBody
    @RequestMapping(value = "/template/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectTemplateNR(String data) {

        return templateService.updateRejectTemplate(data).toString();
    }

    @AsRight(id = 9, depict = "模板内容获取")
    @ResponseBody
    @RequestMapping(value = "/template/content", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String getTemplateContentNR(String id) {

        return templateService.getTemplateContent(id);
    }

}
