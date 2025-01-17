package es.acero.proxyus.core.validator;

import java.lang.reflect.Method;

@FunctionalInterface
public interface ProxyusMethodValidationRule {

  public ProxyusValidationResult validate(Method method);
}
