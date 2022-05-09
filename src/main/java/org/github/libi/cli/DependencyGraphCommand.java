/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.cli;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.github.libi.services.dgraph.Artifact;
import org.github.libi.services.dgraph.ColorService;
import org.github.libi.services.dgraph.Dependency;
import org.github.libi.services.dgraph.DependencyGraph;
import org.github.libi.services.dgraph.DependencyGraphOps;
import org.github.libi.services.dgraph.DependencyGraphService;
import org.github.libi.services.extapp.GraphVizService;
import org.github.libi.services.extapp.ImageViewerService;
import org.github.libi.services.libiel.LibiELService;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Getter
@Setter
@ShellComponent
public class DependencyGraphCommand {

  private final DependencyGraphService dependencyGraphService;

  private final LibiELService libiELService;

  private final ColorService colorService;

  private final ImageViewerService imageViewerService;

  private final GraphVizService graphVizService;

  public DependencyGraphCommand(
      DependencyGraphService dependencyGraphService,
      LibiELService libiELService, ColorService colorService,
      ImageViewerService imageViewerService,
      GraphVizService graphVizService) {
    this.dependencyGraphService = dependencyGraphService;
    this.libiELService = libiELService;
    this.colorService = colorService;
    this.imageViewerService = imageViewerService;
    this.graphVizService = graphVizService;
  }

  @ShellMethod("Scan directories to create dependency graph")
  public void scanDir(
      Set<String> dirs,
      String graph) {
    var dgraph = dependencyGraphService.buildDependencyGraphUsingGradlew(dirs);
    dependencyGraphService.saveGraph(dgraph, new File("graphs/" + graph + ".json"));
  }

  @ShellMethod("Load dependency graph")
  public void load(
      String graph) {
    dependencyGraphService.loadGraph(new File("graphs/" + graph + ".json"));
    System.out.printf(
        "Loaded graph with %d artifacts.\n", dependencyGraphService.getWorkingGraph().getGraph()
            .vertexSet()
            .size());
  }

  @ShellMethod("Set color for artifacts filtered by expression")
  public void color(
      String color,
      String expr) {
    colorService.setColor(color, expr);
  }

  @ShellMethod("List colors")
  public void colors() {
    var maxWidth = colorService.getColors().keySet().stream()
        .map(String::length)
        .max(Integer::compareTo)
        .orElse(0);
    colorService.getColors().entrySet().forEach(e -> {
      System.out.printf("%-" + maxWidth + "s : \"%s\"\n", e.getKey(), e.getValue());
    });
  }

  @ShellMethod("Remove colors settings")
  public void clearColors() {
    colorService.clear();
  }

  @ShellMethod("Add function")
  public void function(
      String name,
      String expr) {
    libiELService.addFunction(name, expr);
  }

  @ShellMethod("List LibiEL functions")
  public void functions() {
    var functions = libiELService.getSortedFunctions();
    var maxWidth = functions.stream()
        .map(f -> f.getSignature().getName().length())
        .max(Integer::compareTo)
        .orElse(0);
    functions.forEach(f -> {
      System.out.printf("%-" + (maxWidth) + "s : ", f.getSignature().getName());
      if (!f.getSignature().getArguments().isEmpty()) {
        System.out.print(f.getSignature().getArguments() + " ");
      }
      System.out.println(f.getDescription());
    });
  }

  @ShellMethod("Export as dot file")
  public void diagram(
      String expr,
      @ShellOption(defaultValue = "diagram") String name,
      boolean show,
      @ShellOption(defaultValue = "100") int maxSize,
      boolean transitive)
      throws InterruptedException {
    var vs = libiELService.filter(expr);
    Graph<Artifact, Dependency> subgraph = new AsSubgraph<>(
        dependencyGraphService.getWorkingGraph().getGraph(), new HashSet<>(vs.getArtifacts()));
    System.out.printf("Created diagram with %d artifacts\n", subgraph.vertexSet().size());
    var colorMap = colorService.getColorForVertices(new DependencyGraph(subgraph));
    if (transitive) {
      subgraph = DependencyGraphOps.findTransitiveDependencies(
          dependencyGraphService.getWorkingGraph(), new DependencyGraph(subgraph)).getGraph();
    }
    dependencyGraphService.dotExport(subgraph, new File("diagrams/" + name), colorMap);
    if (show) {
      if (subgraph.vertexSet().size() <= maxSize) {
        if (graphVizService.dotGenPng(new File("diagrams/" + name))) {
          imageViewerService.openInViewer(new File("diagrams/" + name + ".png"));
        }
      } else {
        System.out.println("Diagram too big to show. Generated only dot file.");
      }
    }
  }

  @ShellMethod("List artifacts")
  public void list(
      String expr) {
    var vs = libiELService.filter(expr);
    System.out.printf("Found %d artifacts:\n", vs.getArtifacts().size());
    vs.getArtifacts().stream()
        .map(Artifact::getId)
        .sorted()
        .forEach(System.out::println);
  }

  @ShellMethod("Count artifacts")
  public void count(
      String expr) {
    var vs = libiELService.filter(expr);
    System.out.printf("Found %d artifacts\n", vs.getArtifacts().size());
  }

  @ShellMethod("Topological sort")
  public void tsort(
      String expr) {
    var vs = libiELService.filter(expr);
    var subgraph = new AsSubgraph<>(
        dependencyGraphService.getWorkingGraph().getGraph(), new HashSet<>(vs.getArtifacts()));
    var cArtifacts = new TreeSet<>(Comparator.comparing(Artifact::getId));
    subgraph.vertexSet().stream().filter(a -> subgraph.outDegreeOf(a) == 0)
        .forEach(cArtifacts::add);
    while (!cArtifacts.isEmpty()) {
      var cArtifact = cArtifacts.first();
      System.out.println(cArtifact.getId());
      var nb = subgraph.incomingEdgesOf(cArtifact).stream().map(subgraph::getEdgeSource).collect(
          Collectors.toSet());
      subgraph.removeVertex(cArtifact);
      cArtifacts.remove(cArtifact);
      nb.stream().filter(a -> subgraph.outDegreeOf(a) == 0).forEach(cArtifacts::add);
    }
    var remaining = subgraph.vertexSet();
    if (!remaining.isEmpty()) {
      System.out.println(
          "\n !!! WARNING !!! could not sort all artifacts due to cycles. Remaining artifacts :\n");
      remaining.stream()
          .map(Artifact::getId)
          .sorted()
          .forEach(System.out::println);
    }
  }
}
