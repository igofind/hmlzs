package com.core.controller;

import com.core.domain.Category;
import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "分类(栏目)管理者")
@Controller
@RequestMapping("/admin")
public class CategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @AsRight(id = 1, depict = "分类页面访问")
    @RequestMapping("/categoryList")
    public String showPageNR() {

        return getView("category");
    }

    @ResponseBody
    @RequestMapping(value = "/category/list", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String list(String parentId, int pageSize, int page) {

        return categoryService.listAccept(pageSize, page, parentId).toString();
    }

    @AsRight(id = 3, depict = "分类列表数据查询")
    @ResponseBody
    @RequestMapping(value = "/category/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR(String parentId, int pageSize, int page) {

        return categoryService.listAll(pageSize, page, parentId).toString();
    }

    @AsRight(id = 4, depict = "分类创建")
    @ResponseBody
    @RequestMapping(value = "/category/create", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String categoryCreateNR(Category category) {

        return categoryService.createCategory(category).toString();
    }

    @AsRight(id = 5, depict = "分类更新")
    @ResponseBody
    @RequestMapping(value = "/category/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String categoryUpdateNR(String data) throws Exception {

        return categoryService.updateCategory(data).toString();
    }

    @AsRight(id = 6, depict = "分类删除")
    @ResponseBody
    @RequestMapping(value = "/category/delete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String categoryDeleteNR(String data) throws Exception {

        return categoryService.deleteCategory(data).toString();
    }

    @AsRight(id = 7, depict = "分类审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/category/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String categoryAcceptNR(String data) throws Exception {

        return categoryService.updateAcceptCategory(data).toString();

    }

    @AsRight(id = 8, depict = "分类审核(废弃)")
    @ResponseBody
    @RequestMapping(value = "/category/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String categoryRejectNR(String data) throws Exception {

        return categoryService.updateRejectCategory(data).toString();
    }

}
