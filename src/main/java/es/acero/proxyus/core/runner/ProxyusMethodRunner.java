package es.acero.proxyus.core.runner;

import es.acero.proxyus.core.method.ProxyusMethod;
import es.acero.proxyus.core.method.ProxyusMethodResult;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class ProxyusMethodRunner {

  private final ProxyusMethodRunnerBuilder builder;

  private ProxyusMethodRunner(ProxyusMethodRunnerBuilder builder) {
    this.builder = builder;
  }

  public Object run(Object proxyInstance, Method proxiedMethod, Object[] proxiedArgs)
      throws Throwable {

    Object result = null;

    // run parallel before proxy methods
    if (!this.builder.getParallelBeforeMethods().isEmpty()) {
      new ProxyusMethodParallelRunner().run(this.builder.getParallelBeforeMethods(), proxiedArgs);
    }
    // run serial before proxy methods
    if (!this.builder.getSerialBeforeMethods().isEmpty()) {
      new ProxyusMethodSerialRunner().run(this.builder.getSerialBeforeMethods(), proxiedArgs);
    }
    // run intercept method
    Optional<ProxyusMethod> interceptMethod = builder.getInterceptMethod();

    if (interceptMethod.isPresent()) {
      result = interceptMethod.get().invoke(proxiedArgs);
    } else {
      // run non proxied target method
      result = proxiedMethod.invoke(proxyInstance, proxiedArgs);
    }

    // run parallel before proxy methods
    if (!this.builder.getParallelAfterMethods().isEmpty()) {
      new ProxyusMethodParallelRunner(this.toProxyusMethodResult(result))
          .run(this.builder.getParallelAfterMethods(), proxiedArgs);
    }

    // run serial before proxy methods
    if (!this.builder.getSerialAfterMethods().isEmpty()) {
      new ProxyusMethodSerialRunner(this.toProxyusMethodResult(result))
          .run(this.builder.getSerialAfterMethods(), proxiedArgs);
    }

    return result;
  }

  private ProxyusMethodResult toProxyusMethodResult(Object methodResult) {
    return new ProxyusMethodResult() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> Optional<T> get() {
        return (Optional<T>) Optional.ofNullable(methodResult);
      }
    };
  }

  public static final class ProxyusMethodRunnerBuilder {

    private List<ProxyusMethod> parallelBeforeMethods = List.of();
    private List<ProxyusMethod> serialBeforeMethods = List.of();
    private Optional<ProxyusMethod> interceptMethod = Optional.empty();
    private List<ProxyusMethod> parallelAfterMethods = List.of();
    private List<ProxyusMethod> serialAfterMethods = List.of();

    public ProxyusMethodRunnerBuilder withBeforeSerialMethods(List<ProxyusMethod> proxyusMethods) {
      this.serialBeforeMethods = proxyusMethods;
      return this;
    }

    public ProxyusMethodRunnerBuilder withBeforeParallelMethods(
        List<ProxyusMethod> proxyusMethods) {
      this.parallelBeforeMethods = proxyusMethods;
      return this;
    }

    public ProxyusMethodRunnerBuilder withInterceptMethod(Optional<ProxyusMethod> proxyusMethod) {
      this.interceptMethod = proxyusMethod;
      return this;
    }

    public ProxyusMethodRunnerBuilder withAfterSerialMethods(List<ProxyusMethod> proxyusMethods) {
      this.serialAfterMethods = proxyusMethods;
      return this;
    }

    public ProxyusMethodRunnerBuilder withAfterParallelMethods(List<ProxyusMethod> proxyusMethods) {
      this.parallelAfterMethods = proxyusMethods;
      return this;
    }

    public ProxyusMethodRunner build() {
      return new ProxyusMethodRunner(this);
    }

    private List<ProxyusMethod> getParallelBeforeMethods() {
      return parallelBeforeMethods;
    }

    private List<ProxyusMethod> getSerialBeforeMethods() {
      return serialBeforeMethods;
    }

    private Optional<ProxyusMethod> getInterceptMethod() {
      return interceptMethod;
    }

    private List<ProxyusMethod> getParallelAfterMethods() {
      return parallelAfterMethods;
    }

    private List<ProxyusMethod> getSerialAfterMethods() {
      return serialAfterMethods;
    }
  }
}
