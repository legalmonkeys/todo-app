package com.challenge.todo.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for request/response logging with minimal, non-sensitive data.
 * Provides structured logging for monitoring and debugging without exposing sensitive information.
 */
@Configuration
public class LoggingConfig {

  private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOGGER");

  /**
   * Registers the request logging filter with high priority.
   */
  @Bean
  public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
    FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new RequestResponseLoggingFilter());
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registrationBean;
  }

  /**
   * Filter for logging HTTP requests and responses with minimal, non-sensitive data.
   * Uses MDC for correlation and structured logging.
   */
  public static class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      // Generate correlation ID for request tracking
      String correlationId = UUID.randomUUID().toString().substring(0, 8);
      String method = httpRequest.getMethod();
      String uri = httpRequest.getRequestURI();
      String queryString = httpRequest.getQueryString();
      String userAgent = getUserAgent(httpRequest);
      long startTime = System.currentTimeMillis();

      // Set MDC for structured logging
      try {
        MDC.put("correlationId", correlationId);
        MDC.put("method", method);
        MDC.put("uri", uri);

        // Log incoming request (non-sensitive data only)
        logIncomingRequest(correlationId, method, uri, queryString, userAgent);

        // Process the request
        chain.doFilter(request, response);

        // Log response (after processing)
        long duration = System.currentTimeMillis() - startTime;
        logOutgoingResponse(correlationId, method, uri, httpResponse.getStatus(), duration);

      } finally {
        // Clean up MDC
        MDC.clear();
      }
    }

    /**
     * Logs incoming HTTP requests with minimal, non-sensitive information.
     */
    private void logIncomingRequest(String correlationId, String method, String uri, 
                                   String queryString, String userAgent) {
      
      // Skip logging for static resources and health checks to reduce noise
      if (isStaticResource(uri) || isHealthCheck(uri)) {
        return;
      }

      String fullUri = queryString != null ? uri + "?" + sanitizeQueryString(queryString) : uri;
      
      requestLogger.info("→ {} {} {} [{}] {}",
          method,
          fullUri,
          correlationId,
          Instant.now(),
          userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 50)) : "Unknown"
      );
    }

    /**
     * Logs outgoing HTTP responses with status and timing information.
     */
    private void logOutgoingResponse(String correlationId, String method, String uri, 
                                    int status, long durationMs) {
      
      // Skip logging for static resources and health checks
      if (isStaticResource(uri) || isHealthCheck(uri)) {
        return;
      }

      String statusEmoji = getStatusEmoji(status);
      String performanceFlag = getPerformanceFlag(durationMs);
      
      requestLogger.info("← {} {} {} {} {}ms [{}]",
          method,
          uri,
          status,
          statusEmoji,
          durationMs,
          correlationId + performanceFlag
      );
    }

    /**
     * Sanitizes query string to remove potentially sensitive parameters.
     */
    private String sanitizeQueryString(String queryString) {
      if (queryString == null) {
        return "";
      }

      // Remove sensitive parameter values (keep parameter names for debugging)
      return queryString
          .replaceAll("(?i)(password|token|secret|key|auth)=[^&]*", "$1=***")
          .replaceAll("(?i)(email|user)=([^&@]+@[^&]+)", "$1=***@***.***");
    }

    /**
     * Gets a safe user agent string for logging (truncated).
     */
    private String getUserAgent(HttpServletRequest request) {
      String userAgent = request.getHeader("User-Agent");
      if (userAgent == null) {
        return "Unknown";
      }
      
      // Extract browser name for readability
      if (userAgent.contains("Chrome")) return "Chrome";
      if (userAgent.contains("Firefox")) return "Firefox";
      if (userAgent.contains("Safari")) return "Safari";
      if (userAgent.contains("Edge")) return "Edge";
      if (userAgent.contains("curl")) return "curl";
      if (userAgent.contains("Postman")) return "Postman";
      
      return "Browser";
    }

    /**
     * Determines if the URI is for a static resource.
     */
    private boolean isStaticResource(String uri) {
      return uri != null && (
          uri.startsWith("/css/") ||
          uri.startsWith("/js/") ||
          uri.startsWith("/images/") ||
          uri.startsWith("/favicon") ||
          uri.endsWith(".css") ||
          uri.endsWith(".js") ||
          uri.endsWith(".png") ||
          uri.endsWith(".jpg") ||
          uri.endsWith(".ico")
      );
    }

    /**
     * Determines if the URI is a health check endpoint.
     */
    private boolean isHealthCheck(String uri) {
      return uri != null && (
          uri.equals("/health") ||
          uri.equals("/actuator/health") ||
          uri.startsWith("/actuator/")
      );
    }

    /**
     * Returns an emoji representing the HTTP status code category.
     */
    private String getStatusEmoji(int status) {
      if (status >= 200 && status < 300) return "✅";
      if (status >= 300 && status < 400) return "↗️";
      if (status >= 400 && status < 500) return "❌";
      if (status >= 500) return "💥";
      return "❓";
    }

    /**
     * Returns a performance flag based on response time.
     */
    private String getPerformanceFlag(long durationMs) {
      if (durationMs > 2000) return " 🐌"; // Very slow
      if (durationMs > 1000) return " ⚠️";  // Slow
      if (durationMs > 500) return " ⏱️";   // Moderate
      return " ⚡"; // Fast
    }
  }
}
