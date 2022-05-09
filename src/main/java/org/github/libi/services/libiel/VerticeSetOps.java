/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.github.libi.services.dgraph.DependencyFlow;
import org.github.libi.services.dgraph.DependencyGraph;

public class VerticeSetOps {

  public static VerticeSet id(VerticeSet vs, String artifactId) {
    return new VerticeSet(
        vs.getById(artifactId)
            .stream()
            .collect(Collectors.toList()));
  }

  public static VerticeSet gid(VerticeSet vs, String groupId) {
    return new VerticeSet(
        vs.getGroupedArtifacts()
            .getOrDefault(groupId, Collections.emptyList()));
  }

  public static VerticeSet re(VerticeSet vs, String re) {
    var result = new VerticeSet();
    var pattern = Pattern.compile(re);
    return new VerticeSet(
        vs.getId2artifact().values().stream()
            .filter(a -> pattern.matcher(a.getId()).find())
            .collect(Collectors.toList()));
  }

  public static VerticeSet gre(VerticeSet vs, String re) {
    var result = new VerticeSet();
    var pattern = Pattern.compile(re);
    return new VerticeSet(
        vs.getGroupedArtifacts().entrySet().stream()
            .filter(e -> pattern.matcher(e.getKey()).find())
            .flatMap(e -> e.getValue().stream())
            .collect(Collectors.toList()));
  }

  public static VerticeSet add(VerticeSet vs1, VerticeSet vs2) {
    var vertices = new HashSet<>(vs1.getArtifacts());
    vertices.addAll(vs2.getArtifacts());
    return new VerticeSet(vertices);
  }

  public static VerticeSet subtract(VerticeSet vs1, VerticeSet vs2) {
    var vertices = new HashSet<>(vs1.getArtifacts());
    vertices.removeAll(vs2.getArtifacts());
    return new VerticeSet(vertices);
  }

  public static VerticeSet intersect(VerticeSet vs1, VerticeSet vs2) {
    var vertices = new HashSet<>(vs1.getArtifacts());
    vertices.retainAll(vs2.getArtifacts());
    return new VerticeSet(vertices);
  }

  public static VerticeSet flowOut(DependencyGraph graph, VerticeSet vs) {
    var flow = new DependencyFlow(graph);
    return new VerticeSet(flow.flowOutgoing(vs.getArtifacts(), a -> false));
  }

  public static VerticeSet flowIn(DependencyGraph graph, VerticeSet vs) {
    var flow = new DependencyFlow(graph);
    return new VerticeSet(flow.flowIncoming(vs.getArtifacts(), a -> false));
  }

  public static VerticeSet directOut(DependencyGraph graph, VerticeSet vs) {
    var flow = new DependencyFlow(graph);
    return new VerticeSet(flow.directOutgoing(vs.getArtifacts(), a -> false));
  }

  public static VerticeSet directIn(DependencyGraph graph, VerticeSet vs) {
    var flow = new DependencyFlow(graph);
    return new VerticeSet(flow.directIncoming(vs.getArtifacts(), a -> false));
  }
}
