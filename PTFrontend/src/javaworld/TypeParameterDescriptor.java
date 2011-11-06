package javaworld;

public interface TypeParameterDescriptor {
    boolean equals( TypeParameterDescriptor that );
    boolean subtypeOf( TypeParameterDescriptor that );

    TypeParameterDescriptor mapByScheme( ConcretificationScheme scheme );
}
