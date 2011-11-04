package javaworld;

import java.util.Vector;
import java.util.List;

import com.google.common.base.Joiner;

public class ConstructorDescriptor {
    List<TypeDescriptor> parameterTypes;

    public ConstructorDescriptor(List<TypeDescriptor> parameterTypes) {
        this.parameterTypes = new Vector<TypeDescriptor>();
        for( TypeDescriptor t : parameterTypes ) {
            this.parameterTypes.add( t );
        }
    }

    public int getArity() {
        return parameterTypes.size();
    }

    public TypeDescriptor getParameterType(int i) {
        return parameterTypes.get(i);
    }

    public boolean equals(ConstructorDescriptor that) {
        final int n = getArity();
        if( n != that.getArity() ) return false;
        for(int i=0;i<n;i++) {
            if( !getParameterType(i).equals( that.getParameterType(i) ) ) return false;
        }
        return true;
    }

    public boolean conformsTo(ConstructorDescriptor that, ConcretificationScheme scheme) {
        final int n = getArity();
        if( n != that.getArity() ) return false;
        for(int i=0;i<n;i++) {
            if( !getParameterType(i).mapByScheme(scheme).equals( that.getParameterType(i).mapByScheme(scheme) ) ) return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "<constructor>(" );
        Joiner.on( "," ).appendTo( sb, parameterTypes );
        sb.append( ")" );
        return sb.toString();
    }

    void applyScheme( ConcretificationScheme scheme ) {
        List<TypeDescriptor> newParamTypes = new Vector<TypeDescriptor>();
        for( TypeDescriptor td : parameterTypes ) {
            newParamTypes.add( td.mapByScheme( scheme ) );
        }
        parameterTypes = newParamTypes;
    }
}
