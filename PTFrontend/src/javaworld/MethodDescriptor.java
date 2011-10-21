package javaworld;

import java.util.Vector;
import java.util.List;

import com.google.common.base.Joiner;

public class MethodDescriptor {
    // no modifiers -- problem? TODO
    // also: no throws

    String name;
    TypeDescriptor returnType;
    List<TypeDescriptor> parameterTypes;

    public MethodDescriptor(String name, TypeDescriptor returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public ConstructorDescriptor toConstructorDescriptor() {
        return new ConstructorDescriptor( parameterTypes );
    }

    public MethodDescriptor(String name, TypeDescriptor returnType, List<TypeDescriptor> parameterTypes) {
        this( name, returnType );

        this.parameterTypes = new Vector<TypeDescriptor>();
        for( TypeDescriptor t : parameterTypes ) {
            this.parameterTypes.add( t );
        }
    }

    public String getName() {
        return name;
    }

    public int getArity() {
        return parameterTypes.size();
    }

    public TypeDescriptor getReturnType() {
        return returnType;
    }

    public TypeDescriptor getParameterType(int i) {
        return parameterTypes.get(i);
    }

    public boolean signatureEquals(MethodDescriptor that) {
        final int n = getArity();
        if( !name.equals( that.name ) ) return false;
        if( n != that.getArity() ) return false;
        for(int i=0;i<n;i++) {
            if( !getParameterType(i).equals( that.getParameterType(i) ) ) return false;
        }
        return true;
    }

    public boolean equals(MethodDescriptor that) {
        if( !signatureEquals( that ) ) return false;
        if( !returnType.equals( that.returnType ) ) return false;
        return true;
    }

    public boolean conformsTo( MethodDescriptor that ) {
        // Java semantics.
        // todo check more obscure things like throws clauses

        if( !signatureEquals( that ) ) return false; // sanity check

        if( !returnType.isSubtypeOf( that.returnType ) ) return false;

        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( returnType.toString() );
        sb.append( " " );
        sb.append( name );
        sb.append( "(" );
        Joiner.on( "," ).appendTo( sb, parameterTypes );
        sb.append( ")" );
        return sb.toString();
    }
}
