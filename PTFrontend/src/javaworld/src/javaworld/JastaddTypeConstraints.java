package javaworld;

/* This is a utility class for creating TypeConstraints
   from elements in the Jastadd AST.

   May later add methods for creating AST elements from
   existing TypeConstraints. To do this we first need
   to simultaneously give names to all the TypeConstraints
   to be concretified in this way, and the process will
   fail if we encounter any type specified by a TypeConstraint
   which is not to be concretified. (If necessary it is possible
   to find the "closure" of a set of TypeConstraints that must
   be named and concretified in order to concretify the elements
   in the set.)
*/

import AST.InterfaceDecl;
import AST.BodyDecl;
import AST.MethodDecl;
import AST.ConstructorDecl;
import AST.ParameterDeclaration;
import AST.ClassDecl;
import AST.Modifiers;
import AST.Access;
import AST.Opt;
import AST.Block;
import AST.TypeDecl;
import AST.PTDecl;

import AST.RequiredType;
import AST.RequiredClass;
import AST.RequiredInterface;

import java.util.List;
import java.util.Vector;

public class JastaddTypeConstraints {
    static Access simpleTypeDescriptorToAccess( TypeDescriptor td ) {
        /* This doesn't handle TypeDescriptors that are not simple Jastadd types.
           Same goes for all the other simple* methods. These are okay to use
           for some uses, specifically for interfaces (as opposed to general
           constraints), where more general TypeDescriptors should not crop up.
        */
        JastaddTypeDescriptor jtd = (JastaddTypeDescriptor) td;
        return jtd.getAccess();
    }

    static MethodDecl simpleToMethodDecl( MethodDescriptor desc ) {
        AST.List<ParameterDeclaration> params = new AST.List<ParameterDeclaration> ();
        for(int i=0;i<desc.getArity();i++) {
            String pname = "p" + i;
            params.add( new ParameterDeclaration( new Modifiers(),
                                                     simpleTypeDescriptorToAccess( desc.getParameterType( i ) ),
                                                     pname ) );
        }
        return new MethodDecl( new Modifiers(),
                               simpleTypeDescriptorToAccess( desc.getReturnType() ),
                               desc.getName(),
                               params,
                               new AST.List<Access>(), // throws list
                               new Opt<Block>() // empty body
                               );
    }

    static MethodDescriptor describeMethodDecl( MethodDecl mdecl ) {
        String name = mdecl.getID();
        JastaddTypeDescriptor ret = new JastaddTypeDescriptor( mdecl.getTypeAccess() );
        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( ParameterDeclaration pd : mdecl.getParameters() ) {
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );
            params.add( pt );
        }
        return new MethodDescriptor( name, ret, params );

    }

    static ConstructorDescriptor describeConstructorDecl( ConstructorDecl cdecl ) {
        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( ParameterDeclaration pd : cdecl.getParameters() ) {
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );
            params.add( pt );
        }
        return new ConstructorDescriptor( params );

    }

    static void fromInterfaceDeclInto( InterfaceDecl idecl, TypeConstraint tc ) {
        for( BodyDecl bd : idecl.getBodyDecls() ) {
            if( bd instanceof MethodDecl ) {
                MethodDescriptor mdesc = describeMethodDecl( (MethodDecl) bd );
                tc.addMethod( mdesc );
            } else {
                // warn?
            }
        }

        for( Object superio : idecl.implementedInterfaces() ) {
            InterfaceDecl superi = (InterfaceDecl) superio;
            // these are really _extended_, not implemented
            fromInterfaceDeclInto( superi, tc );
        }

        addSuperTypes( idecl, tc );
    }

    static TypeConstraint fromInterfaceDecl( InterfaceDecl idecl ) {
        TypeConstraint tc = new TypeConstraint();

        // since conceptually classes can satisfy interfaces
        // I'm not sure I should .require anything here

        fromInterfaceDeclInto( idecl, tc );

        return tc;
    }

    static void fromClassDeclInto( ClassDecl cdecl, TypeConstraint tc ) {
        for( BodyDecl bd : cdecl.getBodyDecls() ) {
            if( bd instanceof MethodDecl ) {
                MethodDescriptor mdesc = describeMethodDecl( (MethodDecl) bd );
                tc.addMethod( mdesc );
            } else if( bd instanceof ConstructorDecl ) {
                ConstructorDescriptor cdesc = describeConstructorDecl( (ConstructorDecl) bd );
                tc.addConstructor( cdesc );
            } else {
                System.out.println( "[debug/warning] fromClassDeclInto() did not expect " + bd.getClass().getName() );
                // warn?
            }
        }

        ClassDecl sc = cdecl.superclass();
        if( sc != null ) {
            fromClassDeclInto( sc, tc );
        }

        addSuperTypes( cdecl, tc );
    }

    static TypeConstraint fromClassDecl( ClassDecl cdecl ) {
        TypeConstraint tc = new TypeConstraint();
        tc.requireClass();

        fromClassDeclInto( cdecl, tc );

        return tc;
    }

    static TypeConstraint fromReferenceTypeDecl( TypeDecl tdecl ) {
        if( tdecl instanceof ClassDecl ) {
            return fromClassDecl( (ClassDecl) tdecl );
        }
        if( tdecl instanceof InterfaceDecl ) {
            return fromInterfaceDecl( (InterfaceDecl) tdecl );
        }
        return null;
    }

    public static void addSuperTypes( TypeDecl tdecl, TypeConstraint tc ) {
        if( tdecl instanceof ClassDecl ) {
            ClassDecl cdecl = (ClassDecl) tdecl;
            while( cdecl != null ) {
                if( cdecl.isPtInternalClass() ) {
                    String name = cdecl.getID();
                    tc.addInternalSuperclassName( name );
                } else {
                    String name = cdecl.fullName();
                    tc.addExternalSuperclassName( name );
                }
                cdecl = cdecl.superclass();
            }
        }
        java.util.Stack<InterfaceDecl> stack = new java.util.Stack<InterfaceDecl>();
        for( Object o : tdecl.implementedInterfaces() ) {
            stack.push( (InterfaceDecl) o );
        }
        while( !stack.empty() ) {
            InterfaceDecl decl = stack.pop();
            PTDecl parent = (PTDecl) decl.getParentClass( PTDecl.class );

            if( parent == null ) {
                tc.addExternalInterfaceName( decl.fullName() );
            } else {
                tc.addInternalSuperclassName( decl.getID() );
            }
            

            for( Object o : decl.implementedInterfaces() ) {
                stack.push( (InterfaceDecl) o );
            }
        }
    }

    public static RequiredType convertToRequiredType( String name, TypeConstraint tc ) {
        // TODO think about modifiers, these are discarded here
        RequiredType rv;
        AST.List<BodyDecl> bodyDecls = new AST.List<BodyDecl>();

        AST.Opt<Access> superClassAccess = new AST.Opt<Access>();
        AST.List<Access> superInterfaceAccess = new AST.List<Access>();
        
        if( tc.mustBeClass() ) {
            rv = new RequiredClass( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess );
        } else if( tc.mustBeInterface() ) {
            rv = new RequiredInterface( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess );
        } else {
            rv = new RequiredType( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess );
        }

        return rv;
    }
}
