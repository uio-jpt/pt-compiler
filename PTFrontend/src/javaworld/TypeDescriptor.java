package javaworld;

public interface TypeDescriptor {
    boolean equals( TypeDescriptor that );
    boolean isSubtypeOf( TypeDescriptor that );

    AST.Access getAccess();
    TypeDescriptor mapByScheme( ConcretificationScheme scheme );
}
