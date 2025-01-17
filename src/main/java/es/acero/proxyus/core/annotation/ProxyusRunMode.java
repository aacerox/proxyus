package es.acero.proxyus.core.annotation;

public @interface ProxyusRunMode {
  boolean parallel() default false;

  int order() default 0;
}
