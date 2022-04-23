// package run.halo.app.config;
//
// import javax.servlet.DispatcherType;
//
// import cn.hutool.core.util.StrUtil;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.web.servlet.FilterRegistrationBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// import java.util.HashMap;
// import java.util.Map;
// import run.halo.app.filter.XssFilter;
//
// /**
//  * xss configuration
//  *
//  * @author Ljfanny, Yhcrown
//  */
// @Configuration
// @ConditionalOnProperty(
//     value = "xss.enabled",
//     havingValue = "true",
//     matchIfMissing = false)
// public class XssFilterConfiguration {
//
//     @Value("${xss.enabled}")
//     private String enabled;
//
//     @Value("${xss.excludes}")
//     private String excludes;
//
//     @Value("${xss.urlPatterns}")
//     private String urlPatterns;
//
//     @SuppressWarnings({"rawtypes", "unchecked"})
//     @Bean
//     public FilterRegistrationBean xssFilterRegistration() {
//         FilterRegistrationBean registration = new FilterRegistrationBean();
//         registration.setDispatcherTypes(DispatcherType.REQUEST);
//         registration.setFilter(new XssFilter());
//         registration.addUrlPatterns(String.valueOf(StrUtil.split(urlPatterns, ",")));
//         registration.setName("xssFilter");
//         Map<String, String> initParameters = new HashMap<String, String>();
//         initParameters.put("excludes", excludes);
//         initParameters.put("enabled", enabled);
//         registration.setInitParameters(initParameters);
//         return registration;
//     }
//
// }
