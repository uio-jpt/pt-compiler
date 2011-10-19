package javaworld;

import java.util.List;
import java.util.Vector;

import AST.Access;
import AST.ParTypeAccess;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.Wildcard;

import com.google.common.base.Joiner;

public class JastaddTypeDescriptor implements TypeDescriptor {
    TypeAccess typeAccess;
    ParTypeAccess parTypeAccess;

    boolean byDeclaration;
    TypeDecl typeDeclaration;

    boolean isWildcard;

    List<JastaddTypeDescriptor> typeParameters;

    public JastaddTypeDescriptor( Access acc ) {
        /* Note that we keep a reference to the actual access.
           We make the assumption that this is not later mutated
           in ways significant to us.
           (Making a copy extracted from the AST seems like it
            would pose a problem because we later do comparison
            by e.g. calling .decl().)
        */

        typeAccess = null;
        parTypeAccess = null;

        typeParameters = new Vector<JastaddTypeDescriptor>();

        if( acc instanceof TypeAccess ) {
            typeAccess = (TypeAccess) acc;
        } else if( acc instanceof ParTypeAccess ) {
            parTypeAccess = (ParTypeAccess) acc;
            final int n = parTypeAccess.getNumTypeArgument();
            for(int i=0;i<n;i++) {
                typeParameters.add( new JastaddTypeDescriptor( parTypeAccess.getTypeArgument(i) ) );
            }
        } else if( acc instanceof Wildcard ) {
            isWildcard = true;
        } else {
            throw new RuntimeException( "access of class " + acc.getClass().getName() + " passed to JastaddTypeDescriptor does not describe a type in any known way" );
        }
    }

    public JastaddTypeDescriptor( TypeDecl decl ) {
        byDeclaration = true;
        typeDeclaration = decl;
        typeParameters = new Vector<JastaddTypeDescriptor>();
    }

    public Access getAccess() {
        if( byDeclaration ) {
            System.out.println( "[warning] constructing access" );
            return new TypeAccess( typeDeclaration.fullName() );
        }
        if( isWildcard ) {
            new Wildcard();
        }
        if( isParametrized() ) {
            return parTypeAccess;
        }
        return typeAccess;
    }

    public TypeDecl getBaseTypeDecl() {
        if( byDeclaration ) {
            return typeDeclaration;
        }
        if( isParametrized() ) {
            return parTypeAccess.genericDecl();
        }
        return typeAccess.decl();
    }

    public TypeDecl getTypeDecl() {
        if( byDeclaration ) {
            return typeDeclaration;
        }
        return typeAccess.decl();
    }

    public boolean isParametrized() {
        return parTypeAccess != null;
    }

    public int getArity() {
        return typeParameters.size();
    }


    public boolean equals( TypeDescriptor that ) {
        if( !(that instanceof JastaddTypeDescriptor) ) return false;

        JastaddTypeDescriptor jt = (JastaddTypeDescriptor) that;
        
        if( isWildcard != jt.isWildcard ) return false;

        if( isParametrized() != jt.isParametrized() ) return false;

        if( getBaseTypeDecl() != jt.getBaseTypeDecl() ) return false;

        if( getArity() != jt.getArity() ) return false;

        for(int i=0;i<getArity();i++) {
            if( !typeParameters.get(i).equals( jt.typeParameters.get(i) ) ) return false;
        }

        return true;
    }

    public boolean isSubtypeOf( TypeDescriptor that ) {
        // note that we can be subtypes of TypeConstraints, not just other JastaddTypeDescriptors

        // correction: no, this is no longer possible. (must it be?)

        if( !(that instanceof JastaddTypeDescriptor) ) {
            return false;
        }
        JastaddTypeDescriptor jthat = (JastaddTypeDescriptor) that;
        TypeDecl myDecl = getTypeDecl();
        TypeDecl theirDecl = jthat.getTypeDecl();

        boolean rv =  myDecl.subtype( theirDecl );

        System.out.println( "is " + myDecl.fullName() + " a subtype of " + theirDecl.fullName() + "? " + rv );
        return rv;
    }

    public String toString() {
        if( isWildcard ) {
            return "?";
        }

        StringBuilder sb = new StringBuilder();

        sb.append( getBaseTypeDecl().fullName() );
        if( isParametrized() ) {
            sb.append( "<" );
            Joiner.on( "," ).appendTo( sb, typeParameters );
            sb.append( ">" );
        }
        return sb.toString();
    }
}
