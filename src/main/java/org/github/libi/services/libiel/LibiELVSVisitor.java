/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.libiel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.github.libi.libiel.LibiELBaseVisitor;
import org.github.libi.libiel.LibiELParser;
import org.github.libi.libiel.LibiELParser.ExpressionContext;
import org.github.libi.libiel.LibiELParser.VsAllContext;
import org.github.libi.libiel.LibiELParser.VsAtomContext;
import org.github.libi.libiel.LibiELParser.VsCurrentContext;
import org.github.libi.libiel.LibiELParser.VsFilterContext;
import org.github.libi.libiel.LibiELParser.VsFlowContext;
import org.github.libi.libiel.LibiELParser.VsFunctionContext;
import org.github.libi.libiel.LibiELParser.VsGIdContext;
import org.github.libi.libiel.LibiELParser.VsGReContext;
import org.github.libi.libiel.LibiELParser.VsIdContext;
import org.github.libi.libiel.LibiELParser.VsIntersectContext;
import org.github.libi.libiel.LibiELParser.VsNotContext;
import org.github.libi.libiel.LibiELParser.VsReContext;
import org.github.libi.libiel.LibiELParser.VsSelectorContext;
import org.github.libi.libiel.LibiELParser.VsSumContext;
import org.github.libi.libiel.LibiELParser.VsUnaryFlowContext;
import org.github.libi.services.libiel.functions.ArgumentType;
import org.github.libi.services.libiel.functions.FunctionArgument;
import org.github.libi.services.libiel.functions.FunctionArgumentString;
import org.github.libi.services.libiel.functions.FunctionArgumentVS;
import org.github.libi.services.libiel.functions.VSFunctionSignature;

@RequiredArgsConstructor
public class LibiELVSVisitor extends LibiELBaseVisitor<VerticeSet> {

  private final LibiELVisitorCtx visitorCtx;

  @Override
  public VerticeSet visitExpression(ExpressionContext ctx) {
    return super.visitVs(ctx.vs());
  }

  @Override
  public VerticeSet visitVsFilter(VsFilterContext ctx) {
    var chCtxList = ctx.vsAll();
    if (chCtxList.size() == 0) {
      return visitorCtx.getVerticeSet();
    }
    if (chCtxList.size() == 1) {
      return visitVsAll(chCtxList.get(0));
    }
    var fGraph = visitorCtx.getGraph();
    for (var chCtx : chCtxList) {
      var visitor = new LibiELVSVisitor(visitorCtx.createChild(fGraph));
      fGraph = fGraph.subgraph(new HashSet<>(visitor.visitVsAll(chCtx).getArtifacts()));
    }
    return new VerticeSet(fGraph.getGraph().vertexSet());
  }

  @Override
  public VerticeSet visitVsSum(VsSumContext ctx) {
    var vertices = visitVsIntersect(ctx.vsIntersect(0));
    for (int i = 1; i <= (ctx.children.size() - 1) / 2; i++) {
      if (((TerminalNode) ctx.children.get(2 * i - 1)).getSymbol().getType() == LibiELParser.Plus) {
        vertices = VerticeSetOps.add(vertices,
            visitVsIntersect((VsIntersectContext) ctx.children.get(2 * i)));
      } else if (((TerminalNode) ctx.children.get(2 * i - 1)).getSymbol().getType()
          == LibiELParser.Minus) {
        vertices = VerticeSetOps.subtract(vertices,
            visitVsIntersect((VsIntersectContext) ctx.children.get(2 * i)));
      }
    }
    return vertices;
  }

  @Override
  public VerticeSet visitVsIntersect(VsIntersectContext ctx) {
    var iter = ctx.vsUnaryFlow().iterator();
    var vertices = visitVsUnaryFlow(iter.next());
    while (iter.hasNext()) {
      vertices = VerticeSetOps.intersect(vertices, visitVsUnaryFlow(iter.next()));
    }
    return vertices;
  }

  @Override
  public VerticeSet visitVsFlow(VsFlowContext ctx) {
    var vertices = visitVsNot(ctx.vsNot(0));
    for (SymbolContextPair<VsNotContext> pair : this.<VsNotContext>getSymbolContextPairs(ctx,
        1)) {
      var vertices2 = visitVsNot(pair.getSubtree());
      switch (pair.getSymbol()) {
        case LibiELParser.RArrow:
          vertices = VerticeSetOps.intersect(
              VerticeSetOps.add(vertices, vertices2),
              VerticeSetOps.intersect(
                  VerticeSetOps.directOut(visitorCtx.getGraph(), vertices),
                  VerticeSetOps.directIn(visitorCtx.getGraph(), vertices2)));
          break;
        case LibiELParser.LArrow:
          vertices = VerticeSetOps.intersect(
              VerticeSetOps.add(vertices, vertices2),
              VerticeSetOps.intersect(
                  VerticeSetOps.directIn(visitorCtx.getGraph(), vertices),
                  VerticeSetOps.directOut(visitorCtx.getGraph(), vertices2)));
          break;
        case LibiELParser.RLongArrow:
          vertices = VerticeSetOps.intersect(
              VerticeSetOps.flowOut(visitorCtx.getGraph(), vertices),
              VerticeSetOps.flowIn(visitorCtx.getGraph(), vertices2));
          break;
        case LibiELParser.LLongArrow:
          vertices = VerticeSetOps.intersect(
              VerticeSetOps.flowIn(visitorCtx.getGraph(), vertices),
              VerticeSetOps.flowOut(visitorCtx.getGraph(), vertices2));
          break;
      }
    }
    return vertices;
  }

