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
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.github.libi.services.extapp.ExtAppConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradlewService {

  private final ExtAppConfigProperties extAppConfigProperties;

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

  public Map<String, String> getProperties(Project project) {
    ProcessBuilder processBuilder;
    if (project.isRoot()) {
      processBuilder = new ProcessBuilder(getGradlewCmd(), "properties").directory(
          project.getDir());
    } else {
      processBuilder = new ProcessBuilder(getGradlewCmd(), project.getName() + ":properties").directory(
          project.getDir());
    }
    Process process;
    try {
      process = processBuilder.start();
      try (var bis = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        return bis.lines()
            .map(l -> l.split(": ", 2))
            .filter(t -> t.length == 2)
            .collect(Collectors.toMap(t -> t[0], t -> t[1], (o1, o2) -> o2));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Collections.emptyMap();
  }

  protected String getGradlewCmd() {
    return extAppConfigProperties.getGradlew();
  }
}
