/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.github.libi.libiel.LibiELLexer;
import org.github.libi.libiel.LibiELParser;
import org.github.libi.services.dgraph.DependencyGraph;
import org.github.libi.services.dgraph.DependencyGraphService;
import org.github.libi.services.libiel.functions.VSFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibiELService {

  private DependencyGraphService dependencyGraphService;

  private final LibiELEnv libiELEnv;

  @Autowired
  public void setDependencyGraphService(
      DependencyGraphService dependencyGraphService) {
    this.dependencyGraphService = dependencyGraphService;
    readFunctions(new File("functions"));
  }

  public VerticeSet filter(DependencyGraph dependencyGraph, String expression) {
    return filter(libiELEnv, dependencyGraph, dependencyGraphService.getWorkingGraph(), expression);
  }

  public VerticeSet filter(String expression) {
    return filter(dependencyGraphService.getWorkingGraph(), expression);
  }

  public void addFunction(String name, String expr) {
    libiELEnv.getVsFunctions().addExpressionFunction(name, expr);
  }

  public static VerticeSet filter(LibiELEnv env, DependencyGraph dependencyGraph,
      DependencyGraph mainGraph,
      String expression) {
    var charStream = CharStreams.fromString(expression);
    var lexer = new LibiELLexer(charStream);
    var tokens = new CommonTokenStream(lexer);
    var parser = new LibiELParser(tokens);
    var exp = parser.expression();
    return new LibiELVSVisitor(
        new LibiELVisitorCtx(env, dependencyGraph, mainGraph)).visitExpression(exp);
  }

  public List<VSFunction> getSortedFunctions() {
    return libiELEnv.getVsFunctions().getFunctions().stream()
        .sorted(VSFunction.comparator())
        .collect(Collectors.toList());
  }

  private void readFunctions(File file) {
    try (var br = new BufferedReader(new FileReader(file))) {
      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        var tokens = line.split("=", 2);
        if (tokens.length < 2) {
          continue;
        }
        count++;
        addFunction(tokens[0], tokens[1]);
      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
    }
  }
}
