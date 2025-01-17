package es.acero.proxyus.core;

import es.acero.proxyus.core.handler.ProxyusMethodHandler;

public interface ProxyusBuilder<T> {

  public ProxyusBuilder<T> addMethodHandler(ProxyusMethodHandler<T> methodHandler);

  public ProxyusBuilder<T> setProxyInstance(Object proxyInstance);

  public T build();
}
