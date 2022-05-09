/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.github.libi.cmdtree.GradleCommandTreeReader;

@Getter
@Setter
public class ProjectsTreeLineListener implements GradleCommandTreeReader.NewLineListener {

  private static final String LINE_START_FOR_ROOT_PROJECT = "Root project '";

  private static final String LINE_START_FOR_SUBPROJECT = "Project '";

  private final Deque<Project> projectStack = new LinkedList<>();

  private final List<Project> projects = new LinkedList<>();

  @Override
  public void onNewLine(int treeLevel, String text) {
    getProjectFromLine(text, treeLevel)
        .ifPresent(projects::add);
  }

  public Optional<Project> getProjectFromLine(String line, int treeLevel) {
    if (projectStack.isEmpty()) {
      if (line.startsWith(LINE_START_FOR_ROOT_PROJECT)) {
        var endIndex = line.indexOf("'", LINE_START_FOR_ROOT_PROJECT.length());
        if (endIndex < 0) {
          return Optional.empty();
        }
        var projectName = line.substring(LINE_START_FOR_ROOT_PROJECT.length(), endIndex);
        var project = new Project(projectName, null);
        projectStack.offer(project);
        return Optional.of(project);
      } else {
        return Optional.empty();
      }
    } else {
      if (!line.startsWith(LINE_START_FOR_SUBPROJECT)) {
        return Optional.empty();
      }
      var endIndex = line.indexOf("'", LINE_START_FOR_SUBPROJECT.length());
      if (endIndex < 0) {
        return Optional.empty();
      }
      var projectName = line.substring(LINE_START_FOR_SUBPROJECT.length(), endIndex);
      while (projectStack.size() > treeLevel) {
        projectStack.pollLast();
      }
      var project = new Project(projectName, projectStack.peekLast());
      projectStack.offer(project);
      return Optional.of(project);
    }
  }
}
