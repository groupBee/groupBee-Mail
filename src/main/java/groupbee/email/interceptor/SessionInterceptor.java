package groupbee.email.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public class SessionInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
//            HttpSession session = request.getSession();
//            String springSession = request.getHeader("Cookie");
            String springSession = "SESSION=YzEzNWUxNGQtZTNhMC00ZWE3LWI4ZWEtZDk0YTk0YjhjMDIw";
//            HttpSession session = attributes.getRequest().getSession(false);
            template.header("Cookie", springSession);
        }
    }
}