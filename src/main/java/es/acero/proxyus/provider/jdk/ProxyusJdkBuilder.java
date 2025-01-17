package es.acero.proxyus.provider.jdk;

import es.acero.proxyus.core.AbstractProxyusBuilder;
import es.acero.proxyus.core.ProxyusBuilder;
import es.acero.proxyus.core.handler.ProxyusMethodHandler;

import java.lang.reflect.Proxy;

public final class ProxyusJdkBuilder<T> extends AbstractProxyusBuilder<T> {

  private final ProxyusJdkInvocationHandler<T> invocationHandler;

  public ProxyusJdkBuilder(T targetInstance) {
    super(targetInstance);
    this.invocationHandler = new ProxyusJdkInvocationHandler<>(targetInstance);
  }

  @Override
  public ProxyusBuilder<T> addInnerMethodHandler(ProxyusMethodHandler<T> methodHandler) {
    this.invocationHandler.addMethodHandlers(methodHandler);
    return this;
  }

  @Override
  public ProxyusBuilder<T> setInnerProxyInstance(Object proxyInstance) {
    this.invocationHandler.setProxyInstance(proxyInstance);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T innerBuild() {
    return (T)
        Proxy.newProxyInstance(
            getClassLoader(), getTargetClassInterfaces(), this.invocationHandler);
  }
}
