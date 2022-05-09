/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GradlewService {

  private String gradlewCmd;

  @Value("${libi.extapp.gradlew}")
  public void setGradlewCmd(String gradlewCmd) {
    this.gradlewCmd = gradlewCmd;
  }

  public void processCommandOutput(File dir, Consumer<Stream<String>> consumer,
      String... command) {
    ProcessBuilder processBuilder = new ProcessBuilder(command).directory(dir);
    Process process;
    try {
      process = processBuilder.start();
      try (var bis = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        consumer.accept(bis.lines());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void processGradlewProjectsOutput(File dir, Consumer<Stream<String>> consumer) {
    processCommandOutput(dir, consumer, getGradlewCmd(), "projects");
  }

  public void processGradlewRootDependencies(File dir, Consumer<Stream<String>> consumer) {
    processCommandOutput(dir, consumer, getGradlewCmd(), "dependencies");
  }

  public void processGradlewDependencies(Project project, Consumer<Stream<String>> consumer) {
    if (project.isRoot()) {
      processGradlewRootDependencies(project.getDir(), consumer);
    } else {
      processCommandOutput(project.getDir(), consumer, getGradlewCmd(), project.getName() + ":dependencies");
    }
  }

  protected String getGradlewCmd() {
    return gradlewCmd;
  }
}
