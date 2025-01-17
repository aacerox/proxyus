package es.acero.proxyus.core.handler;

import java.lang.reflect.Method;

public interface ProxyusInstanceHandler {

  public Object invoke(
      Object proxyedInstance, Object proxyInstance, Method proxyedMethod, Object[] proxyedArgs)
      throws Throwable;
}
