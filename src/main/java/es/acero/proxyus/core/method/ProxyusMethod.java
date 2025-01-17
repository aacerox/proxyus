package es.acero.proxyus.core.method;

@FunctionalInterface
public interface ProxyusMethod {

  <T> T invoke(Object... args) throws Throwable;
}
