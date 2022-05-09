/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel.functions;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder
@Data
public class VSFunctionSignature {
  private final String name;

  @Singular
  private final List<ArgumentType> arguments;
}
