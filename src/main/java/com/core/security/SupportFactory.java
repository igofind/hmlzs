package com.core.security;

import com.core.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by sunpeng
 */
@Component
public abstract class SupportFactory {

    public abstract SecuritySupport getSecuritySupport();

}
