template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; n(int k){this.j = k;} }
}

template T2 {
   class D {  int i; void m() { i = 4;} }
   class E extends D {int j; n(int k){this.j = k;} }
}

// --------------------------------------------------------------------------------------------------------------------------
// Pakke P6, merger T1 og T2.  Skifter ikke navn på variable i D og E.  Skulle gi kollisjon
// -----------------------
package P6 {
   inst T1 with A => X, B => Y;
   inst T2 with D => X (  m() -> mm), E => Y (  n(int) -> nn);
   class Y adds {
      void m(){ Y rY = new Y();} 
   }
}
