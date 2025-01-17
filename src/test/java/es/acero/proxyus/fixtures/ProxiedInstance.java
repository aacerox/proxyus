package es.acero.proxyus.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxiedInstance implements ProxiedInterface {

  private Map<String, Integer> callsMap = new HashMap<>();

  @Override
  public void sayHello(String name) {
    System.out.println("Hello %s".formatted(name));
    // add am element to calls map if not exists with value 1 or increment the value if exists
    callsMap.merge("sayHello", 1, (key, value) -> value + 1);
  }

  @Override
  public String getName() {
    System.out.println("calling getName");
    callsMap.merge("getName", 1, (key, value) -> value + 1);
    return "Alex";
  }

  @Override
  public List<String> spell(String... words) {
    System.out.println("calling spell");
    List<String> result = new ArrayList<>();
    callsMap.merge("spell", 1, (key, value) -> value + 1);
    result.addAll(Arrays.stream(words).toList());
    return result;
  }

  @Override
  public Map<String, Integer> getCallsMap() {
    return callsMap;
  }
}
