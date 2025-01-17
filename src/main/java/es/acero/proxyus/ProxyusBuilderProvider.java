package es.acero.proxyus;

import java.util.function.Function;

import es.acero.proxyus.core.ProxyusBuilder;
import es.acero.proxyus.provider.jdk.ProxyusJdkBuilder;

public enum ProxyusBuilderProvider {
  JDK(targetInstance -> new ProxyusJdkBuilder<>(targetInstance));

  private Function<Object, ProxyusBuilder<?>> builderFactoryFn;

  private ProxyusBuilderProvider(Function<Object, ProxyusBuilder<?>> builderFactoryFn) {
    this.builderFactoryFn = builderFactoryFn;
  }

  @SuppressWarnings("unchecked")
  public <T> ProxyusBuilder<T> newProxyBuilder(T targetInstance) {
    return (ProxyusBuilder<T>) this.builderFactoryFn.apply(targetInstance);
  }
}
