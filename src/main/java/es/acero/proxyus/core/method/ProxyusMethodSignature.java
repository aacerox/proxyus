package es.acero.proxyus.core.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import es.acero.proxyus.core.method.ProxyusMethodSignature.ProxyMethodSignatureBuilder.ProxyusMethodParameter;

public final class ProxyusMethodSignature {

  private final String methodName;
  private final List<ProxyusMethodParameter> parameters;

  public static ProxyusMethodSignature of(Method method) {
    List<ProxyusMethodParameter> methodParams =
        Arrays.stream(method.getParameters())
            .map(
                reflectParam ->
                    new ProxyusMethodParameter(reflectParam.getName(), reflectParam.getType()))
            .toList();
    return new ProxyusMethodSignature(method.getName(), methodParams);
  }

  public static ProxyMethodSignatureBuilder of(String methodName) {
    return new ProxyMethodSignatureBuilder(methodName);
  }

  private ProxyusMethodSignature(String name, List<ProxyusMethodParameter> parameters) {
    this.methodName = name;
    this.parameters = parameters;
  }

  @Override
  public int hashCode() {

    String parametersLiteral =
        parameters.stream().map(ProxyusMethodParameter::toString).collect(Collectors.joining("#"));

    return Objects.hash(this.methodName, parametersLiteral);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ProxyusMethodSignature other = (ProxyusMethodSignature) obj;
    return Objects.equals(methodName, other.methodName)
        && Objects.equals(parameters, other.parameters);
  }

  public static final class ProxyMethodSignatureBuilder {

    private final String methodName;
    private final List<ProxyusMethodParameter> parameters;

    public ProxyMethodSignatureBuilder(String methodName) {
      super();
      this.methodName = methodName;
      this.parameters = new ArrayList<>();
    }

    public ProxyMethodSignatureBuilder withParameter(Class<?> type) {
      this.parameters.add(new ProxyusMethodParameter("arg%d".formatted(parameters.size()), type));

      return this;
    }

    public ProxyusMethodSignature build(Class<?> targetType)
        throws NoSuchMethodException, SecurityException {
      // check that method exists in target instance
      targetType.getMethod(
          this.methodName,
          parameters.stream()
              .map(ProxyusMethodParameter::type)
              .toArray(count -> new Class<?>[count]));
      return new ProxyusMethodSignature(this.methodName, this.parameters);
    }

    protected static final record ProxyusMethodParameter(String name, Class<?> type) {

      @Override
      public String toString() {
        return name + "[" + type.getName() + "]";
      }
    }
  }
}
