package es.acero.proxyus.core.validator;

import es.acero.proxyus.core.annotation.Proxyus;
import es.acero.proxyus.core.annotation.ProxyusInterceptMethod;
import es.acero.proxyus.core.method.ProxyusMethod;
import es.acero.proxyus.core.method.ProxyusMethodParameter;
import es.acero.proxyus.core.util.ProxyusAnnotationUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProxyusValidationRules {

  private ProxyusValidationRules() {
    // hidden constructor
  }

  /**
   * Check that at least one @ProxyusMethod annotation is present
   *
   * @param proxyInstance the proxy instance to validate
   * @return a validation result
   */
  public static ProxyusValidationResult checkProxyusMethodAnnotationPresent(Object proxyInstance) {
    ProxyusValidationResult result = ProxyusValidationResult.of();
    long beforeMethods = ProxyusAnnotationUtil.getProxyusBeforeMethodStream(proxyInstance).count();
    long interceptMethods =
        ProxyusAnnotationUtil.getProxyusInterceptMethodStream(proxyInstance).count();
    long afterMethods = ProxyusAnnotationUtil.getProxyusAfterMethodStream(proxyInstance).count();

    if ((beforeMethods + interceptMethods + afterMethods) == 0) {
      result.fail(
          "No @ProxyusMethod annotation found. Add @ProxyusBeforeMethod, @ProxyusAfterMethod or"
              + " @ProxyusInterceptMethod annotation to a class public method");
    }

    return result;
  }

  /**
   * Check that a @Proxyus annotation is present
   *
   * @param proxyInstance the proxy instance to validate
   * @return a validation result
   */
  public static ProxyusValidationResult checkProxyusAnnotationPresent(Object proxyInstance) {
    ProxyusValidationResult result = ProxyusValidationResult.of();
    if (!ProxyusAnnotationUtil.hasProxyusAnnotation(proxyInstance)) {
      result.fail("No @Proxyus annotation found");
    }
    return result;
  }

  /**
   * Check that there's only one intercept annotation per method name
   *
   * @param proxyInstance the proxy instance to validate
   * @return a validation result
   */
  public static ProxyusValidationResult checkOneInterceptByTargetMethod(Object proxyInstance) {
    ProxyusValidationResult result = ProxyusValidationResult.of();

    Map<String, List<Method>> interceptAnnotationByMethod =
        ProxyusAnnotationUtil.getProxyusInterceptMethodStream(proxyInstance)
            .collect(
                Collectors.groupingBy(
                    method ->
                        ProxyusAnnotationUtil.getInterceptAnnotation(method)
                            .map(ProxyusInterceptMethod::value)
                            .orElseThrow()));

    for (Entry<String, List<Method>> entry : interceptAnnotationByMethod.entrySet()) {
      // more than one intercept annotated method for the same target method found
      if (entry.getValue().size() > 1) {
        result.fail(
            "More than one @ProxyusInterceptMethod found for target method '%s'"
                .formatted(entry.getKey()));
      }
    }

    return result;
  }

  /**
   * Check that a method annotated with @ProxyusBeforeMethod or @ProxyusAfterMethod returns void
   *
   * @param method the method to validate
   * @return a validation result
   */
  public static ProxyusValidationResult checkVoidReturnOnMethod(Method method) {

    ProxyusValidationResult result = ProxyusValidationResult.of();
    if (!method.getReturnType().equals(Void.TYPE)) {
      result.fail(
          "Method %s annotated with @ProxyusBeforeMethod od @ProxyusAfterMethod should return void"
              .formatted(method.getName()));
    }
    return result;
  }

  /**
   * Check that a method annotated with @ProxyusInterceptMethod has its last argument of type
   * ProxyusMethod
   *
   * @param method the method to validate
   * @return a validation result
   */
  public static ProxyusValidationResult checkMethodInterceptLastArgType(Method method) {

    ProxyusValidationResult result = ProxyusValidationResult.of();
    Parameter lastMethodArgument = method.getParameters()[method.getParameterCount() - 1];
    if (!lastMethodArgument.getType().equals(ProxyusMethod.class)) {
      result.fail(
          "Method %s annotated with @ProxyusMethodIntercept should have its last argument of type %s"
              .formatted(method.getName(), ProxyusMethod.class.getName()));
    }
    return result;
  }

  /**
   * Check that the return type of a method annotated with @ProxyusInterceptMethod matches the
   * expected return type of the proxied method name
   *
   * @param proxyInstance the proxy instance to validate
   * @return a validation result
   */
  public static ProxyusMethodValidationRule checkReturnValueMatchExpected(Object proxyInstance) {
    return proxyMethod -> {
      ProxyusValidationResult result = ProxyusValidationResult.of();
      Class<?> proxiedType = getProxiedType(proxyInstance);
      String proxiedMethodName = getProxiedMethodName(proxyMethod);
      // proxied method should match proxyus intercept annotation method name and instance method
      // return type
      boolean proxiedMethodNameTypeNotFound =
          Arrays.stream(proxiedType.getMethods())
              .noneMatch(
                  proxiedMethod ->
                      proxiedMethod.getName().equals(proxiedMethodName)
                          && proxiedMethod.getReturnType().equals(proxyMethod.getReturnType()));

      if (proxiedMethodNameTypeNotFound) {
        result.fail(
            "Method %s annotated with @ProxyusInterceptMethod should have a return type of %s"
                .formatted(proxyMethod.getName(), proxiedType.getName()));
      }
      return result;
    };
  }

  /**
   * Check that the arguments of a method annotated
   * with @ProxyusBeforeMethod, @ProxyusInterceptMethod or @ProxyusAfterMethod match the proxied
   * method signature
   *
   * @param proxyInstance the proxy instance to validate
   * @param proxiedMethodNameResolverFn a function to resolve the proxied method name
   * @return a validation result
   */
  public static ProxyusMethodValidationRule checkArgsMatchProxied(
      Object proxyInstance, Function<Method, String> proxiedMethodNameResolverFn) {
    return proxyMethod -> {
      ProxyusValidationResult result = ProxyusValidationResult.of();
      Class<?> proxiedType = getProxiedType(proxyInstance);
      String proxiedMethodName = proxiedMethodNameResolverFn.apply(proxyMethod);

      // check that proxied method signature matches the instance method signature
      Optional<Method> targetMethod = findTargetMethod(proxiedType, proxiedMethodName);

      if (targetMethod.isEmpty()) {
        result.fail(
            "Cannot find any method %s on proxied class %s"
                .formatted(proxyMethod.getName(), proxiedType.getName()));

      } else {

        // check that the method signature matches the target method signature
        Method proxiedMethod = targetMethod.get();

        // check that the number of parameters match
        result = checkArgsCountMatchProxied(proxyMethod, proxiedMethod);
        if (!result.isFailed()) {
          // check that the type of the parameters match
          result = checkArgsTypeMatchProxied(proxyMethod, proxiedType, proxiedMethod);
        }
      }
      return result;
    };
  }

  private static Optional<Method> findTargetMethod(Class<?> proxiedType, String proxiedMethodName) {
    return ProxyusAnnotationUtil.getProxiedMethodStream(proxiedType)
        .filter(proxiedMethod -> proxiedMethod.getName().equals(proxiedMethodName))
        .findFirst();
  }

  private static ProxyusValidationResult checkArgsTypeMatchProxied(
      Method method, Class<?> proxiedType, Method proxiedMethod) {
    ProxyusValidationResult result = ProxyusValidationResult.of();

    for (int i = 0; i < proxiedMethod.getParameters().length; i++) {
      Parameter proxiedMethodParameter = proxiedMethod.getParameters()[i];
      Parameter instanceMethodParameter = method.getParameters()[i];
      if (!proxiedMethodParameter.getType().equals(instanceMethodParameter.getType())) {
        result.fail(
            "Parameter %s does not match expected type: Method %s annotated with @ProxyusBeforeMethod, @ProxyusInterceptMethod or @ProxyusAfterMethod should have the same signature as %s"
                .formatted(
                    instanceMethodParameter.getName(), method.getName(), proxiedType.getName()));
        break;
      }
    }

    return result;
  }

  private static ProxyusValidationResult checkArgsCountMatchProxied(
      Method proxyMethod, Method proxiedMethod) {
    ProxyusValidationResult result = ProxyusValidationResult.of();
    int proxiedMethodArgsCount = proxiedMethod.getParameters().length;
    // Parameters that extends ProxyusMethodParameters are skipped because they are added
    // dinamically durign the method invocation
    int instanceMethodArgsCount =
        Arrays.stream(proxyMethod.getParameters())
            .filter(
                parameter -> !ProxyusMethodParameter.class.isAssignableFrom(parameter.getType()))
            .toArray()
            .length;
    if (ProxyusAnnotationUtil.getAfterAnnotation(proxyMethod).isPresent()
        || ProxyusAnnotationUtil.getBeforeAnnotation(proxyMethod).isPresent()) {
      if (proxiedMethodArgsCount != instanceMethodArgsCount) {
        result.fail(
            "Parameter count does not match: Proxied method %s has %d parameters while method %s annotated with @ProxyusBeforeMethod or @ProxyusAfterMethod has %d parameters"
                .formatted(
                    proxiedMethod.getName(),
                    proxiedMethodArgsCount,
                    proxyMethod.getName(),
                    instanceMethodArgsCount));
      }
    } else if (ProxyusAnnotationUtil.getInterceptAnnotation(proxyMethod).isPresent()
        && (proxiedMethodArgsCount != (instanceMethodArgsCount - 1))) {
      // subtract 1 to skip last ProxyusMethod type parameter, where intercepted method should
      // go
      result.fail(
          "Parameter count does not match: Proxied method %s has %d parameters while method %s annotated with @ProxyusInterceptMethod has %d parameters subtracting last required parameter of type 'ProxyusMethod'"
              .formatted(
                  proxiedMethod.getName(),
                  proxiedMethodArgsCount,
                  proxyMethod.getName(),
                  instanceMethodArgsCount));
    }

    return result;
  }

  private static String getProxiedMethodName(Method method) {
    return ProxyusAnnotationUtil.getInterceptAnnotation(method)
        .map(ProxyusInterceptMethod::value)
        .orElseThrow();
  }

  private static Class<?> getProxiedType(Object proxyInstance) {
    return ProxyusAnnotationUtil.getProxyusAnnotation(proxyInstance)
        .map(Proxyus::value)
        .orElseThrow();
  }
}