  @Override
  public VerticeSet visitVsUnaryFlow(VsUnaryFlowContext ctx) {
    var vertices = visitVsFlow(ctx.vsFlow());
    for (var symbol : getSymbolList(ctx, 1)) {
      switch (symbol) {
        case LibiELParser.RArrow:
          vertices = VerticeSetOps.directOut(visitorCtx.getGraph(), vertices);
          break;
        case LibiELParser.LArrow:
          vertices = VerticeSetOps.directIn(visitorCtx.getGraph(), vertices);
          break;
        case LibiELParser.RLongArrow:
          vertices = VerticeSetOps.flowOut(visitorCtx.getGraph(), vertices);
          break;
        case LibiELParser.LLongArrow:
          vertices = VerticeSetOps.flowIn(visitorCtx.getGraph(), vertices);
          break;
      }
    }
    return vertices;
  }

  @Override
  public VerticeSet visitVsNot(VsNotContext ctx) {
    if (ctx.Not() == null) {
      return visitVsAtom(ctx.vsAtom());
    }
    return VerticeSetOps.subtract(visitorCtx.getVerticeSet(), visitVsAtom(ctx.vsAtom()));
  }

  @Override
  public VerticeSet visitVsSelector(VsSelectorContext ctx) {
    return super.visitVsSelector(ctx);
  }

  @Override
  public VerticeSet visitVsId(VsIdContext ctx) {
    return VerticeSetOps.id(visitorCtx.getVerticeSet(),
        ctx.string().getText().substring(1, ctx.string().getText().length() - 1));
  }

  @Override
  public VerticeSet visitVsRe(VsReContext ctx) {
    return VerticeSetOps.re(visitorCtx.getVerticeSet(),
        ctx.string().getText().substring(1, ctx.string().getText().length() - 1));
  }

  @Override
  public VerticeSet visitVsGId(VsGIdContext ctx) {
    return VerticeSetOps.gid(visitorCtx.getVerticeSet(),
        ctx.string().getText().substring(1, ctx.string().getText().length() - 1));
  }

  @Override
  public VerticeSet visitVsGRe(VsGReContext ctx) {
    return VerticeSetOps.gre(visitorCtx.getVerticeSet(),
        ctx.string().getText().substring(1, ctx.string().getText().length() - 1));
  }

  @Override
  public VerticeSet visitVsAll(VsAllContext ctx) {
    if (ctx.All() == null) {
      return visitVsSum(ctx.vsSum());
    }
    var visitor = new LibiELVSVisitor(visitorCtx.createChildWithMainGraph());
    return visitor.visitVsSum(ctx.vsSum());
  }

  @Override
  public VerticeSet visitVsCurrent(VsCurrentContext ctx) {
    return visitorCtx.getVerticeSet();
  }

  @Override
  public VerticeSet visitVsAtom(VsAtomContext ctx) {
    if (ctx.vs() != null) {
      return visitVs(ctx.vs());
    }
    if (ctx.vsFunction() != null) {
      return visitVsFunction(ctx.vsFunction());
    }
    if (ctx.vsSelector() != null) {
      return visitVsSelector(ctx.vsSelector());
    }
    return visitorCtx.getVerticeSet();
  }

  @Override
  public VerticeSet visitVsFunction(VsFunctionContext ctx) {
    String functionName = ctx.Word().getText();
    List<FunctionArgument> args = ctx.vsFunctionArgument().stream()
        .map(arg -> {
          if (arg.vs() != null) {
            return new FunctionArgumentVS(visitVs(arg.vs()));
          } else if (arg.string() != null) {
            return new FunctionArgumentString(
                arg.string().getText().substring(1, arg.string().getText().length() - 1));
          }
          return null;
        }).collect(Collectors.toList());
    List<ArgumentType> argTypes = args.stream()
        .map(FunctionArgument::getType)
        .collect(Collectors.toList());

    var func = visitorCtx.getEnv().getVsFunctions().get(
        VSFunctionSignature.builder()
            .name(functionName)
            .arguments(argTypes)
            .build());
    if (func == null) {
      throw new RuntimeException("No function " + functionName + " with arguments " + argTypes);
    }
    return func.call(visitorCtx, args);
  }

  private <C extends ParserRuleContext> List<SymbolContextPair<C>> getSymbolContextPairs(
      ParserRuleContext parent,
      int startIndex) {
    var result = new ArrayList<SymbolContextPair<C>>();
    for (int i = startIndex; i < parent.children.size() - 1; i += 2) {
      result.add(
          SymbolContextPair.<C>builder()
              .symbol(((TerminalNode) parent.children.get(i)).getSymbol().getType())
              .subtree((C) (parent.children.get(i + 1)))
              .build());
    }
    return result;
  }

  private List<Integer> getSymbolList(ParserRuleContext parent,
      int startIndex) {
    var result = new ArrayList<Integer>();
    for (int i = startIndex; i < parent.children.size(); i++) {
      result.add(((TerminalNode) parent.children.get(i)).getSymbol().getType());
    }
    return result;
  }

  @RequiredArgsConstructor
  @Getter
  @Builder
  private static class SymbolContextPair<C extends ParserRuleContext> {

    private final int symbol;

    private final C subtree;
  }
}
