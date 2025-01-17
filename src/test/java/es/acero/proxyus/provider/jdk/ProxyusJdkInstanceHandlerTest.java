package es.acero.proxyus.provider.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.acero.proxyus.fixtures.ProxiedInstance;
import es.acero.proxyus.fixtures.ProxiedInterface;
import es.acero.proxyus.fixtures.ProxyusValidInstance;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class ProxyusJdkInstanceHandlerTest {

  @Test
  void handle_first_call() throws Throwable {
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusValidInstance proxyusInstance = new ProxyusValidInstance(callsMap);
    ProxiedInterface proxiedInstance = new ProxiedInstance();
    ProxyusJdkInstanceHandler handler = new ProxyusJdkInstanceHandler();

    Object result = testTheMethod(handler, proxyusInstance, proxiedInstance, "sayHello", "a name");
    Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> true);
    // Then
    assertNull(result);
    // Proxyus instance is called
    assertEquals(7, callsMap.size());
    assertEquals(1, callsMap.get("beforeSayHello"));
    assertEquals(1, callsMap.get("beforeOneSayHello"));
    assertEquals(1, callsMap.get("beforeTwoSayHello"));
    assertEquals(1, callsMap.get("interceptSayHello"));
    assertEquals(1, callsMap.get("afterSayHello"));
    assertEquals(1, callsMap.get("afterOneSayHello"));
    assertEquals(1, callsMap.get("afterTwoSayHello"));

    // Proxied instance is called
    assertEquals(1, proxiedInstance.getCallsMap().size());
    assertEquals(1, proxiedInstance.getCallsMap().get("sayHello"));
  }

  @Test
  void handle_cached_call() throws Throwable {
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusValidInstance proxyusInstance = new ProxyusValidInstance(callsMap);
    ProxiedInterface proxiedInstance = new ProxiedInstance();
    ProxyusJdkInstanceHandler handler = new ProxyusJdkInstanceHandler();

    // When
    Object firstResult =
        testTheMethod(handler, proxyusInstance, proxiedInstance, "sayHello", "a name");
    Object secondResult =
        testTheMethod(handler, proxyusInstance, proxiedInstance, "sayHello", "another name");
    Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> true);
    // Then
    assertNull(firstResult);
    assertNull(secondResult);
    // Proxyus instance is called
    assertEquals(7, callsMap.size());
    assertEquals(2, callsMap.get("beforeSayHello"));
    assertEquals(2, callsMap.get("beforeOneSayHello"));
    assertEquals(2, callsMap.get("beforeTwoSayHello"));
    assertEquals(2, callsMap.get("interceptSayHello"));
    assertEquals(2, callsMap.get("afterSayHello"));
    assertEquals(2, callsMap.get("afterOneSayHello"));
    assertEquals(2, callsMap.get("afterTwoSayHello"));
    // Proxied instance is called
    assertEquals(1, proxiedInstance.getCallsMap().size());
    assertEquals(2, proxiedInstance.getCallsMap().get("sayHello"));
  }

  @Test
  void handle_result_call() throws Throwable {
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusValidInstance proxyusInstance = new ProxyusValidInstance(callsMap);
    ProxiedInterface proxiedInstance = new ProxiedInstance();
    ProxyusJdkInstanceHandler handler = new ProxyusJdkInstanceHandler();

    Object result = testTheMethod(handler, proxyusInstance, proxiedInstance, "getName");
    Awaitility.await().atLeast(Duration.ofSeconds(2));
    // Then
    assertNotNull(result);
    assertEquals("hello Alex", result);

    // Proxyus instance is called
    assertEquals(3, callsMap.size());
    assertEquals(1, callsMap.get("beforeGetName"));
    assertEquals(1, callsMap.get("interceptGetName"));
    assertEquals(1, callsMap.get("afterGetName"));
    assertEquals(1, proxyusInstance.getCallsResultMap().size());
    assertEquals(
        Optional.of("hello Alex"), proxyusInstance.getCallsResultMap().get("afterGetName"));

    // Proxied instance is called
    assertEquals(1, proxiedInstance.getCallsMap().size());
    assertEquals(1, proxiedInstance.getCallsMap().get("getName"));
  }

  private Object testTheMethod(
      ProxyusJdkInstanceHandler handler,
      ProxyusValidInstance proxyusInstance,
      ProxiedInterface proxiedInstance,
      String name,
      Object... args)
      throws Throwable {

    // When
    return handler.invoke(proxyusInstance, proxiedInstance, getMethod(name), args);
  }

  private Method getMethod(String methodName) {
    return Arrays.stream(ProxiedInstance.class.getDeclaredMethods())
        .filter(method -> method.getName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }
}
