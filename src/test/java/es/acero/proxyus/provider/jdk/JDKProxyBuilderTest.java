package es.acero.proxyus.provider.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import es.acero.proxyus.Proxyus;
import es.acero.proxyus.core.handler.AbstractProxyusMethodHandler;
import es.acero.proxyus.core.method.ProxyusMethodSignature;

class JDKProxyBuilderTest {

  @Test
  void invoke_proxied_method() throws Exception {

    Map<String, String> target = new HashMap<>();

    Map<String, String> proxied =
        Proxyus.useProxyBuilder(ProxyusJdkBuilder::new)
            .forInstance(target)
            .addMethodHandler(new MapGetMethodHandler())
            .build();

    assertEquals("proxied", proxied.get("any"));
  }

  @Test
  void invoke_non_proxied_method() throws Exception {

    Map<String, String> target = new HashMap<>();

    Map<String, String> proxied =
        Proxyus.useProxyBuilder(ProxyusJdkBuilder::new)
            .forInstance(target)
            .addMethodHandler(new MapPutMethodHandler())
            .build();
    proxied.put("one", "value one");
    proxied.put("two", "value two");

    assertEquals("proxied", proxied.get("one"));
    assertEquals("proxied", proxied.get("two"));
  }

  private static final class MapGetMethodHandler extends AbstractProxyusMethodHandler<Map<String, String>> {

    protected MapGetMethodHandler() throws NoSuchMethodException, SecurityException {
      super(ProxyusMethodSignature.of("get").withParameter(Object.class).build(Map.class));
    }

    @Override
    public Object invoke(Map<String, String> targetInstance, Object[] args) throws Throwable {

      return "proxied";
    }
  }

  private static final class MapPutMethodHandler extends AbstractProxyusMethodHandler<Map<String, String>> {

    protected MapPutMethodHandler() throws NoSuchMethodException, SecurityException {
      super(
          ProxyusMethodSignature.of("put")
              .withParameter(Object.class)
              .withParameter(Object.class)
              .build(Map.class));
    }

    @Override
    public Object invoke(Map<String, String> targetInstance, Object[] args) throws Throwable {

      return targetInstance.put((String) args[0], "proxied");
    }
  }
}
