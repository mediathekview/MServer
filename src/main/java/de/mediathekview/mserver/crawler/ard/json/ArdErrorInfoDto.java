package de.mediathekview.mserver.crawler.ard.json;

import java.util.Objects;

public class ArdErrorInfoDto {

  private final String code;
  private final String message;

  public ArdErrorInfoDto(final String code, final String message) {
    this.code = code;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getCode() {
    return code;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArdErrorInfoDto that)) {
      return false;
    }
    return Objects.equals(code, that.code) &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }
}
