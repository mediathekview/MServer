package mServer.crawler.sender.ard.json;

import java.util.Objects;

public class ArdErrorInfoDto {

  private String code;
  private String message;

  public ArdErrorInfoDto(String code, String message) {
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArdErrorInfoDto)) {
      return false;
    }
    ArdErrorInfoDto that = (ArdErrorInfoDto) o;
    return Objects.equals(code, that.code) &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }
}
