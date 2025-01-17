package es.acero.proxyus.core.runner;

import java.util.List;

import es.acero.proxyus.core.method.ProxyusMethod;

public interface ProxyusMethodRunStrategy {
  void run(List<ProxyusMethod> methods, Object... targetArgs) throws Throwable;
}
