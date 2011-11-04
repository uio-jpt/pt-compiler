/* This is a NodeMutator which is used to rewrite references
   to required types.
*/

package javaworld;

import java.util.HashMap;
import java.util.Map;

import AST.TypeDecl;
import AST.TypeAccess;
import AST.Access;
import AST.ASTNode;

class RequiredTypeRewriter extends NodeMutator {
    Map< TypeDecl, Access > mappings = new HashMap<TypeDecl, Access>();
    /* TypeDecl do not have hashCode or equality beyond that of Object,
       so different type variables of the same name in different templates
       should not collide (by object identity they are distinct). */

    public void addRewrite( TypeDecl tv, Access ta ) {
        mappings.put( tv, ta );
    }

    public void debugPrint() {
        for( Map.Entry kv : mappings.entrySet() ) {
            System.out.println( "\t " + kv.getKey() + " -> " + kv.getValue() );
        }
    }

    public void mutate( ASTNode node ) {
        node.replaceTypeAccesses( mappings );
    }
}
