package es.acero.proxyus.core.handler;

import es.acero.proxyus.core.method.ProxyusMethodSignature;

public interface ProxyusMethodHandler<T> {

  ProxyusMethodSignature getMethodSignature();

  boolean match(ProxyusMethodSignature methodSignature);

  Object invoke(T targetInstance, Object[] args) throws Throwable;
}
