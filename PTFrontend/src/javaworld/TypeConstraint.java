package javaworld;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import com.google.common.base.Joiner;

public class TypeConstraint {
    boolean canBeClass;
    boolean canBeInterface;

    Set<MethodDescriptor> methods;
    Set<ConstructorDescriptor> constructors;

    Set<TypeDescriptor> extendedTypes; // should ultimately only be one
    Set<TypeDescriptor> implementedTypes;

    ArrayList<TypeParameterDescriptor> typeParametersRequired;

        // note: this class can represent constraints that no actual Java class can match
        //       a TypeConstraint merge e.g. resulting in multiple superclasses will
        //       go through, and then the result can be queried to determine that it is
        //       an impossible constraint and that the merge was thus illegal

    TypeDescriptor specificType; // this can be null!

    public void assertNoTypeParameters() {
        // TODO!
    }

    public void addTypeParameter( TypeParameterDescriptor tpd ) {
        System.out.println( "adding type parameter " + tpd + " to " + this );
        typeParametersRequired.add( tpd );
    }

    public void setSpecificType( TypeDescriptor td ) {
        System.out.println( "setting specific type " + td );
        specificType = td;
    }

    public Iterator<TypeParameterDescriptor> getTypeParametersIterator() {
        return typeParametersRequired.iterator();
    }

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
        for( ConstructorDescriptor md : constructors ) {
            if( md.equals( desc ) ) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMethod(MethodDescriptor desc) {
        for( MethodDescriptor md : methods ) {
            if( md.equals( desc ) ) {
                return true;
            }
        }
        return false;
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

        typeParametersRequired = new ArrayList<TypeParameterDescriptor> ();
    }

    public void addMethod(MethodDescriptor m) {
        if( hasMethod( m ) ) {
            return;
        }
        System.out.println( "adding method " + m );
        methods.add( m );
    }

    public void addConstructor(ConstructorDescriptor c) {
        if( hasConstructor( c ) ) {
            return;
        }
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

        if( !ignorable ) {
            for( TypeDescriptor toRemove : madeRedundant ) {
                extendedTypes.remove( toRemove );
            }

            extendedTypes.add( td );
        }
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

        for( TypeParameterDescriptor td : that.typeParametersRequired ) {
            typeParametersRequired.add( td );
        }
    }

    public TypeConstraint merge( TypeConstraint that ) {
        TypeConstraint rv = clone();
        rv.absorb( that );
        return rv;
    }

    public boolean satisfiesOnTypeParameters( TypeConstraint constraint, ConcretificationScheme scheme ) {
        /* // special case not needed
        if( constraint.typeParametersRequired.isEmpty() && typeParametersRequired.isEmpty() ) {
            return true;
        }
        */

        Iterator<TypeParameterDescriptor> mine = typeParametersRequired.iterator();
        Iterator<TypeParameterDescriptor> constraining = constraint.typeParametersRequired.iterator();
        while( mine.hasNext() && constraining.hasNext() ) {
            System.out.println( "CHECKING TPS" );
            if( !mine.next().mapByScheme(scheme).equals( constraining.next().mapByScheme(scheme) ) ) {
                return false;
            }
        }
        if( mine.hasNext() ) return false;
        if( constraining.hasNext() ) return false;

        return true;
    }

    public boolean satisfies( TypeConstraint constraint, ConcretificationScheme scheme ) {
        System.out.println( "trying for satisfaction of: " + constraint );

        if( !( (canBeClass && constraint.canBeClass)
               ||
               (canBeInterface && constraint.canBeInterface) ) ) {
            System.out.println( "ruled out on type" );
            return false;
        }

        System.out.println( "checking satisfies?" );
        System.out.println( "requirement: " + constraint );
        System.out.println( "candidate: " + this );
        System.out.println( "candidate specific type: " + specificType );

        if( !satisfiesOnTypeParameters( constraint, scheme ) ) {
            return false;
        }

        for( MethodDescriptor md : constraint.methods ) {
            // we must supply one method that conforms to md.
            boolean ok = false;
            for( MethodDescriptor cmd : methods ) {
                if( cmd.conformsTo( md, scheme ) ) {
                    ok = true;
                }
            }
            if( !ok ) {
                System.out.println( "ruled out on " + md );
                return false;
            }
        }

        for( ConstructorDescriptor cd : constraint.constructors ) {
            boolean ok = false;
            for( ConstructorDescriptor ccd : constructors ) {
                if( ccd.conformsTo( cd, scheme ) ) {
                    ok = true;
                }
            }
            if( !ok ) {
                System.out.println( "ruled out on " + cd );
                return false;
            }
        }

        for( TypeDescriptor mustExtend : constraint.extendedTypes ) {
            boolean okay = false;
            if( specificType != null ) {
                System.out.println( "testing " + specificType + " vs " + mustExtend.mapByScheme( scheme ) );
                if( specificType.isSubtypeOf( mustExtend.mapByScheme( scheme ) ) ) {
                    okay = true;
                }
            }

            for( TypeDescriptor doesExtend : extendedTypes ) {
                if( doesExtend.isSubtypeOf( mustExtend.mapByScheme( scheme ) ) ) {
                    okay = true;
                }
            }
            if( !okay ) return false;
        }

        for( TypeDescriptor mustImplement : constraint.implementedTypes ) {
            boolean okay = false;

            if( specificType != null ) {
                System.out.println( "testing " + specificType + " vs " + mustImplement.mapByScheme( scheme ) );

                if( specificType.isSubtypeOf( mustImplement.mapByScheme( scheme ) ) ) {
                    okay = true;
                }
            }

            for( TypeDescriptor doesImplement : implementedTypes ) {
                if( doesImplement.isSubtypeOf( mustImplement.mapByScheme( scheme ) ) ) {
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
