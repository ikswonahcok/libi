/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.HashSet;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

public class DependencyGraphOps {

  public static DependencyGraph findTransitiveDependencies(DependencyGraph mainDependencyGraph,
      DependencyGraph dependencySubgraph) {
    var mainGraph = mainDependencyGraph.getGraph();
    if (!(mainGraph instanceof AbstractBaseGraph)) {
      return dependencySubgraph;
    }
    var clonedGraph = (Graph<Artifact, Dependency>) ((AbstractBaseGraph) mainGraph).clone();
    var toRemove = new HashSet<>(mainGraph.vertexSet());
    toRemove.removeAll(dependencySubgraph.getGraph().vertexSet());
    toRemove.forEach(removedArtifact -> {
      var inArtifacts = clonedGraph.incomingEdgesOf(removedArtifact).stream()
          .map(clonedGraph::getEdgeSource)
          .collect(Collectors.toSet());
      var outArtifacts = clonedGraph.outgoingEdgesOf(removedArtifact).stream()
          .map(clonedGraph::getEdgeTarget)
          .collect(Collectors.toSet());
      inArtifacts.forEach(inArtifact -> {
        outArtifacts.forEach(outArtifact -> {
          if (clonedGraph.getEdge(inArtifact, outArtifact) == null) {
            var depIn = clonedGraph.getEdge(inArtifact, removedArtifact);
            var depOut = clonedGraph.getEdge(removedArtifact, outArtifact);
            var transitiveDep = new Dependency();
            transitiveDep.setTransitive(true);
            transitiveDep.setDist(1 + depIn.getDist() + depOut.getDist());
            clonedGraph.addEdge(inArtifact, outArtifact, transitiveDep);
          }
        });
      });
      clonedGraph.removeVertex(removedArtifact);
    });

    return new DependencyGraph(clonedGraph);
  }
}
