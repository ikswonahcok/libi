/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FunctionArgumentString implements FunctionArgument {

  private final String value;

  @Override
  public ArgumentType getType() {
    return ArgumentType.STRING;
  }
}
