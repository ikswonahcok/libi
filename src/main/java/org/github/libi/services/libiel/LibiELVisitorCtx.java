/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import lombok.Getter;
import org.github.libi.services.dgraph.DependencyGraph;

@Getter
public class LibiELVisitorCtx {

  private final LibiELEnv env;

  private final DependencyGraph graph;

  private final DependencyGraph mainGraph;

  private final VerticeSet verticeSet;

  public LibiELVisitorCtx(LibiELEnv env, DependencyGraph graph) {
    this.env = env;
    this.graph = graph;
    this.mainGraph = graph;
    verticeSet = new VerticeSet(graph.getGraph().vertexSet());
  }

  public LibiELVisitorCtx(LibiELEnv env, DependencyGraph graph, DependencyGraph mainGraph) {
    this.env = env;
    this.graph = graph;
    this.mainGraph = mainGraph;
    verticeSet = new VerticeSet(graph.getGraph().vertexSet());
  }

  public LibiELVisitorCtx createChild(DependencyGraph graph) {
    return new LibiELVisitorCtx(env, graph, mainGraph);
  }

  public LibiELVisitorCtx createChildWithMainGraph() {
    return new LibiELVisitorCtx(env, mainGraph, mainGraph);
  }
}
