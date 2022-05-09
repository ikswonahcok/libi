/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import lombok.Getter;
import org.github.libi.services.libiel.functions.VSFunctions;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LibiELEnv {

  private final VSFunctions vsFunctions = new VSFunctions();
}
