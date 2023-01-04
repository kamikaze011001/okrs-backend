package org.ptit.orks.core_audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityService {
  public static String getUserId() {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      return "SYSTEM_ID";
    }
    return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
  }

  public static String getUserName() {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      return "SYSTEM";
    }
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
  }
}
