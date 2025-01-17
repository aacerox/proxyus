package es.acero.proxyus;

public class GivenWhenThen {

  public static <G> WhenBuilder<G> given(String description, TestGivenStage<G> givenStage)
      throws Exception {
    System.out.println("Given ".concat(description));
    return new WhenBuilder<>(givenStage.get());
  }

  public static final class WhenBuilder<G> {

    private final G givenValue;

    private WhenBuilder(G givenValue) {
      this.givenValue = givenValue;
    }

    public <W> ThenBuilder<W> when(String description, TestWhenStage<G, W> whenStage)
        throws Exception {
      System.out.println("When ".concat(description));
      return new ThenBuilder<>(whenStage.applyWhen(this.givenValue));
    }
  }

  public static final class ThenBuilder<W> {

    private final W whenValue;

    public ThenBuilder(W whenValue) {
      this.whenValue = whenValue;
    }

    public void then(String description, TestThenStage<W> thenStage) throws Exception {
      System.out.println("Then ".concat(description));
      thenStage.applyThen(this.whenValue);
    }
  }

  @FunctionalInterface
  public interface TestGivenStage<G> {
    public G get() throws Exception;
  }

  @FunctionalInterface
  public interface TestWhenStage<G, W> {
    public W applyWhen(G givenValue) throws Exception;
  }

  @FunctionalInterface
  public interface TestThenStage<W> {
    public void applyThen(W actualValue) throws Exception;
  }
}
