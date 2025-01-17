package es.acero.proxyus.core.validator;

import es.acero.proxyus.core.annotation.ProxyusAfterMethod;
import es.acero.proxyus.core.annotation.ProxyusBeforeMethod;
import es.acero.proxyus.core.annotation.ProxyusInterceptMethod;
import es.acero.proxyus.core.util.ProxyusAnnotationUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ProxyusInstanceValidator {

  private final Object proxyInstance;

  public ProxyusInstanceValidator(Object proxyInstance) {
    super();
    this.proxyInstance = proxyInstance;
  }

  public Object validate() {

    return new ProxyusValidatorBuilder()
        // check that a @Proxyus annotation is present
        .addInstanceRule(ProxyusValidationRules::checkProxyusAnnotationPresent)
        // check that at least one @ProxyusMethod annotation is present
        .addInstanceRule(ProxyusValidationRules::checkProxyusMethodAnnotationPresent)
        // check that there's only one intercept annotation per method name
        .addInstanceRule(ProxyusValidationRules::checkOneInterceptByTargetMethod)
        .addBeforeMethodRule(
            ProxyusValidationRules.checkArgsMatchProxied(
                this.proxyInstance,
                method ->
                    ProxyusAnnotationUtil.getBeforeAnnotation(method)
                        .map(ProxyusBeforeMethod::value)
                        .orElseThrow()))
        // find @ProxyusBefore annotation and check that method are void
        .addBeforeMethodRule(ProxyusValidationRules::checkVoidReturnOnMethod)
        /*
         * find @ProxyusIntercept annotation and check:
         * - that last argument is of type ProxyusMethod
         * - that method return expected type according to proxied class method
         * - that method signature match the target proxied method signature
         */
        .addInterceptMethodRule(ProxyusValidationRules::checkMethodInterceptLastArgType)
        .addInterceptMethodRule(
            ProxyusValidationRules.checkReturnValueMatchExpected(this.proxyInstance))
        .addInterceptMethodRule(
            ProxyusValidationRules.checkArgsMatchProxied(
                this.proxyInstance,
                method ->
                    ProxyusAnnotationUtil.getInterceptAnnotation(method)
                        .map(ProxyusInterceptMethod::value)
                        .orElseThrow()))

        // Check that method signature match the target proxied method signature
        .addAfterMethodRule(
            ProxyusValidationRules.checkArgsMatchProxied(
                this.proxyInstance,
                method ->
                    ProxyusAnnotationUtil.getAfterAnnotation(method)
                        .map(ProxyusAfterMethod::value)
                        .orElseThrow()))
        // find @ProxyusAfter annotation and check that method are void
        .addAfterMethodRule(ProxyusValidationRules::checkVoidReturnOnMethod)
        .validate(proxyInstance);
  }

  private class ProxyusValidatorBuilder {
    private final List<ProxyusInstanceValidationRule> instanceRules = new ArrayList<>();
    private Map<ProxyusMethodType, List<ProxyusMethodValidationRule>> methodRules =
        new EnumMap<>(ProxyusMethodType.class);

    public enum ProxyusMethodType {
      BEFORE,
      INTERCEPT,
      AFTER;
    }

    public ProxyusValidatorBuilder addInstanceRule(ProxyusInstanceValidationRule validationRule) {
      this.instanceRules.add(validationRule);
      return this;
    }

    public ProxyusValidatorBuilder addBeforeMethodRule(ProxyusMethodValidationRule validationRule) {
      methodRules.compute(
          ProxyusMethodType.BEFORE, this.remappingMethodValidationRule(validationRule));

      return this;
    }

    public ProxyusValidatorBuilder addInterceptMethodRule(
        ProxyusMethodValidationRule validationRule) {
      methodRules.compute(
          ProxyusMethodType.INTERCEPT, this.remappingMethodValidationRule(validationRule));

      return this;
    }

    public ProxyusValidatorBuilder addAfterMethodRule(ProxyusMethodValidationRule validationRule) {
      methodRules.compute(
          ProxyusMethodType.AFTER, this.remappingMethodValidationRule(validationRule));

      return this;
    }

    public Object validate(Object proxyInstance) {
      // validate instance rules
      this.instanceRules.forEach(rule -> rule.validate(proxyInstance).throwWhenFail(proxyInstance));

      // validate before method rules
      validateMethodRules(
          ProxyusAnnotationUtil.getProxyusBeforeMethodStream(proxyInstance),
          ProxyusMethodType.BEFORE);

      // validate intercept method rules
      validateMethodRules(
          ProxyusAnnotationUtil.getProxyusInterceptMethodStream(proxyInstance),
          ProxyusMethodType.INTERCEPT);

      // validate after method rules
      validateMethodRules(
          ProxyusAnnotationUtil.getProxyusAfterMethodStream(proxyInstance),
          ProxyusMethodType.AFTER);

      return proxyInstance;
    }

    private void validateMethodRules(Stream<Method> methodStream, ProxyusMethodType methodType) {
      methodStream.forEach(
          method ->
              this.methodRules
                  .get(methodType)
                  .forEach(rule -> rule.validate(method).throwWhenFail(proxyInstance)));
    }

    private BiFunction<
            ProxyusMethodType, List<ProxyusMethodValidationRule>, List<ProxyusMethodValidationRule>>
        remappingMethodValidationRule(ProxyusMethodValidationRule validationRule) {
      return (key, value) -> {
        List<ProxyusMethodValidationRule> result = value == null ? new ArrayList<>() : value;
        result.add(validationRule);
        return result;
      };
    }
  }
}
