template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; void n(int k){this.j = k;} }
}


// -------------------------------------------------------------------------------------------------
// Pakke P1, bruker T1. Skal være OK
// -----------------------
package P1 {
   inst T1;
   class C{
       int i; 
       void m(){
           A rA = new A();
           rA.m();
           B rB = new B();
           rB.n(4);
           rB.i = 234;
           rB.j = i;}
   }
}
