package es.acero.proxyus.provider.jdk;

import es.acero.proxyus.core.ProxyusRegistry;
import es.acero.proxyus.core.handler.ProxyusMethodHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

public class ProxyusJdkInvocationHandler<T> implements InvocationHandler {

  private final ProxyusRegistry<T> registry;
  private final T targetInstance;
  private Object proxyInstance;

  protected ProxyusJdkInvocationHandler(T targetInstance) {
    this.targetInstance = targetInstance;
    this.registry = new ProxyusRegistry<>();
  }

  public void addMethodHandlers(ProxyusMethodHandler<T> methodHandler) {
    this.checkAlreadyProxied(this.registry.getInstanceHandler() != null);
    registry.register(methodHandler);
  }

  public void setProxyInstance(Object proxyInstance) {
    this.checkAlreadyProxied(this.registry.getHandlersCount() > 0);
    this.proxyInstance = proxyInstance;
    this.registry.register(new ProxyusJdkInstanceHandler());
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Object result = null;
    if (this.registry.getHandlersCount() > 0) {
      Optional<ProxyusMethodHandler<T>> methodHandler = this.registry.find(method);

      if (methodHandler.isPresent()) {
        result = methodHandler.get().invoke(this.targetInstance, args);
      } else {
        result = method.invoke(this.targetInstance, args);
      }
    } else {
      result = this.registry.getInstanceHandler().invoke(this.proxyInstance, this.targetInstance, method, args);
    }

    return result;
  }

  private void checkAlreadyProxied(boolean condition) {
    if (condition) {
      throw new IllegalStateException(
          "ProxyusMethodHandler and ProxyusInstance cannot coexist: please chose only one method to proxy %s"
              .formatted(this.targetInstance.getClass().getName()));
    }
  }
}
