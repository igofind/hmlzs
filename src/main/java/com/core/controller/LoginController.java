package com.core.controller;

import com.core.security.SecuritySupport;
import com.core.security.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by sunpeng
 */
@Controller
@RequestMapping("/admin")
public class LoginController extends BaseController {

    private Log logger = LogFactory.getLog(this.getClass());

    /**
     * request: http:// xx.yy.com/aa/admin/
     * redirect the index page.
     *
     * @return
     */
    @RequestMapping(value = "/")
    public String simpleRequest() {

        return getViewRedirect("index");
    }

    /**
     * request: http:// xx.yy.com/aa/admin
     * (is not equals to http:// xx.yy.com/aa/admin/)
     * <p/>
     * redirect the index page.
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String simpleRootRequest() {
        return getViewRedirect("admin/index");
    }

    /**
     * request: http:// xx.yy.com/aa/admin/index
     * the request 'http:// xx.yy.com/aa/admin/' and 'http:// xx.yy.com/aa/admin'
     * will redirect here both. And there should check login status first.
     * <p/>
     * (NR == NeedRight)
     *
     * @return
     */
    @RequestMapping("/index")
    public String indexNR() {

        return getView("index");
    }

    /**
     * open the login page(direct request)
     *
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String showPage() throws Exception {

        SecuritySupport securitySupport = supportFactory.getSecuritySupport();
        UserInfo userInfo = securitySupport.getUserInfo();
        if (userInfo != null && userInfo.getId() > 0) {
            return getViewRedirect("index");
        }
        return getView("login");
    }

    /**
     * deal with the login action
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String login(HttpServletResponse resp) {

        // do something to login
        SecuritySupport support = supportFactory.getSecuritySupport();

        ObjectNode objectNode = support.login(resp);

        objectNode.put("success", true);

        return objectNode.toString();
    }

    /**
     * @param resp
     * @return
     */
    @RequestMapping("/logout")
    public String logoutNR(HttpServletResponse resp) {

        // do something to logout
        SecuritySupport support = supportFactory.getSecuritySupport();
        support.logout(resp);

        return getViewRedirect("login");
    }

}
