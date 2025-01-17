package es.acero.proxyus.core.runner;

import es.acero.proxyus.core.method.ProxyusMethod;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ProxyusMethodParallelRunner extends AbstractProxyusMethodRunStrategy {

  public ProxyusMethodParallelRunner() {
    super();
  }

  public ProxyusMethodParallelRunner(Object... runtimeTargetArgs) {
    super(runtimeTargetArgs);
  }

  @Override
  public void runMethods(List<ProxyusMethod> methods, Object... targetArgs) throws Throwable {
    ForkJoinPool pool = ForkJoinPool.commonPool();
    methods.forEach(method -> pool.execute(new ProxyusMethodWorker(method, targetArgs)));
  }

  private static final class ProxyusMethodWorker extends RecursiveAction {

    private final ProxyusMethod method;
    private final Object[] targetArgs;

    public ProxyusMethodWorker(ProxyusMethod method, Object[] targetArgs) {
      this.method = method;
      this.targetArgs = targetArgs;
    }

    @Override
    protected void compute() {
      try {
        method.invoke(targetArgs);
      } catch (Throwable ex) {
        throw new UnsupportedOperationException(
            "Cannot run parallel ProxyusMethod: %s".formatted(ex.getMessage()));
      }
    }
  }
}
