package es.acero.proxyus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.acero.proxyus.core.handler.AbstractProxyusMethodHandler;
import es.acero.proxyus.core.method.ProxyusMethodSignature;
import es.acero.proxyus.fixtures.ProxiedInstance;
import es.acero.proxyus.fixtures.ProxiedInterface;
import es.acero.proxyus.fixtures.ProxyusValidInstance;
import es.acero.proxyus.provider.jdk.ProxyusJdkBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class ProxyusTest {

  @Test
  void proxy_with_method_handler() throws Exception {
    ProxiedInterface proxiedInstance = new ProxiedInstance();

    ProxiedInterface proxy =
        Proxyus.useProxyBuilder(ProxyusJdkBuilder::new)
            .forInstance(proxiedInstance)
            .addMethodHandler(new SpellMethodHandler())
            .build();

    assertEquals(List.of("hello", "world", "alex"), proxy.spell("hello", "world"));
  }

  @Test
  void proxy_with_instance_handler() throws Exception {
    ProxiedInterface proxiedInstance = new ProxiedInstance();
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusValidInstance proxyusInstance = new ProxyusValidInstance(callsMap);

    ProxiedInterface proxy =
        Proxyus.useProxyBuilder(ProxyusJdkBuilder::new)
            .forInstance(proxiedInstance)
            .setProxyInstance(proxyusInstance)
            .build();

    proxy.sayHello("alex");

    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> callsMap.size() == 7);

    // Proxyus instance is called
    System.out.println(callsMap.toString());
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
  void error_when_both_handlers_applied() throws Exception {

    ProxiedInterface proxiedInstance = new ProxiedInstance();
    Map<String, Integer> callsMap = new HashMap<>();
    ProxyusValidInstance proxyusInstance = new ProxyusValidInstance(callsMap);

    assertThrows(
        IllegalStateException.class,
        () ->
            Proxyus.useProxyBuilder(ProxyusJdkBuilder::new)
                .forInstance(proxiedInstance)
                .addMethodHandler(new SpellMethodHandler())
                .setProxyInstance(proxyusInstance)
                .build());
  }

  private static final class SpellMethodHandler
      extends AbstractProxyusMethodHandler<ProxiedInterface> {

    protected SpellMethodHandler() throws NoSuchMethodException, SecurityException {
      super(
          ProxyusMethodSignature.of("spell")
              .withParameter(String[].class)
              .build(ProxiedInterface.class));
    }

    @Override
    public Object invoke(ProxiedInterface targetInstance, Object[] args) throws Throwable {
      // Arrays.stream(args).map(String.class::cast).toArray(String[]::new)
      List<String> spells = targetInstance.spell((String[]) args[0]);
      spells.add("alex");
      return spells;
    }
  }
}
