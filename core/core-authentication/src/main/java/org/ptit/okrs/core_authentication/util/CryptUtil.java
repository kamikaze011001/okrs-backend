package org.ptit.okrs.core_authentication.util;

import java.util.Base64;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CryptUtil {

  private static final BCryptPasswordEncoder B_CRYPT_ENCODER = new BCryptPasswordEncoder();

  public static PasswordEncoder getPasswordEncoder() {
    return B_CRYPT_ENCODER;
  }

  public static String generateResetKey(String email) {
    var uuid = UUID.randomUUID().toString();
    return Base64.getEncoder().encodeToString((uuid + email).getBytes());
  }
}
