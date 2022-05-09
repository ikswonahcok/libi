/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.github.libi.cmdtree.GradleCommandTreeReader;
import org.github.libi.services.libiel.VerticeSet;
import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.json.JSONExporter;
import org.jgrapht.nio.json.JSONImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DependencyGraphService {

  private final AtomicReference<DependencyGraph> workingGraph = new AtomicReference<>();

  private GradlewService gradlewService;

  public DependencyGraph buildDependencyGraphUsingGradlew(Set<String> dirs) {
    var graph = new DependencyGraph();
    dirs.stream().map(File::new).forEach(file -> addDependencyRecursive(file, graph, 3));
    workingGraph.set(graph);
    return graph;
  }

  @Autowired
  public void setGradlewService(GradlewService gradlewService) {
    this.gradlewService = gradlewService;
  }

  public List<Project> getProjects(File dir) {
    var listener = new ProjectsTreeLineListener();
    var treeReader = new GradleCommandTreeReader(listener);
    gradlewService.processGradlewProjectsOutput(dir,
        treeReader::readLines
    );
    listener.getProjects().forEach(project -> project.setDir(dir));
    return listener.getProjects();
  }

  public void scanDependencies(Project project, DependencyGraph graph,
      Map<String, Artifact> project2Artifact) {
    System.out.println("Scanning dependencies for project " + project.getName());
    var artifact = project2Artifact.get(project.getName());
    var listener = new DGraphTreeLineListener(graph, artifact);
    var treeReader = new GradleCommandTreeReader(listener);
    listener.setProjectName2ArtifactDict(project2Artifact);
    listener.addArtifact(artifact);
    gradlewService.processGradlewDependencies(project, treeReader::readLines);
  }

  public Map<String, String> getProperties(Project project) {
    ProcessBuilder processBuilder;
    if (project.isRoot()) {
      processBuilder = new ProcessBuilder("./gradlew", "properties").directory(
          project.getDir());
    } else {
      processBuilder = new ProcessBuilder("./gradlew", project.getName() + ":properties").directory(
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

  public Optional<Artifact> getArtifact(Project project) {
    var properties = getProperties(project);
    var group = properties.get("group");
    var name = properties.get("name");
    if (group != null && name != null) {
      return Optional.of(new Artifact(group, name));
    } else {
      return Optional.empty();
    }
  }

  public void saveGraph(DependencyGraph dependencyGraph, File file) {
    try {
      Files.createDirectories(file.getParentFile().toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var exporter = new JSONExporter<Artifact, Dependency>(
        v -> v.getId());
    exporter.setVertexAttributeProvider((v) -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      map.put("isLibrary", DefaultAttribute.createAttribute(v.isLibrary()));
      map.put("runtimeDependencies",
          DefaultAttribute.createAttribute(
              v.getRuntimeDependencies().stream()
                  .collect(
                      Collectors.joining(","))));
      map.put("compileDependencies",
          DefaultAttribute.createAttribute(
              v.getCompileDependencies().stream()
                  .collect(
                      Collectors.joining(","))));
      return map;
    });
    exporter.setEdgeAttributeProvider((e) -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (e.getType() != null) {
        map.put("type", DefaultAttribute.createAttribute(e.getType().toString()));
      }
      return map;
    });
    try (var writer = new FileWriter(file)) {
      Files.createDirectories(file.getParentFile().toPath());
      exporter.exportGraph(dependencyGraph.getGraph(), writer);
    } catch (IOException e) {
    }
  }

  public void loadGraph(File file) {
    var importer = new JSONImporter<Artifact, Dependency>();
    importer.setVertexFactory(Artifact::new);
    importer.setVertexWithAttributesFactory((id, attrs) -> {
      var v = new Artifact(id);
      Optional.ofNullable(attrs.get("isLibrary"))
          .map(Attribute::getValue)
          .ifPresent(value -> {
            if ("true".equals(value)) {
              v.setLibrary(true);
            }
          });
      Optional.ofNullable(attrs.get("runtimeDependencies"))
          .map(Attribute::getValue)
          .ifPresent(value -> {
            v.getRuntimeDependencies().addAll(Arrays.asList(value.split(",")));
          });
      Optional.ofNullable(attrs.get("compileDependencies"))
          .map(Attribute::getValue)
          .ifPresent(value -> {
            v.getCompileDependencies().addAll(Arrays.asList(value.split(",")));
          });
      return v;
    });
    importer.setEdgeWithAttributesFactory(attrs -> {
      var dependency = new Dependency();
      Optional.ofNullable(attrs.get("type"))
          .map(Attribute::getValue)
          .flatMap(Dependency.Type::fromString)
          .ifPresent(dependency::setType);
      return dependency;
    });
    var graph = new DependencyGraph();
    importer.importGraph(graph.getGraph(), file);
    this.workingGraph.set(graph);
  }

  public DependencyGraph getWorkingGraph() {
    return workingGraph.get();
  }

  public void dotExport(File file) {
    try {
      Files.createDirectories(file.getParentFile().toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    dotExport(workingGraph.get().getGraph(), file);
  }

  public void dotExport(Graph<Artifact, Dependency> graph, File file) {
    dotExport(graph, file, Collections.emptyMap());
  }

  public void dotExport(Graph<Artifact, Dependency> graph, File file,
      Map<String, VerticeSet> colorsToArtifacts) {
    try {
      Files.createDirectories(file.getParentFile().toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var exporter = new DOTExporter<Artifact, Dependency>();
    exporter.setVertexAttributeProvider(a -> {
      Map<String, Attribute> attrs = new HashMap<>();
      colorsToArtifacts.entrySet().stream().filter(e ->
          e.getValue().contains(a)
      ).findFirst().map(Entry::getKey).ifPresent(color ->
          {
            attrs.put("style", new DefaultAttribute<>("filled", AttributeType.STRING));
            attrs.put("fillcolor", new DefaultAttribute<>(color, AttributeType.STRING));
          }
      );
      attrs.put("label", new DefaultAttribute<>(a.getId(), AttributeType.STRING));
      return attrs;
    });
    exporter.setEdgeAttributeProvider(d -> {
      Map<String, Attribute> attrs = new HashMap<>();
      if (d.isTransitive()) {
        attrs.put("style", new DefaultAttribute<>("dashed", AttributeType.STRING));
        attrs.put("label", new DefaultAttribute<>(d.getDist(), AttributeType.INT));
      }
      return attrs;
    });
    exporter.setGraphAttributeProvider(() -> {
      Map<String, Attribute> attrs = new HashMap<>();
      attrs.put("rankdir", new DefaultAttribute<>("LR", AttributeType.STRING));
      return attrs;
    });
    exporter.exportGraph(graph, file);
  }

  private void addDependencyRecursive(File dir, DependencyGraph graph, int level) {
    if (level == 0 || !dir.exists() || !dir.isDirectory()) {
      return;
    }

    var gradlewFile = new File(dir, "gradlew");
    if (gradlewFile.exists()) {
      addDependency(dir, graph);
    } else {
      Stream.of(dir.listFiles()).sorted(Comparator.comparing(File::getName)).forEach(subDir -> {
        if (subDir.isDirectory()) {
          addDependencyRecursive(subDir, graph, level - 1);
        }
      });
    }
  }

  private void addDependency(File dir, DependencyGraph graph) {
    var projects = getProjects(dir);
    var project2Artifact = new HashMap<String, Artifact>();
    projects.stream().forEach(project -> {
      getArtifact(project).ifPresent(artifact -> project2Artifact.put(project.getName(), artifact));
    });
    getProjects(dir).forEach(project -> scanDependencies(project, graph, project2Artifact));
  }
}
