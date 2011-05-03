/* This is a NodeMutator which is used to rewrite type parameters.

   It is supplied with state consisting of paired TypeVariables k
   and TypeAccesses v.

   When mutate(t) is called, it searches through the subtree t for
   TypeAccesses which refer to some k supplied in the state; a
   type variable to be renamed. These are replaced with the
   corresponding TypeAccess v (the actual parameter value supplied
   in the inst).

   The actual search-through-the-AST-work is done in JastAdd code
   in the PTTypeParameters aspect.
*/

package javaworld;

import java.util.HashMap;
import java.util.Map;

import AST.TypeVariable;
import AST.TypeAccess;
import AST.ASTNode;

class ParameterRewriter extends NodeMutator {
    Map< TypeVariable, TypeAccess > mappings = new HashMap<TypeVariable, TypeAccess>();
    /* TypeVariables do not have hashCode or equality beyond that of Object,
       so different type variables of the same name in different templates
       should not collide (by object identity they are distinct). */

    public void addRewrite( TypeVariable tv, TypeAccess ta ) {
        mappings.put( tv, ta );
    }

    public void debugPrint() {
        for( Map.Entry kv : mappings.entrySet() ) {
            System.out.println( "\t " + kv.getKey() + " -> " + kv.getValue() );
        }
    }

    public void mutate( ASTNode node ) {
        node.replaceTypeVariableAccesses( mappings );
    }
}
