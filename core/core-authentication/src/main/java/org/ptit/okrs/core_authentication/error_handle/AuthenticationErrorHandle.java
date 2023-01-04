package org.ptit.okrs.core_authentication.error_handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ptit.okrs.core_util.DateUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Component
public class AuthenticationErrorHandle implements AuthenticationEntryPoint {
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException, ServletException {
    var error = new HashMap<String, Object>();
    error.put("status", 401);
    error.put("timestamp", DateUtils.getCurrentDateTimeStr());
    error.put("message", "UnAuthenticated.");
    response.sendError(401, new ObjectMapper().writeValueAsString(error));
  }
}
