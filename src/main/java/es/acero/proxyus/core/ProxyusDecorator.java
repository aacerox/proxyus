package es.acero.proxyus.core;

public interface ProxyusDecorator<T> {

  public T decorate(T targetInstance);
}
