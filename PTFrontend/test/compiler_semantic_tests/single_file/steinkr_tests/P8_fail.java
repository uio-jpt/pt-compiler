template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; void n(int k){this.j = k;} }
}

template T2 {
   class D {  int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){this.j = k;} }
}

// --------------------------------------------------------------------------------------------------------------------------
// Pakke P8, merger T1 og T2.  Merger B og E uten å merge superklassene A og D
// -----------------------
package P8 {
   inst T1 with A => X, B => Y;
   inst T2 with D => Z, E => Y (j -> jj, n(int) -> nn);
  class X adds {  }
  class Y adds {  }
  class Z adds {  }
}
