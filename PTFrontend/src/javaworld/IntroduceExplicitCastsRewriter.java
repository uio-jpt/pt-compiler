package javaworld;

import AST.ASTNode;

public class IntroduceExplicitCastsRewriter extends NodeMutator {
    public void mutate( ASTNode node ) {
        node.introduceExplicitCasts();
    }
}
