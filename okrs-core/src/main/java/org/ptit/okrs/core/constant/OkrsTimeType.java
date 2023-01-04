package org.ptit.okrs.core.constant;

public enum OkrsTimeType {
  A_MONTH(OkrsTimeTypeDuration.A_MONTH),
  THREE_MONTH(OkrsTimeTypeDuration.THREE_MONTH),
  SIX_MONTH(OkrsTimeTypeDuration.SIX_MONTH),
  A_YEAR(OkrsTimeTypeDuration.A_YEAR),
  THREE_YEAR(OkrsTimeTypeDuration.THREE_YEAR),
  CUSTOM(OkrsTimeTypeDuration.CUSTOM);

  private final Integer value;

  OkrsTimeType(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return this.value;
  }
}
