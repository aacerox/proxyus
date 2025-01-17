package es.acero.proxyus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GivenWhenThenTest {

  @DisplayName("say hello when new person passed")
  @Test
  void test_given_when_with_return_value() throws Exception {

    GivenWhenThen.given("A new person name", NameFixture::newName)
        .when(
            "add a hello prefix",
            newName -> "Hello, %s %s".formatted(newName.name(), newName.surname()))
        .then(
            "a salute is created",
            actualSalute -> {
              assertEquals("Hello, Alex Alvarez", actualSalute);
            });
  }

  @Test
  void test_given_when_with_no_return_value() throws Exception {

    GivenWhenThen.given("A new name", NameFixture::newName)
        .when(
            "print name",
            name -> {
              SayHelloService sayHelloService = new SayHelloService();
              sayHelloService.sayHello(name);
              return sayHelloService;
            })
        .then(
            "a salute is called",
            service -> {
              assertTrue(service.isSaluted());
            });
  }

  private static final record PersonName(String name, String surname) {}

  private static final class NameFixture {

    private static PersonName newName() {
      return new PersonName("Alex", "Alvarez");
    }
  }

  private static final class SayHelloService {

    private boolean isSaluted;

    public void sayHello(PersonName name) {
      System.out.println("Hello, ".concat(name.toString()));
      isSaluted = true;
    }

    public boolean isSaluted() {
      return this.isSaluted;
    }
  }
}
