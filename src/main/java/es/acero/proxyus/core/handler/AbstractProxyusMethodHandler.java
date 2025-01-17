package es.acero.proxyus.core.handler;

import es.acero.proxyus.core.method.ProxyusMethodSignature;

public abstract class AbstractProxyusMethodHandler<T> implements ProxyusMethodHandler<T> {

  private ProxyusMethodSignature methodSignature;

  protected AbstractProxyusMethodHandler(ProxyusMethodSignature methodSignature) {
    this.methodSignature = methodSignature;
  }

  @Override
  public boolean match(ProxyusMethodSignature methodSignature) {
    return this.methodSignature.equals(methodSignature);
  }

  @Override
  public ProxyusMethodSignature getMethodSignature() {
    return methodSignature;
  }
}
