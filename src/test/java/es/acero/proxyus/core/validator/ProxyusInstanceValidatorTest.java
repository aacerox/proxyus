package es.acero.proxyus.core.validator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.acero.proxyus.core.annotation.Proxyus;
import es.acero.proxyus.core.annotation.ProxyusAfterMethod;
import es.acero.proxyus.core.annotation.ProxyusBeforeMethod;
import es.acero.proxyus.core.annotation.ProxyusInterceptMethod;
import es.acero.proxyus.core.method.ProxyusMethod;
import es.acero.proxyus.fixtures.ProxiedInstance;
import es.acero.proxyus.fixtures.ProxyusValidInstance;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProxyusInstanceValidatorTest {

  @Test
  void proxyus_valid_instance() throws Exception {
    // Given
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new ProxyusValidInstance(callsMap));

    // When
    Object proxyusValidInstance = proxyusInstanceValidator.validate();

    // Then
    assertNotNull(proxyusValidInstance);
  }

  @Test
  void no_proxyus_annnotation_present() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new NoProxyusAnnotation());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void only_proxyus_annotation_present() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new OnlyProxyusAnnotation());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void multiple_intercept_annotation_same_method_present() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new MultipleInterceptAnnotation());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void before_method_not_match_args() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new BeforeMethodNotMatchDifferentArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);

    proxyusInstanceValidator = new ProxyusInstanceValidator(new BeforeMethodNotMatchMultipleArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void before_method_not_return_void() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new BeforeMethodNotReturnVoid());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void intercept_method_not_match_args() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new InterceptMethodNotMatchDifferentArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);

    proxyusInstanceValidator =
        new ProxyusInstanceValidator(new InterceptMethodNotMatchMultipleArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void intercept_method_not_match_return_type() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new InterceptMethodNotMatchReturnType());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void intercept_method_without_proxyMethod_last_argument() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new InterceptMethodNotMatchReturnType());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void after_method_not_match_args() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new AfterMethodNotMatchDifferentArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);

    proxyusInstanceValidator = new ProxyusInstanceValidator(new AfterMethodNotMatchMultipleArgs());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  @Test
  void after_method_not_return_void() throws Exception {
    // Given
    ProxyusInstanceValidator proxyusInstanceValidator =
        new ProxyusInstanceValidator(new AfterMethodNotReturnVoid());

    // When, then
    assertThrows(UnsupportedOperationException.class, proxyusInstanceValidator::validate);
  }

  private static final class NoProxyusAnnotation {}

  @Proxyus(ProxiedInstance.class)
  private static final class OnlyProxyusAnnotation {}

  @Proxyus(ProxiedInstance.class)
  private static final class MultipleInterceptAnnotation {

    @ProxyusInterceptMethod("sayHello")
    public void interceptSayHello(String name, ProxyusMethod sayHelloMethod) {}

    @ProxyusInterceptMethod("sayHello")
    public void anotherInterceptSayHello(String name, ProxyusMethod sayHelloMethod) {}
  }

  @Proxyus(ProxiedInstance.class)
  private static final class BeforeMethodNotMatchMultipleArgs {
    @ProxyusBeforeMethod("getName")
    public void beforeGetName(String name, int age) {}
  }

  @Proxyus(ProxiedInstance.class)
  private static final class BeforeMethodNotMatchDifferentArgs {
    @ProxyusBeforeMethod("getName")
    public void beforeGetName(int age) {}
  }

  @Proxyus(ProxiedInstance.class)
  private static final class BeforeMethodNotReturnVoid {
    @ProxyusBeforeMethod("getName")
    public String beforeGetName(String name) {
      return "hello";
    }
  }

  @Proxyus(ProxiedInstance.class)
  private static final class InterceptMethodNotMatchDifferentArgs {
    @ProxyusInterceptMethod("getName")
    public String interceptGetName(int age, ProxyusMethod getNameMethod) {
      return "hello";
    }
  }

  @Proxyus(ProxiedInstance.class)
  private static final class InterceptMethodNotMatchMultipleArgs {
    @ProxyusInterceptMethod("getName")
    public String interceptGetName(String name, int age, ProxyusMethod getNameMethod) {
      return "hello";
    }
  }

  @Proxyus(ProxiedInstance.class)
  private static final class InterceptMethodNotMatchReturnType {

    @ProxyusInterceptMethod("getName")
    public int interceptGetName(String name, ProxyusMethod getNameMethod) {
      return 1;
    }
  }

  @Proxyus(ProxiedInstance.class)
  private static final class AfterMethodNotMatchMultipleArgs {
    @ProxyusAfterMethod("getName")
    public void afterGetName(String name, int age) {}
  }

  @Proxyus(ProxiedInstance.class)
  private static final class AfterMethodNotMatchDifferentArgs {
    @ProxyusAfterMethod("getName")
    public void afterGetName(int age) {}
  }

  @Proxyus(ProxiedInstance.class)
  private static final class AfterMethodNotReturnVoid {
    @ProxyusAfterMethod("getName")
    public String afterGetName(String name) {
      return "hello";
    }
  }
}
