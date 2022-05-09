/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;

@Getter
public class DependencyGraph {

  private final Graph<Artifact, Dependency> graph;

  public DependencyGraph(Graph<Artifact, Dependency> graph) {
    this.graph = graph;
  }

  public DependencyGraph() {
    this.graph = new DefaultDirectedGraph<>(Dependency.class);
  }

  public void addArtifact(Artifact artifact) {
    graph.addVertex(artifact);
  }

  public Dependency addDependency(Artifact source, Artifact target) {
    graph.addVertex(source);
    graph.addVertex(target);
    graph.addEdge(source, target, new Dependency());
    return graph.getEdge(source, target);
  }

  public Optional<Dependency> getDependency(Artifact source, Artifact target) {
    return Optional.ofNullable(graph.getEdge(source, target));
  }

  public DependencyGraph subgraph(Set<Artifact> artifacts) {
    var subgraph = new AsSubgraph<>(graph, artifacts);
    return new DependencyGraph(subgraph);
  }
}
