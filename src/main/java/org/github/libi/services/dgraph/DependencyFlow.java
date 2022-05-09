/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jgrapht.Graph;

@Getter
@Setter
@RequiredArgsConstructor
public class DependencyFlow {
  private final Graph<Artifact, Dependency> graph;

  public DependencyFlow(DependencyGraph graph) {
    this.graph = graph.getGraph();
  }

  public Set<Artifact> flowOutgoing(Collection<Artifact> artifacts, Predicate<Artifact> stopCondition) {
    Set<Artifact> visited = new HashSet<>();
    Set<Artifact> toCheck = new HashSet<>();
    toCheck.addAll(artifacts);
    while (!toCheck.isEmpty()) {
      var current = toCheck.iterator().next();
      toCheck.remove(current);
      visited.add(current);
      graph.outgoingEdgesOf(current).stream()
          .map(graph::getEdgeTarget)
          .filter(Predicate.not(stopCondition))
          .filter(Predicate.not(visited::contains))
          .forEach(toCheck::add);
    }
    return visited;
  }

  public Set<Artifact> directOutgoing(Collection<Artifact> artifacts, Predicate<Artifact> stopCondition) {
    Set<Artifact> visited = new HashSet<>();
    Set<Artifact> toCheck = new HashSet<>();
    toCheck.addAll(artifacts);
    while (!toCheck.isEmpty()) {
      var current = toCheck.iterator().next();
      toCheck.remove(current);
      visited.add(current);
      graph.outgoingEdgesOf(current).stream()
          .map(graph::getEdgeTarget)
          .filter(Predicate.not(stopCondition))
          .filter(Predicate.not(visited::contains))
          .forEach(visited::add);
    }
    return visited;
  }

  public Set<Artifact> flowIncoming(Collection<Artifact> artifacts, Predicate<Artifact> stopCondition) {
    Set<Artifact> visited = new HashSet<>();
    Set<Artifact> toCheck = new HashSet<>();
    toCheck.addAll(artifacts);
    while (!toCheck.isEmpty()) {
      var current = toCheck.iterator().next();
      toCheck.remove(current);
      visited.add(current);
      graph.incomingEdgesOf(current).stream()
          .map(graph::getEdgeSource)
          .filter(Predicate.not(stopCondition))
          .filter(Predicate.not(visited::contains))
          .forEach(toCheck::add);
    }
    return visited;
  }

  public Set<Artifact> directIncoming(Collection<Artifact> artifacts, Predicate<Artifact> stopCondition) {
    Set<Artifact> visited = new HashSet<>();
    Set<Artifact> toCheck = new HashSet<>();
    toCheck.addAll(artifacts);
    while (!toCheck.isEmpty()) {
      var current = toCheck.iterator().next();
      toCheck.remove(current);
      visited.add(current);
      graph.incomingEdgesOf(current).stream()
          .map(graph::getEdgeSource)
          .filter(Predicate.not(stopCondition))
          .filter(Predicate.not(visited::contains))
          .forEach(visited::add);
    }
    return visited;
  }
}
