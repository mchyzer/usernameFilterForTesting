/**
 * @author mchyzer
 * $Id$
 */
package org.hyzer.usernameFilterForTesting;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * <pre>
 * 
 * j2ee filter to set a backdoor username with a password.  Setup in web.xml
 * 
 * <filter>
 *   <filter-name>usernameFilterForTesting</filter-name>
 *   <filter-class>org.hyzer.usernameFilterForTesting.UsernameFilterForTesting</filter-class>
 * </filter>
 * <filter-mapping>
 *   <filter-name>usernameFilterForTesting</filter-name>
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * 
 * <context-param>
 *  <param-name>usernameFilterForTestingEnabled</param-name>
 *  <param-value>true</param-value>
 * </context-param>
 * <context-param>
 *  <param-name>usernameFilterForTestingPassword</param-name>
 *  <param-value>abc123XYZ789</param-value>
 * </context-param>
 * 
 * </pre>
 */
public class UsernameFilterForTesting implements Filter {

  /**
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    ServletRequest servletRequestOrWrapper = servletRequest;
    
    if (enabled) {
    
      String usernameFilterForTestingPassword = servletRequest.getParameter("usernameFilterForTestingPassword");
      
      String usernameFilterForTestingUsername = null;
      
      //start with session (default)
      HttpServletRequest httpServletRequest = null;
      if (servletRequest instanceof HttpServletRequest) {
        httpServletRequest = (HttpServletRequest)servletRequest;
        usernameFilterForTestingUsername = (String)httpServletRequest.getSession().getAttribute("usernameFilterForTestingUsername");
      }
      
      //if its in the URL, that supercedes...
      if (usernameFilterForTestingPassword != null && password != null && password.equals(usernameFilterForTestingPassword)) {
        
        String tempUsername = servletRequest.getParameter("usernameFilterForTestingUsername");
        
        if (tempUsername != null && !"".equals(usernameFilterForTestingUsername.trim())) {

          usernameFilterForTestingUsername = tempUsername;

          //store in session
          if (httpServletRequest != null) {
            httpServletRequest.getSession().setAttribute("usernameFilterForTestingUsername", usernameFilterForTestingUsername);
          }
        }
      }
      
      if (usernameFilterForTestingUsername != null && !"".equals(usernameFilterForTestingUsername.trim())) {
        
        //set this
        servletRequest.setAttribute("REMOTE_USER", usernameFilterForTestingUsername);
        
        //store in session
        if (httpServletRequest != null) {
        
          final String USERNAME_FILTER_FOR_TESTING_USERNAME = usernameFilterForTestingUsername;
          
          servletRequestOrWrapper = new HttpServletRequestWrapper(httpServletRequest) {

            /**
             * @see javax.servlet.http.HttpServletRequestWrapper#getUserPrincipal()
             */
            @Override
            public Principal getUserPrincipal() {
              return new Principal() {
                
                public String getName() {
                  return USERNAME_FILTER_FOR_TESTING_USERNAME;
                }
              };
            }
            
          };
        }
      }      
      
    }
    
    filterChain.doFilter(servletRequestOrWrapper, servletResponse);
    
  }

  /**
   * if enabled
   */
  private static boolean enabled = false;
  
  /**
   * password
   */
  private static String password = null;
  
  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    
    String usernameFilterForTestingPassword = filterConfig.getServletContext().getInitParameter("usernameFilterForTestingPassword");
    
    if (usernameFilterForTestingPassword != null && !"".equals(usernameFilterForTestingPassword.trim())) {
      
      if ("abc123XYZ789".equals(usernameFilterForTestingPassword)) {
        throw new RuntimeException("Dont use the sample password!");
      }
      
      password = usernameFilterForTestingPassword;
      
    }
    
    String usernameFilterForTestingEnabled = filterConfig.getServletContext().getInitParameter("usernameFilterForTestingEnabled");

    //in the web.xml set the params
    //<context-param>
    //  <param-name>usernameFilterForTestingEnabled</param-name>
    //  <param-value>true</param-value>
    //</context-param>
    //<context-param>
    //  <param-name>usernameFilterForTestingPassword</param-name>
    //  <param-value>abc123XYZ789</param-value>
    //</context-param>
    
    //in order to be enabled, a password must be set, and it must be set to enabled
    if (password != null && "true".equals(usernameFilterForTestingEnabled)) {
      enabled = true;
      System.out.println("usernameFilterForTesting is enabled, password is in the web.xml, use the param usernameFilterForTestingUsername=something&usernameFilterForTestingPassword=******* to set the username");
    } else {
      System.out.println("usernameFilterForTesting is NOT enabled since usernameFilterForTestingEnabled param in web.xml is not true or there is no usernameFilterForTestingPassword set.  e.g. <context-param><param-name>usernameFilterForTestingEnabled</param-name><param-value>true</param-value></context-param>");
    }
    
  }

}
