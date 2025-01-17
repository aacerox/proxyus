package es.acero.proxyus.core.runner;

import es.acero.proxyus.core.method.ProxyusMethod;
import java.util.List;

public class ProxyusMethodSerialRunner extends AbstractProxyusMethodRunStrategy {

  public ProxyusMethodSerialRunner() {
    super();
  }

  public ProxyusMethodSerialRunner(Object... runtimeTargetArgs) {
    super(runtimeTargetArgs);
  }

  @Override
  public void runMethods(List<ProxyusMethod> methods, Object... targetArgs) throws Throwable {
    for (ProxyusMethod method : methods) {
      method.invoke(targetArgs);
    }
  }
}
