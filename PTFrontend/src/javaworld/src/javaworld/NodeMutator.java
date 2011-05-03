/* This is a general interface for "rewriters" that rewrite
   a whole subtree of the AST. The mutator is created with
   some state (for instance mappings of things that are to
   be renamed) and can then be used several times.

   Note that the way this is done in general in the code
   is that subtree mutation is done right after making a
   full copy (it is done to the copy). We can't mutate
   any part of the original template itself because it
   could be instantiated on several occasions in different
   ways.

   Currently much of this mutation is done InstTupleRew.java,
   in the getRenamed* methods. This is an attempt to do the
   same sort of thing in a cleaner way (the new thing that
   needs to be done now is changing references to type
   variables).
*/

package javaworld;
import AST.ASTNode;

abstract class NodeMutator {
    public abstract void mutate(ASTNode node);
}
