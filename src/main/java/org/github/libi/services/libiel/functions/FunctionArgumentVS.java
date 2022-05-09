/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.github.libi.services.libiel.VerticeSet;

@RequiredArgsConstructor
@Getter
public class FunctionArgumentVS implements FunctionArgument {

  private final VerticeSet value;

  @Override
  public ArgumentType getType() {
    return ArgumentType.VS;
  }
}
