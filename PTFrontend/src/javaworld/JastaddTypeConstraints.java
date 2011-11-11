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
import AST.FieldDeclaration;
import AST.TypeAccess;
import AST.ParameterDeclaration;
import AST.GenericTypeDecl;
import AST.GenericClassDecl;
import AST.GenericInterfaceDecl;
import AST.TypeVariable;
import AST.RawInterfaceDecl;
import AST.Parameterization;
import AST.ParClassDecl;

import AST.RequiredType;
import AST.RequiredClass;
import AST.RequiredInterface;
import AST.PTAbstractConstructor;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

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

    static MethodDescriptor describeMethodDeclSubstituting( MethodDecl mdecl, ConcretificationScheme scheme, Parameterization pm ) {
        String name = mdecl.getID();
        JastaddTypeDescriptor ret = new JastaddTypeDescriptor( Util.declarationFromTypeAccess( mdecl.getTypeAccess( )).substitute( pm ) );

        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( Object pdo : mdecl.getParameters().substitute( pm ) ) {
            ParameterDeclaration pd = (ParameterDeclaration) pdo;
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );

            params.add( pt.mapByScheme( scheme ) );
        }

        return new MethodDescriptor( name, ret.mapByScheme( scheme ), params );

    }

    static MethodDescriptor describeMethodDecl( MethodDecl mdecl, ConcretificationScheme scheme ) {
        String name = mdecl.getID();
        JastaddTypeDescriptor ret = new JastaddTypeDescriptor( mdecl.getTypeAccess() );

        System.out.println( "LOOOKIE" + mdecl.getParameters().getClass().getName() );
        for( ParameterDeclaration pd : mdecl.getParameters() ) {
            System.out.println( "LOOKIE" + pd );
        }
        System.out.println( "WHAT I FOUND " + mdecl.getClass().getName() );
        System.out.println( ".. " + mdecl.getParent().getParent().getClass().getName() );
        System.out.println( ".. " + mdecl.getParent().getParent() );


        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( ParameterDeclaration pd : mdecl.getParameters() ) {
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );

            params.add( pt.mapByScheme( scheme ) );
        }

        return new MethodDescriptor( name, ret.mapByScheme( scheme ), params );

    }

    static ConstructorDescriptor describeConstructorDeclSubstituting( ConstructorDecl cdecl, ConcretificationScheme scheme, Parameterization pm ) {
        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( Object pdo : cdecl.getParameters().substitute( pm ) ) {
            ParameterDeclaration pd = (ParameterDeclaration) pdo;
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );
            params.add( pt.mapByScheme( scheme ) );
        }
        return new ConstructorDescriptor( params );

    }


    static ConstructorDescriptor describeConstructorDecl( ConstructorDecl cdecl, ConcretificationScheme scheme ) {
        List<TypeDescriptor> params = new Vector<TypeDescriptor>();
        for( ParameterDeclaration pd : cdecl.getParameters() ) {
            JastaddTypeDescriptor pt = new JastaddTypeDescriptor( pd.getTypeAccess() );
            params.add( pt.mapByScheme( scheme ) );
        }
        return new ConstructorDescriptor( params );

    }

    public static void fromRequiredTypeBodyDeclInto( BodyDecl bd, TypeConstraint tc, ConcretificationScheme scheme ) {
        // be aware: PTAbstractConstructor convenience-inhertis from MethodDecl, so order here is important
        if( bd instanceof PTAbstractConstructor ) {
            ConstructorDescriptor cdesc = describeMethodDecl( (MethodDecl) bd, scheme ).toConstructorDescriptor();
            tc.addConstructor( cdesc );
        } else if( bd instanceof MethodDecl ) {
            MethodDescriptor mdesc = describeMethodDecl( (MethodDecl) bd, scheme );
            tc.addMethod( mdesc );
        } else {
              // oops
              System.out.println( "[warning] required type had unexpected body declaration of class " + bd.getClass().getName() );
        }
    }

    static void fromInterfaceDeclInto( InterfaceDecl idecl, TypeConstraint tc, ConcretificationScheme scheme ) {
        System.out.println( "converting interface " + idecl + " of type " + idecl.getClass().getName());
        System.out.println( "into " + tc );

        System.out.println( "methods map: " + idecl.localMethodsSignatureMap() );
        System.out.println( "methods map: " + idecl.methodsSignatureMap() );


        /* This works for normally declared ones, but for ParClass/ParInterface
           the methods are not caused by BodyDecls. */
        /*
        for( BodyDecl bd : idecl.getBodyDecls() ) {
            if( bd instanceof MethodDecl ) {
                MethodDescriptor mdesc = describeMethodDecl( (MethodDecl) bd );
                tc.addMethod( mdesc );
            } else {
                // warn?
            }
        }
        */

        java.util.HashMap lmsm = idecl.localMethodsSignatureMap();
        for( Object methodKey : lmsm.keySet() ) {
            System.out.println( "method key is: " + methodKey + " of type " + methodKey.getClass().getName() );
            Object methodValue = lmsm.get( methodKey );
            System.out.println( "method value is: " + methodValue + " of type " + methodValue.getClass().getName() );
            MethodDecl method = (MethodDecl) methodValue;
            MethodDescriptor methodDesc = describeMethodDecl( method, scheme );
            System.out.println( "direct description of method: " + methodDesc );
            methodDesc.applyScheme( scheme );
            System.out.println( "applied description of method: " + methodDesc );
            tc.addMethod( methodDesc );
        }


        System.out.println( "no implemented intfs:" + idecl.implementedInterfaces().size() );

        for( Object superio : idecl.getSuperInterfaceIdList() ) {
            Access myAc = (Access) superio;
            System.out.println( "ACCESS IS :::: " + myAc );
            TypeDecl superdecl = Util.declarationFromTypeAccess( myAc );
            if( superdecl instanceof ClassDecl ) {
                // this is Object (otherwise a class can't be a "superinterface")
                // (somewhat surprised that Object is, in Jastadd)
                continue;
            }
            InterfaceDecl superi = (InterfaceDecl) superdecl;
            if( superi != null ) {
                // these are really _extended_, not implemented
                fromInterfaceDeclInto( superi, tc, scheme );
            }
            tc.addImplementedType( new JastaddTypeDescriptor( myAc ) );
        }

        System.out.println( "result was " + tc );
    }

    static TypeConstraint fromInterfaceDecl( InterfaceDecl idecl, ConcretificationScheme scheme ) {
        TypeConstraint tc = new TypeConstraint();

        // since conceptually classes can satisfy interfaces
        // I'm not sure I should .require anything here

        fromInterfaceDeclInto( idecl, tc, scheme );

        for( Object superio : idecl.implementedInterfaces() ) {
            InterfaceDecl superi = (InterfaceDecl) superio;

            tc.addImplementedType( new JastaddTypeDescriptor( superi ) );
        }

        System.out.println( "from idecl " + idecl.getClass().getName() );
        System.out.println( "from idecl(1) " + idecl.getParent().getClass().getName() );
        System.out.println( "from idecl(2) " + idecl.getParent().getParent().getClass().getName() );

        if( idecl instanceof GenericInterfaceDecl ) {
            // note the difference between ParClassDecl and GenericClassDecl.
            // a _generic_ class has the parameters unrealized, e.g. Iterator
            // a _parametrized_ class has the parameters realized, e.g. Iterator<Integer>
            // the second matches against a required type with no type parameters
            // the first matches against a required type with the appropriate number of type parameters

            // note, this may be wrong .. not sure this code is used, what we're actually
            // seeing in the Jastadd AST is Raw*, not Generic*

            tc.assertNoTypeParameters();

            GenericInterfaceDecl gcd = (GenericInterfaceDecl) idecl;
            for( TypeVariable typeVar : gcd.getTypeParameterList() ) {
                tc.addTypeParameter( new JastaddTypeParameterDescriptor( typeVar ).mapByScheme( scheme ) );
            }
        } else if( idecl instanceof RawInterfaceDecl ) {
            tc.assertNoTypeParameters();

            System.out.println( "dumping tree before rewrite: " + idecl.getParent().getParent().dumpTreeNoRewrite() );
            System.out.println( "dumping tree: " + idecl.getParent().getParent().dumpTree() );

            GenericInterfaceDecl gcd = ((GenericInterfaceDecl) ((RawInterfaceDecl) idecl).genericDecl());
            for( TypeVariable typeVar : gcd.getTypeParameterList() ) {
                System.out.println( "yee: " + typeVar );
                tc.addTypeParameter( new JastaddTypeParameterDescriptor( typeVar ).mapByScheme( scheme ) );
            }
/*
            RawInterfaceDecl gcd = (RawInterfaceDecl) idecl;
            for( Access acc : gcd.getArgumentList() ) {
                System.out.println( " confusing access of acc type " + acc.getClass().getName() );
            }
*/
        }

        System.out.println( "setting specific type to " + idecl );

        tc.setSpecificType( new JastaddTypeDescriptor( idecl ).mapByScheme( scheme ) );

        return tc;
    }

    static void fromClassDeclInto( ClassDecl cdecl, TypeConstraint tc, ConcretificationScheme scheme ) {
        System.out.println( "ASCENDING to add from: " + cdecl.fullName() + " " + cdecl.getClass().getName() );

        java.util.HashMap methodsMap = cdecl.methodsSignatureMap();
        for( Object keyo : methodsMap.keySet() ) {
            String key = (String) keyo;
            MethodDecl md = (MethodDecl) methodsMap.get( key );
            MethodDescriptor mdesc = describeMethodDecl( md, scheme );
            mdesc.applyScheme( scheme );
            tc.addMethod( mdesc );
        }

        java.util.HashMap fieldsMap = cdecl.memberFieldsMap();
        for( Object keyo : fieldsMap.keySet() ) {
            String key = (String) keyo;
            // Oops, ignored.. TODO
        }

        for( Object cons : cdecl.constructors() ) {
            ConstructorDecl constructor = (ConstructorDecl) cons;
            ConstructorDescriptor cdesc = describeConstructorDecl( constructor, scheme);
            cdesc.applyScheme( scheme );
            tc.addConstructor( cdesc );
        }

        /*

        if( cdecl instanceof ParClassDecl ) {
            ParClassDecl pcdecl = (ParClassDecl) cdecl;
            GenericClassDecl gcdecl = (GenericClassDecl) pcdecl.genericDecl();

            for( BodyDecl bd : gcdecl.getBodyDecls() ) {
                if( false && bd instanceof MethodDecl ) {
                    MethodDescriptor mdesc = describeMethodDeclSubstituting( (MethodDecl) bd, scheme, pcdecl );
                    mdesc.applyScheme( scheme );
                    tc.addMethod( mdesc );
                } else if( bd instanceof ConstructorDecl ) {
                    ConstructorDescriptor cdesc = describeConstructorDeclSubstituting( (ConstructorDecl) bd, scheme, pcdecl );
                    cdesc.applyScheme( scheme );
                    tc.addConstructor( cdesc );
                } else if( bd instanceof FieldDeclaration ) {
                    * We do not support field declarations in required types at the moment (should we? not sure)
                       but we do need to tolerate them in extraction from classes, for conformance checking.
                    /
                } else {
                    System.out.println( "[debug/warning] fromClassDeclInto() did not expect " + bd.getClass().getName() );
                    System.out.println( "[debug/warning] was: " + bd.dumpTree() );
                    System.out.println( "[debug/warning] was: " + bd );
                    // warn?
                }
            }
        } else {
            for( BodyDecl bd : cdecl.getBodyDecls() ) {
                if( false && bd instanceof MethodDecl ) {
                    MethodDescriptor mdesc = describeMethodDecl( (MethodDecl) bd, scheme );
                    mdesc.applyScheme( scheme );
                    tc.addMethod( mdesc );
                } else if( bd instanceof ConstructorDecl ) {
                    ConstructorDescriptor cdesc = describeConstructorDecl( (ConstructorDecl) bd, scheme );
                    cdesc.applyScheme( scheme );
                    tc.addConstructor( cdesc );
                } else if( bd instanceof FieldDeclaration ) {
                    * We do not support field declarations in required types at the moment (should we? not sure)
                       but we do need to tolerate them in extraction from classes, for conformance checking.
                    /
                } else {
                    System.out.println( "[debug/warning] fromClassDeclInto() did not expect " + bd.getClass().getName() );
                    System.out.println( "[debug/warning] was: " + bd.dumpTree() );
                    System.out.println( "[debug/warning] was: " + bd );
                    // warn?
                }
            }

        }
*/

/*        // ?? check more
        ClassDecl sc = cdecl.superclass();
        if( sc != null ) {
            fromClassDeclInto( sc, tc, scheme );
        }
*/

        System.out.println ( "creating from " + cdecl.getID() );

    }

    static TypeConstraint fromClassDecl( ClassDecl cdecl, ConcretificationScheme scheme ) {
        TypeConstraint tc = new TypeConstraint();
        tc.requireClass();

        fromClassDeclInto( cdecl, tc, scheme );

        ClassDecl sc = cdecl.superclass();
        if( sc != null ) {
            tc.addSuperType( new JastaddTypeDescriptor( sc ) );
        }

        for( Access ideclaccess : cdecl.getImplementsList() ) {
            TypeDecl idecl = Util.declarationFromTypeAccess( ideclaccess );
            if( idecl == null ) continue;

            tc.addImplementedType( new JastaddTypeDescriptor( idecl ) );
        }

        if( cdecl instanceof GenericClassDecl ) {
            // note the difference between ParClassDecl and GenericClassDecl.
            // a _generic_ class has the parameters unrealized, e.g. Iterator
            // a _parametrized_ class has the parameters realized, e.g. Iterator<Integer>
            // the second matches against a required type with no type parameters
            // the first matches against a required type with the appropriate number of type parameters

            tc.assertNoTypeParameters();

            GenericClassDecl gcd = (GenericClassDecl) cdecl;
            for( TypeVariable typeVar : gcd.getTypeParameterList() ) {
                tc.addTypeParameter( new JastaddTypeParameterDescriptor( typeVar ).mapByScheme( scheme ) );
            }
        }

        tc.setSpecificType( new JastaddTypeDescriptor( cdecl ).mapByScheme( scheme ) );

        return tc;
    }

    static TypeConstraint fromReferenceTypeDecl( TypeDecl tdecl, ConcretificationScheme scheme ) {
        if( tdecl instanceof ClassDecl ) {
            return fromClassDecl( (ClassDecl) tdecl, scheme );
        }
        if( tdecl instanceof InterfaceDecl ) {
            return fromInterfaceDecl( (InterfaceDecl) tdecl, scheme );
        }
        return null;
    }

