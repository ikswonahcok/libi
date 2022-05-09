/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import java.util.Comparator;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.github.libi.services.libiel.LibiELVisitorCtx;
import org.github.libi.services.libiel.VerticeSet;

@Builder
@Data
public class VSFunction {

  private final VSFunctionSignature signature;

  private final Function function;

  private final String description;

  public VerticeSet call(LibiELVisitorCtx ctx, List<FunctionArgument> arguments) {
    return function.call(ctx, arguments);
  }

  public interface Function {

    VerticeSet call(LibiELVisitorCtx ctx, List<FunctionArgument> args);
  }

  public static Comparator<VSFunction> comparator() {
    Comparator<VSFunction> comparator = Comparator.comparing(
        f -> f.getSignature().getName());
    comparator = comparator.
        thenComparingInt(
            f -> f.getSignature().getArguments().size()
        );
    comparator = comparator
        .thenComparing((f1, f2) -> {
          for (int i = 0; i < f1.getSignature().getArguments().size(); i++) {
            if (f1.getSignature().getArguments().get(i) == ArgumentType.VS
                && f2.getSignature().getArguments().get(i) != ArgumentType.VS) {
              return -1;
            }
            if (f1.getSignature().getArguments().get(i) != ArgumentType.VS
                && f2.getSignature().getArguments().get(i) == ArgumentType.VS) {
              return 1;
            }
          }
          return 0;
        });
    return comparator;
  }
}
