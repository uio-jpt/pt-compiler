package javaworld;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import com.google.common.base.Joiner;

public class TypeConstraint {
    boolean canBeClass;
    boolean canBeInterface;

    Set<MethodDescriptor> methods;
    Set<ConstructorDescriptor> constructors;

    public Iterator<MethodDescriptor> getMethodsIterator() {
        return methods.iterator();
    }

    public Iterator<ConstructorDescriptor> getConstructorsIterator() {
        return constructors.iterator();
    }

    public boolean hasConstructor(ConstructorDescriptor desc) {
        return constructors.contains( desc );
    }

    public boolean hasMethod(MethodDescriptor desc) {
        return methods.contains( desc );
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "[type constraint: " );

        List<String> constraints = new Vector<String>();
        if( mustBeClass() ) {
            constraints.add( "must be class" );
        }
        if( mustBeInterface() ) {
            constraints.add( "must be interface" );
        }
        for( ConstructorDescriptor cd : constructors ) {
            constraints.add( "must have a constructor: " + cd );
        }
        for( MethodDescriptor md : methods ) {
            constraints.add( "must have a method: " + md );
        }

        Joiner.on( ", " ).appendTo( sb, constraints );

        sb.append( "]" );

        return sb.toString();
    }

    public TypeConstraint() {
        canBeClass = canBeInterface = true;
        methods = new HashSet<MethodDescriptor>();
        constructors = new HashSet<ConstructorDescriptor>();
    }

    public void addMethod(MethodDescriptor m) {
        methods.add( m );
    }

    public void addConstructor(ConstructorDescriptor c) {
        canBeInterface = false;
        constructors.add( c );
    }

    public void requireClass() {
        canBeInterface = false;
    }

    public void requireInterface() {
        canBeClass = false;
    }

    public boolean mustBeClass() {
        return !canBeInterface;
    }

    public boolean mustBeInterface() {
        return !canBeClass;
    }

    public TypeConstraint clone() {
        TypeConstraint tc = new TypeConstraint();
        tc.absorb( this );
        return tc;
    }

    public void absorb(TypeConstraint that) {
        if(that.mustBeClass()) {
            requireClass();
        }
        if(that.mustBeInterface()) {
            requireInterface();
        }
        for( ConstructorDescriptor cd : that.constructors ) {
            addConstructor( cd );
        }
        for( MethodDescriptor md : that.methods ) {
            addMethod( md );
        }
    }

    public TypeConstraint merge( TypeConstraint that ) {
        TypeConstraint rv = clone();
        rv.absorb( that );
        return rv;
    }

    public boolean satisfies( TypeConstraint constraint ) {
        if( !( (canBeClass && constraint.canBeClass)
               ||
               (canBeInterface && constraint.canBeInterface) ) ) {
            return false;
        }

        for( MethodDescriptor md : constraint.methods ) {
            // we must supply one method that conforms to md.
            boolean ok = false;
            for( MethodDescriptor cmd : methods ) {
                if( cmd.conformsTo( md ) ) {
                    ok = true;
                }
            }
            if( !ok ) return false;
        }

        for( ConstructorDescriptor cd : constraint.constructors ) {
            boolean ok = false;
            for( ConstructorDescriptor ccd : constructors ) {
                if( ccd.equals( cd ) ) {
                    ok = true;
                }
            }
            if( !ok ) return false;
        }

        return true;
    }
}
