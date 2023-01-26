package org.ptit.okrs.core_util;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_exception.DateInvalidException;
import org.ptit.okrs.core_exception.ForbiddenException;
import org.ptit.orks.core_audit.SecurityService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
public class ValidationUtils {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

  private ValidationUtils() {}

  /**
   * Function validate int date with date format: yyyyMMdd
   *
   * @param date - date need to check
   * @return boolean
   */
  public static boolean validateDate(Integer date) {
    var dateStr = String.valueOf(date).trim();
    try {
      LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
    } catch (RuntimeException e) {
      return false;
    }
    return true;
  }

  /**
   * Function validate start-date, end-date - If start-date, end date is invalid format -> return
   * false - If start-date greater than end-date -> return false
   *
   * @param startDate - input
   * @param endDate - input
   * @return boolean
   */
  public static boolean validateStartDateAndEndDate(Integer startDate, Integer endDate) {

    if (Objects.isNull(startDate) && Objects.isNull(endDate)) {
      return false;
    }

    if (Objects.isNull(startDate)) {
      return validateDate(endDate);
    }

    if (Objects.isNull(endDate)) {
      return validateDate(startDate);
    }

    if (!validateDate(startDate) || !validateDate(endDate)) {
      return false;
    }

    var currentDateInt = DateUtils.getCurrentDateInteger();
    return startDate > endDate || startDate > currentDateInt || endDate < currentDateInt;
  }

  public static void validateForbiddenUser(String userId) {

    if (!SecurityService.getUserId().equals(userId)) {
      log.info("(validateForbiddenUser)userId : {}", userId);
      throw new ForbiddenException(userId);
    }
  }

  public static void validateDateFormat(int date) {
    if (!validateDate(date)) {
      log.info("(validateDateFormat)date : {}", date);
      throw new DateInvalidException(date);
    }
  }
}
