/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.github.libi.services.dgraph.Artifact;
import org.github.libi.services.dgraph.Dependency;
import org.jgrapht.graph.AsSubgraph;
import org.springframework.context.annotation.Lazy;

@Lazy(false)
@LibiELComponent
public class LibiELFunctions {

  @LibiELFunction("display artifacts count")
  public VerticeSet count(LibiELVisitorCtx ctx, String name, VerticeSet vs) {
    System.out.println(
        "Artifacts count for " + name
            + " is " + vs.getArtifacts()
            .size());
    return vs;
  }

  @LibiELFunction("find dependency graf roots (Artifact with no incoming dependencies)")
  public VerticeSet roots(LibiELVisitorCtx ctx) {
    return new VerticeSet(
        ctx.getVerticeSet().getArtifacts().stream()
            .filter(a -> ctx.getGraph().getGraph().inDegreeOf(a) == 0)
            .collect(Collectors.toSet()));
  }

  @LibiELFunction("find dependency graf roots (Artifact with no incoming dependencies)")
  public VerticeSet roots(LibiELVisitorCtx ctx, VerticeSet vs) {
    var artifacts = vs.getArtifacts();
    var subgraph = new AsSubgraph<>(ctx.getGraph().getGraph(), new HashSet<>(artifacts));
    return new VerticeSet(
        artifacts.stream()
            .filter(a -> subgraph.inDegreeOf(a) == 0)
            .collect(Collectors.toSet()));
  }

  @LibiELFunction("find dependency graf leaves (Artifact with no outgoing dependencies)")
  public VerticeSet leaves(LibiELVisitorCtx ctx) {
    return new VerticeSet(
        ctx.getVerticeSet().getArtifacts().stream()
            .filter(a -> ctx.getGraph().getGraph().outDegreeOf(a) == 0)
            .collect(Collectors.toSet()));
  }

  @LibiELFunction("find dependency graf leaves (Artifact with no outgoing dependencies)")
  public VerticeSet leaves(LibiELVisitorCtx ctx, VerticeSet vs) {
    var artifacts = vs.getArtifacts();
    var subgraph = new AsSubgraph<>(ctx.getGraph().getGraph(), new HashSet<>(artifacts));
    return new VerticeSet(
        artifacts.stream()
            .filter(a -> subgraph.outDegreeOf(a) == 0)
            .collect(Collectors.toSet()));
  }

  @LibiELFunction("find dependency graf isolated vertices (Artifact with no dependencies)")
  public VerticeSet isolated(LibiELVisitorCtx ctx) {
    return new VerticeSet(
        ctx.getVerticeSet().getArtifacts().stream()
            .filter(a -> ctx.getGraph().getGraph().degreeOf(a) == 0)
            .collect(Collectors.toSet()));
  }

  @LibiELFunction("find dependency graf cycles")
  public VerticeSet cycles(LibiELVisitorCtx ctx) {
    var subgraph = new AsSubgraph<>(
        ctx.getGraph().getGraph());
    var cArtifacts = subgraph.vertexSet().stream()
        .filter(a ->
            subgraph.outDegreeOf(a) == 0 || subgraph.inDegreeOf(a) == 0)
        .collect(Collectors.toCollection(HashSet::new));
    while (!cArtifacts.isEmpty()) {
      var cArtifact = cArtifacts.iterator().next();
      var nb = subgraph.edgesOf(cArtifact).stream()
          .map(subgraph::getEdgeSource)
          .filter(Predicate.not(cArtifact::equals))
          .collect(Collectors.toSet());
      subgraph.removeVertex(cArtifact);
      cArtifacts.remove(cArtifact);
      nb.stream()
          .filter(a -> subgraph.outDegreeOf(a) == 0 || subgraph.inDegreeOf(a) == 0)
          .forEach(cArtifacts::add);
    }
    return new VerticeSet(subgraph.vertexSet());
  }

  @LibiELFunction("get compile dependency")
  public VerticeSet compile(LibiELVisitorCtx ctx, VerticeSet vs) {
    var allArtifacts = new HashSet<>(vs.getArtifacts());
    vs.getArtifacts().stream()
        .map(Artifact::getCompileDependencies)
        .flatMap(Collection::stream)
        .map(ctx.getVerticeSet()::getById)
        .flatMap(Optional::stream)
        .forEach(allArtifacts::add);

    return new VerticeSet(allArtifacts);
  }

  @LibiELFunction("get runtime dependency")
  public VerticeSet runtime(LibiELVisitorCtx ctx, VerticeSet vs) {
    var allArtifacts = new HashSet<>(vs.getArtifacts());
    vs.getArtifacts().stream()
        .map(Artifact::getRuntimeDependencies)
        .flatMap(Collection::stream)
        .map(ctx.getVerticeSet()::getById)
        .flatMap(Optional::stream)
        .forEach(allArtifacts::add);

    return new VerticeSet(allArtifacts);
  }
}
