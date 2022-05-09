/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.github.libi.services.libiel.LibiELService;
import org.github.libi.services.libiel.VerticeSet;
import org.springframework.stereotype.Service;

@Service
public class ColorService {

  private final LibiELService libiELService;

  private final Map<String, String> colors = new LinkedHashMap<>();

  public ColorService(LibiELService libiELService) {
    this.libiELService = libiELService;
  }

  public void setColor(String color, String expr) {
    colors.put(color, expr);
  }

  public Map<String, VerticeSet> getColorForVertices(DependencyGraph graph) {
    return colors.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> libiELService.filter(graph, e.getValue()),
            (o1, o2) -> o1,
            LinkedHashMap::new));
  }

  public Map<String, String> getColors() {
    return colors;
  }

  public void clear() {
    colors.clear();
  }
}