/*
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
*/

    public static RequiredType convertToRequiredType( String name, TypeConstraint tc, AST.ASTNode context ) {
        // TODO think about modifiers, these are discarded here
        RequiredType rv;
        AST.List<BodyDecl> bodyDecls = new AST.List<BodyDecl>();

        AST.Opt<Access> superClassAccess = new AST.Opt<Access>();
        AST.List<Access> superInterfaceAccess = new AST.List<Access>();

        AST.List<TypeVariable> typeParameters = new AST.List<TypeVariable>();
        // TODO: need special attention for _circular_ generic types.
        //       interface X<N extends X<N,C>, C extends Y<N,C> >
        //       interface Y<N extends X<N,C>, C extends Y<N,C> >
        //      this would loop infinitely creating new MethodDescriptor
        //      objects!
        // (on second thought, this wouldn't actually happen HERE, but
        // when MDs are created)
        

        // TODO revise
        // pay attention to duplicates etc

        Iterator<TypeDescriptor> extendedTypesI = tc.getExtendedTypesIterator();
        while( extendedTypesI.hasNext() ) {
            TypeDescriptor extendedType = extendedTypesI.next();
            if( extendedType instanceof JastaddTypeDescriptor ) {
                Access myAcc = ((JastaddTypeDescriptor) extendedType).getAccess();
                superClassAccess = new AST.Opt<Access>( (Access) myAcc.fullCopy() );
            }
        }

        Iterator<TypeDescriptor> implementedTypesI = tc.getImplementedTypesIterator();
        while( implementedTypesI.hasNext() ) {
            TypeDescriptor implementedType = implementedTypesI.next();
            if( implementedType instanceof JastaddTypeDescriptor ) {
                Access myAcc = ((JastaddTypeDescriptor) implementedType).getAccess();
                superInterfaceAccess = superInterfaceAccess.add( (Access) myAcc.fullCopy() );
            }
        }

        Iterator<TypeParameterDescriptor> typeParametersI = tc.getTypeParametersIterator();
        while( typeParametersI.hasNext() ) {
            System.out.println( "ADDING for copying A TYPE PARAMETER" );
            TypeParameterDescriptor typeParameter = typeParametersI.next();
            if( typeParameter instanceof JastaddTypeParameterDescriptor ) {
                TypeVariable myAcc = ((JastaddTypeParameterDescriptor) typeParameter).getTypeVariable();
                typeParameters = typeParameters.add( (TypeVariable) myAcc.fullCopy() );
            }
        }


        if( tc.mustBeClass() ) {
            rv = new RequiredClass( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess, typeParameters );
        } else if( tc.mustBeInterface() ) {
            rv = new RequiredInterface( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess, typeParameters );
        } else {
            rv = new RequiredType( new Modifiers(), name, bodyDecls, superClassAccess, superInterfaceAccess, typeParameters );
        }

        Set<String> addedSignatures = new HashSet<String> ();

        Iterator<MethodDescriptor> methodsI = tc.getMethodsIterator();
        while( methodsI.hasNext() ) {
            MethodDescriptor methodDesc = methodsI.next();
            BodyDecl bodyDecl = methodDescriptorToBodyDecl( methodDesc );
            bodyDecl.setParent( context );
            String signature = ((MethodDecl)bodyDecl).signature() + "::Method";

            if( !addedSignatures.contains( signature ) ) {
                System.out.println( "did add " + signature + " " + bodyDecl);
                addedSignatures.add( signature );
                rv.addBodyDecl( bodyDecl );
            } else {
                System.out.println( "ignored " + signature );
            }
        }

        Iterator<ConstructorDescriptor> constructorsI = tc.getConstructorsIterator();
        while( constructorsI.hasNext() ) {
            ConstructorDescriptor consDesc = constructorsI.next();
            BodyDecl bodyDecl = constructorDescriptorToBodyDecl( consDesc );
            bodyDecl.setParent( context );

            String signature = ((PTAbstractConstructor)bodyDecl).signature() + "::Constructor";
            if( !addedSignatures.contains( signature ) ) {
                addedSignatures.add( signature );
                System.out.println( "did add " + signature + " " + bodyDecl);
                rv.addBodyDecl( bodyDecl );
            } else {
                System.out.println( "ignored " + signature );
            }
        }


        System.out.println( "CRAFTED this required type:" + rv.dumpTree() );

        return rv;
    }

    public static BodyDecl methodDescriptorToBodyDecl( MethodDescriptor desc ) {
        Modifiers mods = new Modifiers();

        Access rtAccess = (Access) desc.getReturnType().getAccess().fullCopy();
        String name = desc.getName();
        AST.List<AST.ParameterDeclaration> parameters = new AST.List();
        AST.List throwsClause = new AST.List();
        AST.Opt<AST.Block> noBlock = new AST.Opt<AST.Block>();

        for(int i=0;i<desc.getArity();i++) {
            String parname = "p_" + (i+1);
            ParameterDeclaration par = new ParameterDeclaration( desc.getParameterType(i).getAccess(), parname );
            parameters.add( par );
        }

        BodyDecl rv = new AST.MethodDecl( mods,
                                        rtAccess,
                                        name,
                                        parameters,
                                        throwsClause,
                                        noBlock );

        return rv;
    }

    public static BodyDecl constructorDescriptorToBodyDecl( ConstructorDescriptor desc ) {
        Modifiers mods = new Modifiers();

        TypeAccess rtAccess = new AST.PrimitiveTypeAccess( "void" );
        AST.List<AST.ParameterDeclaration> parameters = new AST.List();
        AST.List throwsClause = new AST.List();
        AST.Opt<AST.Block> noBlock = new AST.Opt<AST.Block>();

        for(int i=0;i<desc.getArity();i++) {
            String parname = "p_" + (i+1);
            ParameterDeclaration par = new ParameterDeclaration( desc.getParameterType(i).getAccess(), parname );
            parameters.add( par );
        }

        return new AST.PTAbstractConstructor( mods,
                                              rtAccess,
                                              "$UNNAMED-CONSTRUCTOR$", // this gets corrected by a rewrite
                                              parameters,
                                              throwsClause,
                                              noBlock );
    }
}
