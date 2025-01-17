package es.acero.proxyus.provider.jdk;

import es.acero.proxyus.core.annotation.ProxyusAfterMethod;
import es.acero.proxyus.core.annotation.ProxyusBeforeMethod;
import es.acero.proxyus.core.handler.ProxyusInstanceHandler;
import es.acero.proxyus.core.method.ProxyusMethod;
import es.acero.proxyus.core.runner.ProxyusMethodRunner;
import es.acero.proxyus.core.runner.ProxyusMethodRunner.ProxyusMethodRunnerBuilder;
import es.acero.proxyus.core.util.ProxyusAnnotationUtil;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProxyusJdkInstanceHandler implements ProxyusInstanceHandler {

  private final Map<Method, ProxyusMethodRunner> methodRunnerRegistry = new HashMap<>();

  @Override
  public Object invoke(
      final Object proxyInstance, Object targetInstance, Method proxiedMethod, Object[] proxiedArgs)
      throws Throwable {
    ProxyusMethodRunner methodRunner = null;
    // check if method runner is already registered
    if (methodRunnerRegistry.containsKey(proxiedMethod)) {
      methodRunner = methodRunnerRegistry.get(proxiedMethod);
    } else {
      // create method runner

      Map<Boolean, List<ProxyusMethodRunnerRecord>> beforeMethodRecords =
          this.createBeforeProxyusMethods(proxyInstance, proxiedMethod);

      Map<Boolean, List<ProxyusMethodRunnerRecord>> afterMethodRecords =
          this.createAfterProxyusMethods(proxyInstance, proxiedMethod);

      methodRunner =
          new ProxyusMethodRunnerBuilder()
              .withBeforeSerialMethods(
                  beforeMethodRecords.get(false).stream()
                      .map(ProxyusMethodRunnerRecord::method)
                      .toList())
              .withBeforeParallelMethods(
                  beforeMethodRecords.get(true).stream()
                      .map(ProxyusMethodRunnerRecord::method)
                      .toList())
              .withInterceptMethod(
                  this.createInterceptProxyusMethod(proxyInstance, targetInstance, proxiedMethod))
              .withAfterSerialMethods(
                  afterMethodRecords.get(false).stream()
                      .sorted((a, b) -> Integer.compare(a.order, b.order))
                      .map(ProxyusMethodRunnerRecord::method)
                      .toList())
              .withAfterParallelMethods(
                  afterMethodRecords.get(true).stream()
                      .map(ProxyusMethodRunnerRecord::method)
                      .toList())
              .build();

      methodRunnerRegistry.put(proxiedMethod, methodRunner);
    }

    return methodRunner.run(proxyInstance, proxiedMethod, proxiedArgs);
  }

  private Map<Boolean, List<ProxyusMethodRunnerRecord>> createBeforeProxyusMethods(
      Object proxyInstance, Method targetMethod) {

    return ProxyusAnnotationUtil.getProxyusBeforeMethodStream(proxyInstance)
        // filter by target method name
        .filter(
            proxyMethod ->
                ProxyusAnnotationUtil.getBeforeAnnotation(proxyMethod)
                    .orElseThrow()
                    .value()
                    .equals(targetMethod.getName()))
        // map to MethodRunnerRecord
        .map(
            proxyMethod -> {
              ProxyusBeforeMethod proxyusBefore =
                  ProxyusAnnotationUtil.getBeforeAnnotation(proxyMethod).orElseThrow();
              return new ProxyusMethodRunnerRecord(
                  ProxyusMethodImpl.of(proxyInstance, proxyMethod),
                  proxyusBefore.runMode().parallel(),
                  proxyusBefore.runMode().order());
            })
        .collect(Collectors.partitioningBy(methodRunner -> methodRunner.parallel));
  }

  private Optional<ProxyusMethod> createInterceptProxyusMethod(
      Object proxyInstance, Object targetInstance, Method targetMethod) {

    return ProxyusAnnotationUtil.getProxyusInterceptMethodStream(proxyInstance)
        .filter(
            proxyMethod ->
                ProxyusAnnotationUtil.getInterceptAnnotation(proxyMethod)
                    .orElseThrow()
                    .value()
                    .equals(targetMethod.getName()))
        .map(
            proxyMethod -> {
              Map<Class<?>, Object> extraArgsMap = Map.of();
              if (this.hasLastArgumentOfType(proxyMethod, ProxyusMethod.class)) {
                extraArgsMap =
                    Map.of(ProxyusMethod.class, ProxyusMethodImpl.of(targetInstance, targetMethod));
              }
              return ProxyusMethodImpl.of(proxyInstance, proxyMethod, extraArgsMap);
            })
        .findFirst();
  }

  private Map<Boolean, List<ProxyusMethodRunnerRecord>> createAfterProxyusMethods(
      Object proxyInstance, Method targetMethod) {

    return ProxyusAnnotationUtil.getProxyusAfterMethodStream(proxyInstance)
        // filter by target method name
        .filter(
            proxyMethod ->
                ProxyusAnnotationUtil.getAfterAnnotation(proxyMethod)
                    .orElseThrow()
                    .value()
                    .equals(targetMethod.getName()))
        // map to MethodRunnerRecord
        .map(
            proxyMethod -> {
              ProxyusAfterMethod proxyusAfter =
                  ProxyusAnnotationUtil.getAfterAnnotation(proxyMethod).orElseThrow();
              return new ProxyusMethodRunnerRecord(
                  ProxyusMethodImpl.of(proxyInstance, proxyMethod, Map.of()),
                  proxyusAfter.runMode().parallel(),
                  proxyusAfter.runMode().order());
            })
        .collect(Collectors.partitioningBy(methodRunner -> methodRunner.parallel));
  }

  private boolean hasLastArgumentOfType(Method proxyMethod, Class<?> type) {
    return proxyMethod.getParameters()[proxyMethod.getParameterCount() - 1].getType().equals(type);
  }

  private static final class ProxyusMethodImpl implements ProxyusMethod {
    private final Method method;
    private final Object instance;
    private final Map<Class<?>, Object> extraArgsMap;

    public static ProxyusMethod of(Object instance, Method method) {
      return ProxyusMethodImpl.of(instance, method, Map.of());
    }

    public static ProxyusMethod of(
        Object instance, Method method, Map<Class<?>, Object> extraArgsMap) {
      return new ProxyusMethodImpl(instance, method, extraArgsMap);
    }

    private ProxyusMethodImpl(Object instance, Method method, Map<Class<?>, Object> extraArgsMap) {
      super();
      this.method = method;
      this.instance = instance;
      this.extraArgsMap = extraArgsMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(Object... args) throws Throwable {
      Object[] finalArgs = resolveFinalArgs(args);
      // Check that method args match final args if not remove extra args
      if (this.method.getParameterCount() != finalArgs.length) {
        finalArgs = Arrays.copyOf(finalArgs, this.method.getParameterCount());
      }

      return (T) this.method.invoke(instance, finalArgs);
    }

    private Object[] resolveFinalArgs(Object... args) {
      Object[] finalArgs = args;

      if (!extraArgsMap.isEmpty()) {
        finalArgs = Arrays.copyOf(args, args.length + this.extraArgsMap.size());
        // fill expected parameters with extra args
        for (int i = args.length; i < finalArgs.length; i++) {
          finalArgs[i] = this.extraArgsMap.get(method.getParameterTypes()[i]);
        }
      }
      return finalArgs;
    }
  }

  private record ProxyusMethodRunnerRecord(ProxyusMethod method, boolean parallel, int order) {}
}
