package es.acero.proxyus;

import java.util.function.Function;

import es.acero.proxyus.core.ProxyusBuilder;

public class Proxyus {

  private Proxyus() {
    super();
  }

  public static ProxyBuilderFactory useProxyBuilder(Function<Object, ProxyusBuilder<?>> builderFactoryFn) {
    return new ProxyBuilderFactory(builderFactoryFn);
  }

  public static final class ProxyBuilderFactory {
    private Function<Object, ProxyusBuilder<?>> builderFactoryFn;

    private ProxyBuilderFactory(Function<Object, ProxyusBuilder<?>> builderFactoryFn) {
      this.builderFactoryFn = builderFactoryFn;
    }

    @SuppressWarnings("unchecked")
    public <T> ProxyusBuilder<T> forInstance(T targetInstance) {
      return (ProxyusBuilder<T>) builderFactoryFn.apply(targetInstance);
    }
  }
}
