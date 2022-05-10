/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.extapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageViewerService {

  private final ExtAppConfigProperties extAppConfigProperties;

  public void openInViewer(File imageFile) {
    var processBuilder = new ProcessBuilder(
        extAppConfigProperties.getImageViewer(),
        imageFile.getPath());
    try {
      Files.createDirectories(imageFile.getParentFile().toPath());
      processBuilder.start();
    } catch (IOException e) {
      log.error("Cannot view file " + imageFile, e);
      e.printStackTrace();
    }
  }
}
