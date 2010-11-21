template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; void n(int k){this.j = k;} }
}

// -------------------------------------------------------------------------------------------------
// Pakke P2, bruker T1 to ganger med navneforandring i en. Skal være OK
// -----------------------
package P2 {
    inst T1 with A => AA, B => BB;
    inst T1;
    class C{
        int i; 
        void m(){ A rA = new A(); rA.m();  B rB = new B(); rB.n(4);  rB.i = 234; rB.j = i;}
        void m(int j){ AA rAA = new AA(); rAA.m();  BB rBB = new BB(); rBB.n(4);  rBB.i = 234; rBB.j = j;}
    }
}
