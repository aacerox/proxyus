package es.acero.proxyus.fixtures;

import es.acero.proxyus.core.annotation.Proxyus;
import es.acero.proxyus.core.annotation.ProxyusAfterMethod;
import es.acero.proxyus.core.annotation.ProxyusBeforeMethod;
import es.acero.proxyus.core.annotation.ProxyusInterceptMethod;
import es.acero.proxyus.core.annotation.ProxyusRunMode;
import es.acero.proxyus.core.method.ProxyusMethod;
import es.acero.proxyus.core.method.ProxyusMethodResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Proxyus(ProxiedInstance.class)
public class ProxyusValidInstance {

  private final Map<String, Integer> callsMap;
  private Map<String, Object> callsResultMap = new HashMap<>();

  public ProxyusValidInstance(Map<String, Integer> callsMap) {
    this.callsMap = callsMap;
  }

  @ProxyusBeforeMethod("sayHello")
  public void beforeSayHello(String name) {
    logTest("beforeSayHello");
  }

  @ProxyusBeforeMethod(value = "sayHello", runMode = @ProxyusRunMode(parallel = false, order = 1))
  public void beforeOneSayHello(String name) {
    logTest("beforeOneSayHello");
  }

  @ProxyusBeforeMethod(value = "sayHello", runMode = @ProxyusRunMode(parallel = false, order = 2))
  public void beforeTwoSayHello(String name) {
    logTest("beforeTwoSayHello");
  }

  @ProxyusInterceptMethod("sayHello")
  public void interceptSayHello(String name, ProxyusMethod sayHelloMethod) throws Throwable {
    logTest("interceptSayHello");
    sayHelloMethod.invoke(name);
  }

  @ProxyusAfterMethod(value = "sayHello", runMode = @ProxyusRunMode(parallel = true))
  public void afterSayHello(String name) {
    logTest("afterSayHello");
  }

  @ProxyusAfterMethod(value = "sayHello", runMode = @ProxyusRunMode(parallel = true))
  public void afterOneSayHello(String name) {
    logTest("afterOneSayHello");
  }

  @ProxyusAfterMethod(value = "sayHello", runMode = @ProxyusRunMode(parallel = true))
  public void afterTwoSayHello(String name) {
    logTest("afterTwoSayHello");
  }

  @ProxyusBeforeMethod("getName")
  public void beforeGetName() {
    logTest("beforeGetName");
  }

  @ProxyusInterceptMethod("getName")
  public String interceptGetName(ProxyusMethod getNameMethod) throws Throwable {
    logTest("interceptGetName");
    return "hello " + getNameMethod.invoke();
  }

  @ProxyusAfterMethod("getName")
  public void afterGetName(ProxyusMethodResult getNameResult) {
    logTest("afterGetName");
    callsResultMap.put("afterGetName", getNameResult.get());
  }

  public Map<String, Object> getCallsResultMap() {
    return callsResultMap;
  }

  private void logTest(String message) {
    logTest(message, () -> {});
  }

  private void logTest(String methodName, Runnable runnable) {
    log("calling %s".formatted(methodName));
    runnable.run();
    callsMap.merge(methodName, 1, (key, value) -> value + 1);
    log("merged %s".formatted(methodName));
  }

  private void log(String message) {
    System.out.println(
        "%s - %s"
            .formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("DD/MM/yyyy-HH:mm:ss.SSS")),
                message));
  }
}
