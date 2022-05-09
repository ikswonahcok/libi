/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.github.libi.services.libiel.functions.ArgumentType;
import org.github.libi.services.libiel.functions.FunctionArgumentString;
import org.github.libi.services.libiel.functions.FunctionArgumentVS;
import org.github.libi.services.libiel.functions.VSFunctionSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LibiELBeanPostProcessor implements BeanPostProcessor {

  private final LibiELEnv libiELEnv;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean.getClass().isAnnotationPresent(LibiELComponent.class)) {
      addFunctions(bean);
    }
    return bean;
  }

  public void addFunctions(Object bean) {
    for (var method : bean.getClass().getMethods()) {
      var annotation = AnnotationUtils.findAnnotation(method, LibiELFunction.class);
      if (annotation != null) {
        var description = annotation.value();
        createVSFunctionSignature(method).ifPresent(signature -> {
          libiELEnv.getVsFunctions().add(signature, (ctx, args) -> {
            Object[] functionArgs = new Object[args.size() + 1];
            functionArgs[0] = ctx;
            for (int i = 1; i < functionArgs.length; i++) {
              var arg = args.get(i - 1);
              if (arg instanceof FunctionArgumentString) {
                functionArgs[i] = ((FunctionArgumentString) arg).getValue();
              } else if (arg instanceof FunctionArgumentVS) {
                functionArgs[i] = ((FunctionArgumentVS) arg).getValue();
              }
            }
            try {
              return (VerticeSet) method.invoke(bean, functionArgs);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
              throw new RuntimeException(e);
            }
          }, description);
        });
      }
    }
  }

  private Optional<VSFunctionSignature> createVSFunctionSignature(Method method) {
    if (method.getParameterCount() < 1) {
      return Optional.empty();
    }
    if (!VerticeSet.class.isAssignableFrom(method.getReturnType())) {
      return Optional.empty();
    }
    var builder = VSFunctionSignature.builder();
    builder.name(method.getName());
    var parameters = method.getParameterTypes();
    if (!LibiELVisitorCtx.class.isAssignableFrom(parameters[0])) {
      return Optional.empty();
    }
    for (int i = 1; i < method.getParameterCount(); i++) {
      var argType = parameters[i];
      if (String.class.isAssignableFrom(parameters[i])) {
        builder.argument(ArgumentType.STRING);
      } else if (VerticeSet.class.isAssignableFrom(parameters[i])) {
        builder.argument(ArgumentType.VS);
      } else {
        return Optional.empty();
      }
    }
    return Optional.of(builder.build());
  }
}
