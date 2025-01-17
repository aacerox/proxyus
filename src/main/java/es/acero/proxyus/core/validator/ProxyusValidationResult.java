package es.acero.proxyus.core.validator;

public class ProxyusValidationResult {
  private String errorMessage;

  public static ProxyusValidationResult of() {
    return new ProxyusValidationResult();
  }

  public void fail(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public boolean isFailed() {
    return this.errorMessage != null;
  }

  public void throwWhenFail(Object targetInstance) {
    if (this.errorMessage != null) {
      throw new UnsupportedOperationException(
          "Cannot create proxy instance from %s: %s "
              .formatted(targetInstance.getClass(), this.errorMessage));
    }
  }
}
