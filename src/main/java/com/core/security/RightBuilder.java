package com.core.security;

import com.core.repository.SecurityRepository;
import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.security.domain.Right;
import com.core.security.domain.Role;
import com.core.security.domain.RoleRight;
import com.core.util.Constant;
import com.core.util.EncryptUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;

/**
 * Created by sunpeng
 */
public class RightBuilder {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private SecurityRepository securityRepository;

    @Transactional
    @PostConstruct
    public void init() {

        try {
            logger.debug("---------------------- RightBuilder init start --------------------");

            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
            Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents("com.core.controller");

            for (BeanDefinition beanDefinition : beanDefinitions) {

                Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());

                if (aClass.isAnnotationPresent(RightCheck.class)) {

                    buildRole(aClass);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    /**
     * @param clazz
     * @return
     */
    @Transactional
    public boolean buildRole(Class<?> clazz) throws Exception {

        Role role = null;

        if (clazz.isAnnotationPresent(RightCheck.class)) {

            role = securityRepository.findRole(clazz);

            if (role == null) {

                role = new Role();

                RightCheck rightCheck = clazz.getAnnotation(RightCheck.class);
                role.setMd5(EncryptUtil.md5(clazz.getName()));
                role.setDepict(rightCheck.depict());
                role.setName(StringUtils.isBlank(rightCheck.rightGroupName()) ? clazz.getSimpleName() : rightCheck.rightGroupName());
                role.setStatus(Constant.COMMON_STATUS_ACCEPT);
                role.setCreateDate(new Date());

                role.setId(securityRepository.create(role));
            }

        } else {
            return false;
        }

        Method[] declaredMethods = clazz.getDeclaredMethods();

        if (declaredMethods.length > 0) {
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.isAnnotationPresent(AsRight.class)) {

                    Right right = securityRepository.findRight(declaredMethod);

                    if (right == null) {

                        logger.debug("[" + declaredMethod.toString() + "] as a right.");

                        right = new Right();

                        AsRight asRight = declaredMethod.getAnnotation(AsRight.class);

                        right.setDepict(asRight.depict());

                        right.setName(declaredMethod.getName());
                        right.setMd5(buildRightKey(declaredMethod));
                        right.setStatus(Constant.COMMON_STATUS_ACCEPT);
                        right.setCreateDate(new Date());

                        right.setId(securityRepository.create(right));

                        RoleRight rtr = new RoleRight();
                        rtr.setCreateDate(new Date());
                        rtr.setRightId(right.getId());
                        rtr.setRoleId(role.getId());

                        securityRepository.create(rtr);
                    } else {
                        logger.debug("right exit and md5 = " + right.getMd5());
                        RoleRight rr = securityRepository.findRR(role.getId(), right.getId());
                        if (rr == null) {

                            RoleRight rtr = new RoleRight();
                            rtr.setCreateDate(new Date());
                            rtr.setRightId(right.getId());
                            rtr.setRoleId(role.getId());

                            securityRepository.create(rtr);
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * @param clazz
     * @return
     */
    public final static String buildRoleKey(Class clazz) {

        return EncryptUtil.md5(clazz.getName());
    }

    /**
     * @param method
     * @return
     */
    public final static String buildRightKey(Method method) {

        AsRight asRight = method.getAnnotation(AsRight.class);

        return EncryptUtil.md5(method.toGenericString() + asRight.id());
    }

}
