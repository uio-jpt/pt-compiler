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

    Set<TypeDescriptor> extendedTypes; // should ultimately only be one
    Set<TypeDescriptor> implementedTypes;

        // note: this class can represent constraints that no actual Java class can match
        //       a TypeConstraint merge e.g. resulting in multiple superclasses will
        //       go through, and then the result can be queried to determine that it is
        //       an impossible constraint and that the merge was thus illegal


    public Iterator<TypeDescriptor> getExtendedTypesIterator() {
        return extendedTypes.iterator();
    }

    public Iterator<TypeDescriptor> getImplementedTypesIterator() {
        return implementedTypes.iterator();
    }

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


        for( TypeDescriptor extendedType : extendedTypes ) {
            constraints.add( "must extend a type: " + extendedType );
        }

        for( TypeDescriptor implementedType : implementedTypes ) {
            constraints.add( "must implement a type: " + implementedType );
        }

        Joiner.on( ", " ).appendTo( sb, constraints );

        sb.append( "]" );

        return sb.toString();
    }

    public TypeConstraint() {
        canBeClass = canBeInterface = true;
        methods = new HashSet<MethodDescriptor>();
        constructors = new HashSet<ConstructorDescriptor>();

        implementedTypes = new HashSet<TypeDescriptor>();
        extendedTypes = new HashSet<TypeDescriptor>();
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

    public void addSuperType(TypeDescriptor td ) {
        boolean ignorable = false;
        Set<TypeDescriptor> madeRedundant = new HashSet<TypeDescriptor>();

        for( TypeDescriptor myTd : extendedTypes ) {
            if( td.isSubtypeOf( myTd ) ) {
                madeRedundant.add( td );
            } else if( myTd.isSubtypeOf( td ) ) {
                // if I'm already extending a subtype ( a more specific type )
               // then this is ignorable
               ignorable = true;
            }
        }
        System.out.println( "supertype is ignorable? " + ignorable );

        if( !ignorable ) {
            for( TypeDescriptor toRemove : madeRedundant ) {
                extendedTypes.remove( toRemove );
            }

            extendedTypes.add( td );
        }

        System.out.println( "no . ex tedned typ es now " + extendedTypes.size() );
    }

    public void addImplementedType( TypeDescriptor td ) {
        implementedTypes.add( td );
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


        for( TypeDescriptor td : that.extendedTypes ) {
            addSuperType( td );
        }

        for( TypeDescriptor td : that.implementedTypes ) {
            addImplementedType( td );
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

        System.out.println( "requirement: " + constraint );
        System.out.println( "candidate: " + this );

        for( TypeDescriptor mustExtend : constraint.extendedTypes ) {
            boolean okay = false;
            for( TypeDescriptor doesExtend : extendedTypes ) {
                if( doesExtend.isSubtypeOf( mustExtend ) ) {
                    okay = true;
                }
            }
            if( !okay ) return false;
        }

        for( TypeDescriptor mustImplement : constraint.implementedTypes ) {
            boolean okay = false;
            for( TypeDescriptor doesImplement : implementedTypes ) {
                if( doesImplement.isSubtypeOf( mustImplement ) ) {
                    okay = true;
                }
            }
            if( !okay ) return false;
        }

/*
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
*/

        return true;
    }
}
