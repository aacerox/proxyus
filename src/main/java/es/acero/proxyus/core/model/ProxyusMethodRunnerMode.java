package es.acero.proxyus.core.model;

public class ProxyusMethodRunnerMode {
  private boolean parallel;

  public static ProxyusMethodRunnerMode parallel() {
    return new ProxyusMethodRunnerMode(true);
  }

  public static ProxyusMethodRunnerMode sequential() {
    return new ProxyusMethodRunnerMode(false);
  }

  private ProxyusMethodRunnerMode(boolean parallel) {
    this.parallel = parallel;
  }

  public boolean isParallel() {
    return parallel;
  }
}
