package com.maxxton.microdocs.crawler.doclet.scanner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.maxxton.microdocs.core.reflect.ReflectDescriptionTag;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.SimpleDocTreeVisitor;

/**
 * Custom scanner for all tags we're interested in
 *
 * @author R. Sonke (r.sonke@maxxton.com)
 */
public class TagScanner extends SimpleDocTreeVisitor<Void, Void> {

  private final Map<String, ReflectDescriptionTag> tags;
  private static final List<String> CUSTOM_TAGS = Arrays.asList("response", "example", "ignoreDownstreamCheck");

  public TagScanner(Map<String, ReflectDescriptionTag> tags) {
    this.tags = tags;
  }

  @Override
  public Void visitAuthor(AuthorTree node, Void aVoid) {
    tags.put(node.getTagName(), new ReflectDescriptionTag(node.getTagName(), node.getName().stream().map(Object::toString).collect(Collectors.joining())));
    return super.visitAuthor(node, aVoid);
  }

  @Override
  public Void visitReturn(ReturnTree node, Void aVoid) {
    tags.put(node.getTagName(), new ReflectDescriptionTag(node.getTagName(), node.getDescription().stream().map(Object::toString).collect(Collectors.joining())));
    return super.visitReturn(node, aVoid);
  }

  @Override
  public Void visitParam(ParamTree node, Void aVoid) {
    tags.put(node.getTagName(), new ReflectDescriptionTag(node.getTagName(), node.getName().getName().toString(), node.getDescription().stream().map(Object::toString).collect(Collectors.joining())));
    return super.visitParam(node, aVoid);
  }

  @Override
  public Void visitDeprecated(DeprecatedTree node, Void aVoid) {
    tags.put(node.getTagName(), new ReflectDescriptionTag(node.getTagName(), node.getBody().stream().map(Object::toString).collect(Collectors.joining())));
    return super.visitDeprecated(node, aVoid);
  }

  @Override
  public Void visitDocComment(DocCommentTree tree, Void p) {
    return visit(tree.getBlockTags(), null);
  }

  /**
   * Catch the custom tags response, example, ignoreDownstreamCheck
   */
  @Override
  public Void visitUnknownBlockTag(UnknownBlockTagTree tree, Void p) {
    String name = tree.getTagName();
    if (CUSTOM_TAGS.contains(name)) {
      String content = tree.getContent().toString();
      tags.put(name, new ReflectDescriptionTag(name, content));
    }
    return null;
  }
}
