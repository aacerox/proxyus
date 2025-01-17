package es.acero.proxyus.core.validator;

@FunctionalInterface
public interface ProxyusInstanceValidationRule {

  public ProxyusValidationResult validate(Object proxyInstance);
}
