package es.acero.proxyus.core;

import es.acero.proxyus.core.handler.ProxyusMethodHandler;
import es.acero.proxyus.core.validator.ProxyusInstanceValidator;

public abstract class AbstractProxyusBuilder<T> implements ProxyusBuilder<T> {

  private final Class<?> targetClazz;
  private final T targetInstance;
  private boolean usingMethodHandler = false;
  private boolean usingInstanceHandler = false;

  protected AbstractProxyusBuilder(T targetInstance) {
    super();
    this.targetClazz = targetInstance.getClass();
    this.targetInstance = targetInstance;
  }

  @Override
  public ProxyusBuilder<T> addMethodHandler(ProxyusMethodHandler<T> methodHandler) {
    usingMethodHandler = true;
    return this.addInnerMethodHandler(methodHandler);
  }

  @Override
  public ProxyusBuilder<T> setProxyInstance(Object proxyInstance) {
    usingInstanceHandler = true;
    return setInnerProxyInstance(new ProxyusInstanceValidator(proxyInstance).validate());
  }

  @Override
  public T build() {
    // check that only one handler method is used
    if (usingInstanceHandler && usingMethodHandler) {
      throw new IllegalStateException(
          "Cannot instantiate proxy: Only one handler method can be used. Choose between"
              + " addMethodHandler or setProxyInstance.");
    }

    return innerBuild();
  }

  public abstract ProxyusBuilder<T> addInnerMethodHandler(ProxyusMethodHandler<T> methodHandler);

  public abstract ProxyusBuilder<T> setInnerProxyInstance(Object proxyInstance);

  public abstract T innerBuild();

  protected Class<?> getTargetClazz() {
    return targetClazz;
  }

  protected T getTargetInstance() {
    return targetInstance;
  }

  protected Class<?>[] getTargetClassInterfaces() {
    Class<?>[] result = null;
    // check
    if (targetClazz.isInterface()) {
      // if class is already an interface, use it as proxy interface
      result = new Class<?>[] {targetClazz};
    } else {
      // extract interfaces from instance
      result = targetClazz.getInterfaces();
    }

    return result;
  }

  protected ClassLoader getClassLoader() {
    return targetInstance.getClass().getClassLoader();
  }
}
