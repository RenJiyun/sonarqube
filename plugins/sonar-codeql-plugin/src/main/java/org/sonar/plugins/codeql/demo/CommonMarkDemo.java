package org.sonar.plugins.codeql.demo;

import org.apache.commons.io.IOUtils;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CommonMarkDemo {
    public static void main(String[] args) throws IOException {
        // read from file in the resources folder
        File testMd = new File(CommonMarkDemo.class.getClassLoader().getResource("test.md").getFile());

        // read the file content into a string
        FileInputStream in = new FileInputStream(testMd);
        String content = IOUtils.toString(in);
//        System.out.println(content);

        Parser parser = Parser.builder().build();
        Node document = parser.parse(content);
        Visitor visitor = new AbstractVisitor() {
            @Override
            public void visit(FencedCodeBlock fencedCodeBlock) {
                System.out.println(fencedCodeBlock.getLiteral());
            }

            @Override
            public void visit(ThematicBreak thematicBreak) {
                visitChildren(thematicBreak);
            }
        };

        System.out.println("===========================");
        document.accept(visitor);
        System.out.println("===========================");
    }
}
