/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VSFunctions {

  private final Map<VSFunctionSignature, VSFunction> functions = new HashMap<>();

  public void add(VSFunction function) {
    functions.put(function.getSignature(), function);
  }

  public void add(VSFunctionSignature signature, VSFunction.Function function, String description) {
    add(VSFunction.builder()
        .signature(signature)
        .function(function)
        .description(description)
        .build());
  }

  public VSFunction get(VSFunctionSignature signature) {
    return functions.get(signature);
  }

  public void addExpressionFunction(String name, String expr) {
    add(
        VSFunctionSignature.builder()
            .name(name)
            .build(),
        new VSExprFunction(expr),
        "expression \"" + expr + "\"");
  }

  public Collection<VSFunction> getFunctions() {
    return functions.values();
  }
}
