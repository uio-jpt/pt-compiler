package SparseMatrix { 
    inst LinkedList with 
       List => ListOfLists, 
       Elem => ElemList; 
    
    class ListOfLists adds{ /* ... */ } 
    class ElemList adds{ /* ... */ }

    inst LinkedList with List => ElemList, Elem => Person; 

    class Person adds { /* ... */ }
}
