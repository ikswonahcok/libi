/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.github.libi.services.libiel.LibiELService;
import org.github.libi.services.libiel.LibiELVisitorCtx;
import org.github.libi.services.libiel.VerticeSet;

@RequiredArgsConstructor
public class VSExprFunction implements VSFunction.Function {

  private final String expr;

  @Override
  public VerticeSet call(LibiELVisitorCtx ctx, List<FunctionArgument> args) {
    return LibiELService.filter(ctx.getEnv(), ctx.getGraph(), ctx.getMainGraph(), expr);
  }
}
