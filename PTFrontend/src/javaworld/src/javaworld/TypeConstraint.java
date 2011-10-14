package javaworld;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import com.google.common.base.Joiner;

public class TypeConstraint {
    boolean canBeClass;
    boolean canBeInterface;

    Set<MethodDescriptor> methods;
    Set<ConstructorDescriptor> constructors;


        // these are internal. they might be renamed. they are short names, e.g. "A"
    Set<String> nominalImplementedInterfacesInternal;
    Set<String> nominalExtendedClassesInternal;

        // these are external. they will NOT be renamed. they are fullNames, e.g. "java.lang.Runnable"
    Set<String> nominalImplementedInterfacesExternal;
    Set<String> nominalExtendedClassesExternal;

        // note: this class can represent constraints that no actual Java class can match
        //       a TypeConstraint merge e.g. resulting in multiple superclasses will
        //       go through, and then the result can be queried to determine that it is
        //       an impossible constraint and that the merge was thus illegal


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

    public void addExternalInterfaceName( String name ) {
        nominalImplementedInterfacesExternal.add( name );
    }

    public void addInternalInterfaceName( String name ) {
        nominalImplementedInterfacesInternal.add( name );
    }

    public void addExternalSuperclassName( String name ) {
        nominalExtendedClassesExternal.add( name );
    }

    public void addInternalSuperclassName( String name ) {
        nominalExtendedClassesInternal.add( name );
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

        if( ! nominalImplementedInterfacesInternal.isEmpty() ) {
            constraints.add( "must extend internal interface(s): " + Joiner.on( ", " ).join( nominalImplementedInterfacesInternal ) );
        }

        if( ! nominalImplementedInterfacesExternal.isEmpty() ) {
            constraints.add( "must implement external interface(s): " + Joiner.on( ", " ).join( nominalImplementedInterfacesExternal ) );
        }
        
        if( ! nominalExtendedClassesInternal.isEmpty() ) {
            constraints.add( "must extend internal class(es): " + Joiner.on( ", " ).join( nominalExtendedClassesInternal ) );
        }

        if( ! nominalExtendedClassesExternal.isEmpty() ) {
            constraints.add( "must extend external class(es): " + Joiner.on( ", " ).join( nominalExtendedClassesExternal ) );
        }

        Joiner.on( ", " ).appendTo( sb, constraints );

        sb.append( "]" );

        return sb.toString();
    }

    public TypeConstraint() {
        canBeClass = canBeInterface = true;
        methods = new HashSet<MethodDescriptor>();
        constructors = new HashSet<ConstructorDescriptor>();

        nominalImplementedInterfacesInternal = new HashSet<String>();
        nominalImplementedInterfacesExternal = new HashSet<String>();
        nominalExtendedClassesInternal = new HashSet<String>();
        nominalExtendedClassesExternal = new HashSet<String>();
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

    public void applyRenames( Map<String,String> renames ) {
        HashSet<String> newImplemented = new HashSet<String>();
        HashSet<String> newExtended = new HashSet<String>();

        for( String key : nominalImplementedInterfacesInternal ) {
            if( renames.keySet().contains( key ) ) {
                newImplemented.add( renames.get( key ) );
            } else {
                newImplemented.add( key );
            }
        }

        for( String key : nominalExtendedClassesInternal ) {
            if( renames.keySet().contains( key ) ) {
                newExtended.add( renames.get( key ) );
            } else {
                newExtended.add( key );
            }
        }

        nominalImplementedInterfacesInternal = newImplemented;
        nominalExtendedClassesInternal = newExtended;
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
        for( String name : that.nominalImplementedInterfacesInternal ) {
            nominalImplementedInterfacesInternal.add( name );
        }
        for( String name : that.nominalExtendedClassesInternal ) {
            nominalExtendedClassesInternal.add( name );
        }
        for( String name : that.nominalImplementedInterfacesExternal ) {
            nominalImplementedInterfacesExternal.add( name );
        }
        for( String name : that.nominalExtendedClassesExternal ) {
            nominalExtendedClassesExternal.add( name );
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

        for( String name : constraint.nominalExtendedClassesExternal ) {
            if( !nominalExtendedClassesExternal.contains( name ) ) {
                return false;
            }
        }

        for( String name : constraint.nominalExtendedClassesInternal ) {
            if( !nominalExtendedClassesInternal.contains( name ) ) {
                return false;
            }
        }

        for( String name : constraint.nominalImplementedInterfacesExternal ) {
            if( !nominalImplementedInterfacesExternal.contains( name ) ) {
                return false;
            }
        }

        for( String name : constraint.nominalImplementedInterfacesInternal ) {
            if( !nominalImplementedInterfacesInternal.contains( name ) ) {
                return false;
            }
        }

        return true;
    }
}
