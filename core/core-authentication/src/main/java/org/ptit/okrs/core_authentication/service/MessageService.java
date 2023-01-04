package org.ptit.okrs.core_authentication.service;

import java.util.Locale;
import java.util.Map;

public interface MessageService {

  /**
   * return i18n message to client
   * @param code - code of the message in properties
   * @param locale - locale of client
   * @param params - params of i18n message
   * @return i18n message
   */
  String getI18nMessage(String code, Locale locale, Map<String, String> params);
}
