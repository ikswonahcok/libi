/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.io.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Project {

  private final String name;

  private final Project parent;

  private String artifactGroup;

  private String artifactName;

  private File dir;

  public boolean isRoot() {
    return parent == null;
  }
}
