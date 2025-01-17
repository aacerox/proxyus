package es.acero.proxyus.core.runner;

import es.acero.proxyus.core.method.ProxyusMethod;
import java.util.List;

public abstract class AbstractProxyusMethodRunStrategy implements ProxyusMethodRunStrategy {

  private final Object[] runtimeTargetArgs;

  protected AbstractProxyusMethodRunStrategy() {
    this((Object[]) null);
  }

  protected AbstractProxyusMethodRunStrategy(Object... runtimeTargetArgs) {

    this.runtimeTargetArgs = runtimeTargetArgs;
  }

  @Override
  public void run(List<ProxyusMethod> methods, Object... targetArgs) throws Throwable {
    this.runMethods(methods, this.getFinalTargetArgs(targetArgs));
  }

  public abstract void runMethods(List<ProxyusMethod> methods, Object... targetArgs)
      throws Throwable;

  private Object[] getFinalTargetArgs(Object... targetArgs) {
    Object[] finalTargetArgs = targetArgs;
    // add runtime target args to final  args
    if (runtimeTargetArgs != null) {
      finalTargetArgs = new Object[targetArgs.length + runtimeTargetArgs.length];
      System.arraycopy(targetArgs, 0, finalTargetArgs, 0, targetArgs.length);
      System.arraycopy(
          runtimeTargetArgs, 0, finalTargetArgs, targetArgs.length, runtimeTargetArgs.length);
    }
    return finalTargetArgs;
  }
}
