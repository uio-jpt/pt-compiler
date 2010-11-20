template T3 {
   class D { tabstract void tm(); int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){ j = k;} }
}

// --------------------------------------------------------------------------------------------------------------------------
// Pakke P9, instansierer T3 uten å konkretisere metoden tm i utvidelsen av D (men i den til E)
// -----------------------
package P9 {
    inst T3 with E=>E(tm() -> tm);
  // Ingen adds-del til D
  class E adds { void tm() {j = 2; } }
}
