/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

// Third-party imports
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.SelfConfigurableService;
import ome.system.ServiceFactory;
import ome.tools.spring.AOPAdapter;
import ome.tools.spring.InternalServiceFactory;

/**
 * Abstract base class for all OMERO services. Responsible
 * for login, logout, and various forms of interception.
 * 
 * @DEV.TODO This class will be moved to a interceptor!
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public abstract class AbstractBean implements SelfConfigurableService {

    /** Interceptors that are determinined at compile time by server/build.xml
     *  The REPLACE token will be replaced with a (possibly) empty comma-
     *  comma separated list of strings representing the class names of 
     *  HardWiredInterceptor subclasses which are prepended to the list of
     *  interceptors for each call. Note: these interceptors will NOT be applied
     *  to server internal calls.
     */
    private final static List<HardWiredInterceptor> CPTORS = HardWiredInterceptor
            .parse(new String[] { /* @REPLACE@ */});

    private transient Log logger = LogFactory.getLog(this.getClass());

    private transient OmeroContext applicationContext;

    private transient ServiceFactory serviceFactory;

    private transient SecuritySystem securitySystem;

    private transient QueryFactory queryFactory;

    private @Resource
    SessionContext sessionContext;

    // ~ Lifecycle implementations
    // =========================================================================

    public void create() {
        selfConfigure();
        logger.debug("Created:\n" + getLogString());
    }

    public void destroy() {
        applicationContext = null;
        securitySystem = null;
        serviceFactory = null;
        logger.debug("Destroying:\n" + getLogString());
    }

    /**
     * Responsible for using the {@link Principal} in the {@link SessionContext}
     * and for wrapping all calls to Omero services with the method interceptors
     * defined in Spring. No other logic should be added here, otherwise
     * server-side internal calls will no longer work. (They don't use client
     * {@link Principal principals} to login and are already wrapped when
     * acquired from the application context.
     */
    @AroundInvoke
    protected final Object loginAndSpringWrap(InvocationContext context)
            throws Exception {
        try {
            login();
            return call(context);
        } catch (Throwable t) {
            throw translateException(t);
        } finally {
            logout();
        }

    }

    private void login() {
        Principal p;
        if (sessionContext.getCallerPrincipal() instanceof Principal) {
            p = (Principal) sessionContext.getCallerPrincipal();
            securitySystem.login(p);
            if (logger.isDebugEnabled()) {
                logger.debug("Running with user: " + p.getName());
            }
        } else {
            throw new ApiUsageException(
                    "ome.system.Principal instance must be provided on login.");
        }

    }

    private void logout() {
        securitySystem.logout();
    }

    private Object call(InvocationContext context) throws Throwable {

        String factoryName = "&managed:" + getServiceInterface().getName();
        AOPAdapter adapter = AOPAdapter.create(
                (ProxyFactoryBean) applicationContext.getBean(factoryName),
                context, CPTORS);

        Object o = sessionContext.getCallerPrincipal();
        if (!(o instanceof ome.system.Principal)) {
            throw new ApiUsageException("Callers must provide an instance "
                    + "of ome.system.Principal for login.");
        }

        HardWiredInterceptor.initializeUserAttributes(adapter,
                getServiceFactory(), (Principal) sessionContext
                        .getCallerPrincipal());

        return adapter.proceed();
    }

    // ~ Self-configuration (non-JavaEE)
    // =========================================================================

    protected abstract Class<? extends ServiceInterface> getServiceInterface();

    public final void acquireContext() {
        if (this.applicationContext == null) {
            this.applicationContext = OmeroContext.getManagedServerContext();
        }
        serviceFactory = new InternalServiceFactory(applicationContext);
    }

    public final void selfConfigure() {
        this.acquireContext();
        // This will, in turn, call throwIfAlreadySet
        this.applicationContext.applyBeanPropertyValues(this,
                getServiceInterface());
    }

    public final void setApplicationContext(ApplicationContext appCtx)
            throws BeansException {
        throwIfAlreadySet(this.applicationContext, appCtx);
        this.applicationContext = (OmeroContext) appCtx;
    }

    public final void setQueryFactory(QueryFactory factory) {
        throwIfAlreadySet(this.queryFactory, factory);
        this.queryFactory = factory;
    }

    public final void setSecuritySystem(SecuritySystem security) {
        throwIfAlreadySet(this.securitySystem, security);
        this.securitySystem = security;
    }

    // ~ Getters
    // =========================================================================

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public SecuritySystem getSecuritySystem() {
        return securitySystem;
    }

    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    public Log getLogger() {
        return logger;
    }

    // ~ Helpers
    // =========================================================================

    protected void throwIfAlreadySet(Object current, Object injected) {
        if (current != null) {
            throw new InternalException(String.format("%s already configured "
                    + "with %s cannot set inject %s.", this.getClass()
                    .getName(), current, injected));
        }
    }

    protected void passivationNotAllowed() {
        throw new InternalException(
                String
                        .format(
                                "Passivation should have been disabled for this Stateful Session Beans (%s).\n"
                                        + "Please contact the Omero development team for how to ensure that passivation\n"
                                        + "is disabled on your application server.",
                                this.getClass().getName()));
    }

    protected Exception translateException(Throwable t) {
        if (Exception.class.isAssignableFrom(t.getClass())) {
            return (Exception) t;
        } else {
            InternalException ie = new InternalException(t.getMessage());
            ie.setStackTrace(t.getStackTrace());
            return ie;
        }
    }

    protected String getLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean ");
        sb.append(this);
        sb.append("\n with Context ");
        sb.append(applicationContext);
        return sb.toString();
    }

}
