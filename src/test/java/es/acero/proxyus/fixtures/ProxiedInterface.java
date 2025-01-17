package es.acero.proxyus.fixtures;

import java.util.List;
import java.util.Map;

public interface ProxiedInterface {

    void sayHello(String name);

    String getName();

    List<String> spell(String... words);

    Map<String, Integer> getCallsMap();

}