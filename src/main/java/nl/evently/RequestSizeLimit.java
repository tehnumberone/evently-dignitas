package nl.evently;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestSizeLimit implements WebMvcConfigurer {

    static final long MAX_BODY_BYTES = 1_000_000;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (request.getContentLengthLong() > MAX_BODY_BYTES) {
                    throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
                            "Request body must be at most " + MAX_BODY_BYTES + " bytes");
                }
                if (request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
                    throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "Content-Length header is required");
                }
                return true;
            }
        });
    }
}
