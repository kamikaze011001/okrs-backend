package org.ptit.okrs.core_util;

import java.text.DecimalFormat;
import java.util.Random;

public class GeneratorUtils {
  private static final String SIX_DIGITS_STRING = "000000";
  private static final Integer SIX_DIGITS_UPPER_BOUND = 1000000;

  public static String generateOtp() {
    Random random = new Random();
    return new DecimalFormat(SIX_DIGITS_STRING).format(random.nextInt(SIX_DIGITS_UPPER_BOUND));
  }
}
