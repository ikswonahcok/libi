/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.extapp;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GraphVizService {

  public boolean dotGenPng(File file) {
    ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", "-O",
        file.getPath());
    try {
      processBuilder.start().waitFor();
    } catch (Exception e) {
      log.error("Cannot generate png file " + file.getPath(), e);
      return false;
    }
    return true;
  }
}
