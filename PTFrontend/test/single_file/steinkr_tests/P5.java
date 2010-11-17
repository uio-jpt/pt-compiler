template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; void n(int k){this.j = k;} }
}

template T2 {
   class D {  int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){this.j = k;} }
}

// ----------------------------------------------------------------------------------------------
// Pakke P5, merger T1 og T2.  Skulle gå helt bra, pga navneforandringer
// -----------------------
package P5 {
   inst T1 with A => X, B => Y;
   inst T2 with D => X (i->ii, m() -> mm), E => Y (j -> jj, n(int) -> nn);
   class Y adds {
      void m(){ Y rY = new Y();}  // Denne skulle bare override den def. i A
   }
}
