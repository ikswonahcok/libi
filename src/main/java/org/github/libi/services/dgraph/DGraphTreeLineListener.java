/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.github.libi.cmdtree.GradleCommandTreeReader;

@Getter
@Setter
@RequiredArgsConstructor
public class DGraphTreeLineListener implements GradleCommandTreeReader.NewLineListener {

  private static final Map<String, DependencyCategory> LINE_START_2_CATEGORY = Map.of(
      "compileClasspath", DependencyCategory.COMPILE_CLASSPATH,
      "runtimeClasspath", DependencyCategory.RUNTIME_CLASSPATH
  );

  private final DependencyGraph graph;

  private final Deque<Artifact> artifactStack = new LinkedList<>();

  private final Artifact projectArtifact;

  private DependencyCategory currentCategory = null;

  private Map<String, Artifact> projectName2ArtifactDict;

  @Override
  public void onNewLine(int treeLevel, String text) {
    if (treeLevel == 0) {
      currentCategory = Optional.ofNullable(text.split(" - "))
          .filter(tokens -> tokens.length == 2)
          .map(tokens -> tokens[0])
          .map(LINE_START_2_CATEGORY::get)
          .orElse(null);
      clearStack(1);
    } else if (currentCategory != null) {
      clearStack(treeLevel);
      if (text.startsWith("project ")) {
        var artifact = projectName2ArtifactDict.get(text.substring(8).split(" ")[0]);
        addArtifact(artifact);
      } else {
        Optional.ofNullable(text.split("[: ]"))
            .filter(tokens -> tokens.length >= 2)
            .map(tokens -> new Artifact(tokens[0], tokens[1]))
            .ifPresent(this::addArtifact);
      }
    }
  }

  public void addArtifact(Artifact artifact) {
    if (artifactStack.isEmpty()) {
      graph.addArtifact(artifact);
    } else {
      var dependency = graph.addDependency(artifactStack.peekLast(), artifact);
      switch (currentCategory) {
        case COMPILE_CLASSPATH:
          projectArtifact.getCompileDependencies().add(artifact.getId());
          break;
        case RUNTIME_CLASSPATH:
          projectArtifact.getRuntimeDependencies().add(artifact.getId());
          break;
      }
    }
    artifactStack.offer(artifact);
  }

  protected void clearStack(int level) {
    while (artifactStack.size() > level) {
      artifactStack.pollLast();
    }
  }
}
