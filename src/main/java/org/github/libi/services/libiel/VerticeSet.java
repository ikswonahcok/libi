/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.github.libi.services.dgraph.Artifact;

@Getter
@Setter
public class VerticeSet {

  private final Map<String, Artifact> id2artifact;

  private final Map<String, List<Artifact>> groupedArtifacts;

  public VerticeSet(Collection<Artifact> artifacts) {
    this.id2artifact = artifacts.stream()
        .collect(Collectors.toMap(
            Artifact::getId,
            Function.identity(),
            (a1, a2) -> a1));
    this.groupedArtifacts = artifacts.stream()
        .collect(Collectors.groupingBy(Artifact::getGroup));
  }

  public VerticeSet(Artifact artifact) {
    this(Collections.singleton(artifact));
  }

  public VerticeSet() {
    this(Collections.emptyList());
  }

  public Optional<Artifact> getById(String id) {
    return Optional.ofNullable(id2artifact.get(id));
  }

  public Collection<Artifact> getArtifacts() {
    return id2artifact.values();
  }

  public boolean contains(Artifact a) {
    return id2artifact.containsKey(a.getId());
  }
}
