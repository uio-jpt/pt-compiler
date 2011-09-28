template T1 {
   class A {  int i; void m() { i = 3;} }
   class B extends A {int j; void n(int k){this.j = k;} }
}

// --------------------------------------------------------------------------------------------------------------------------
// Pakke P4, bruker T1, med C som utvidelse av B.  Bør gå bra, men litt uklart …
// -----------------------
package P4 {
   inst T1 with B => C;
   class C adds {
       /*
         int j;   // Gir dette kollisjon med ”j” i B?  Bør vel helst ikke det, men jeg tror vi har vaklet litt her
       */
       void n (int k) { System.out.println("override?"); }
      void m(){ C rC = new C();}  // Denne skulle bare override den def. i A
   }
}
