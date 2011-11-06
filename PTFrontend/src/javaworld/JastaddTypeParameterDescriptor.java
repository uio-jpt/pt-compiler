package javaworld;

import AST.TypeVariable;
import AST.TypeDecl;
import AST.List;
import AST.TypeAccess;

import java.util.Map;

public class JastaddTypeParameterDescriptor implements TypeParameterDescriptor {
    TypeVariable parameter;

    public JastaddTypeParameterDescriptor( TypeVariable parameter ) {
        this.parameter = parameter;
    }

    public boolean equals( TypeParameterDescriptor that ) {
        if( !(that instanceof JastaddTypeParameterDescriptor) ) {
            return false;
        }
        System.out.println( "checking equality between " + parameter + " and " + ((JastaddTypeParameterDescriptor)that).parameter );
        System.out.println( "checking subtypes: " + subtypeOf( that ) + " " + that.subtypeOf( this ) );

        return subtypeOf( that ) && that.subtypeOf( this );
    }

    public boolean subtypeOf( TypeParameterDescriptor that ) {
        if( !(that instanceof JastaddTypeParameterDescriptor) ) {
            return false;
        }

        // TODO check whether this is the right way, it's one of those ambiguous names
        return ((JastaddTypeParameterDescriptor)that).parameter.supertypeTypeVariable( parameter );
    }

    public TypeParameterDescriptor mapByScheme( ConcretificationScheme scheme ) {
        TypeVariable myVariable = parameter.fullCopy();

        Map<TypeDecl, TypeAccess> dtaMap = scheme.createDeclToAccessMap();

        // hack to make sure we can reuse this method to replace roots as well
        AST.List parent = new AST.List();
        parent.setParent( parameter.getParent() );
        parent.addChild( myVariable );
        parent.replaceTypeAccesses( dtaMap );
        myVariable = (TypeVariable) parent.getChild(0);

        return new JastaddTypeParameterDescriptor( myVariable );
    }

    public TypeVariable getTypeVariable() {
        return parameter;
    }
}
