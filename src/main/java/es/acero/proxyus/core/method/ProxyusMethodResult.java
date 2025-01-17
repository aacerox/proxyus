package es.acero.proxyus.core.method;

import java.util.Optional;

@FunctionalInterface
public interface ProxyusMethodResult extends ProxyusMethodParameter {

  <T> Optional<T> get();
}
