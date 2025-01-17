package es.acero.proxyus.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import es.acero.proxyus.core.handler.ProxyusInstanceHandler;
import es.acero.proxyus.core.handler.ProxyusMethodHandler;
import es.acero.proxyus.core.method.ProxyusMethodSignature;

public class ProxyusRegistry<T> {

  private final Map<ProxyusMethodSignature, ProxyusMethodHandler<T>> methodHandlerRegistry =
      new HashMap<>();
  private ProxyusInstanceHandler proxyusInstanceHandler;

  public void register(ProxyusMethodHandler<T> methodHandler) {
    methodHandlerRegistry.put(methodHandler.getMethodSignature(), methodHandler);
  }

  public void register(ProxyusInstanceHandler proxyusInstanceHandler) {
    this.proxyusInstanceHandler = proxyusInstanceHandler;
  }

  public int getHandlersCount() {
    return methodHandlerRegistry.size();
  }

  public Optional<ProxyusMethodHandler<T>> find(Method method) {

    return Optional.ofNullable(methodHandlerRegistry.get(ProxyusMethodSignature.of(method)));
  }

  public ProxyusInstanceHandler getInstanceHandler() {
    return this.proxyusInstanceHandler;
  }
}
