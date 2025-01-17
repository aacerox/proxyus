package es.acero.proxyus.core.util;

import es.acero.proxyus.core.annotation.Proxyus;
import es.acero.proxyus.core.annotation.ProxyusAfterMethod;
import es.acero.proxyus.core.annotation.ProxyusBeforeMethod;
import es.acero.proxyus.core.annotation.ProxyusInterceptMethod;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ProxyusAnnotationUtil {

  private ProxyusAnnotationUtil() {
    // hiddenConstructor
  }

  public static boolean hasProxyusAnnotation(Object proxyInstance) {
    return getProxyusAnnotation(proxyInstance).isPresent();
  }

  public static Optional<Proxyus> getProxyusAnnotation(Object proxyInstance) {
    return Optional.ofNullable(proxyInstance.getClass().getAnnotation(Proxyus.class));
  }

  public static Stream<Method> getProxyusBeforeMethodStream(Object proxyInstance) {
    return Arrays.stream(proxyInstance.getClass().getMethods())
        .filter(ProxyusAnnotationUtil::matchBeforeMethod)
        .sorted(ProxyusAnnotationUtil::sortBeforeByOrder);
  }

  public static boolean hasProxyusBeforeAnnotation(Method proxyInstance) {
    return getBeforeAnnotation(proxyInstance).isPresent();
  }

  public static Optional<ProxyusBeforeMethod> getBeforeAnnotation(Method method) {
    return Optional.ofNullable(method.getAnnotation(ProxyusBeforeMethod.class));
  }

  public static boolean matchBeforeMethod(Method method) {
    return getBeforeAnnotation(method).isPresent();
  }

  public static int sortBeforeByOrder(Method method1, Method method2) {
    return Integer.compare(
        getBeforeAnnotation(method1).orElseThrow().runMode().order(),
        getBeforeAnnotation(method2).orElseThrow().runMode().order());
  }

  public static Stream<Method> getProxyusInterceptMethodStream(Object proxyInstance) {
    return Arrays.stream(proxyInstance.getClass().getMethods())
        .filter(ProxyusAnnotationUtil::matchInterceptMethod);
  }

  public static boolean hasProxyusInterceptAnnotation(Method proxyInstance) {
    return getInterceptAnnotation(proxyInstance).isPresent();
  }

  public static Optional<ProxyusInterceptMethod> getInterceptAnnotation(Method method) {
    return Optional.ofNullable(method.getAnnotation(ProxyusInterceptMethod.class));
  }

  public static boolean matchInterceptMethod(Method method) {
    return getInterceptAnnotation(method).isPresent();
  }

  public static Stream<Method> getProxyusAfterMethodStream(Object proxyInstance) {
    return Arrays.stream(proxyInstance.getClass().getMethods())
        .filter(ProxyusAnnotationUtil::matchAfterMethod)
        .sorted(ProxyusAnnotationUtil::sortAfterByOrder);
  }

  public static Stream<Method> getProxiedMethodStream(Class<?> proxiedType) {
    return Arrays.stream(proxiedType.getMethods());
  }

  public static boolean hasProxyusAfterAnnotation(Method proxyInstance) {
    return getAfterAnnotation(proxyInstance).isPresent();
  }

  public static Optional<ProxyusAfterMethod> getAfterAnnotation(Method method) {
    return Optional.ofNullable(method.getAnnotation(ProxyusAfterMethod.class));
  }

  public static boolean matchAfterMethod(Method method) {
    return getAfterAnnotation(method).isPresent();
  }

  public static int sortAfterByOrder(Method method1, Method method2) {
    return Integer.compare(
        getAfterAnnotation(method1).orElseThrow().runMode().order(),
        getAfterAnnotation(method2).orElseThrow().runMode().order());
  }
}
