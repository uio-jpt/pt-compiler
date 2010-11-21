template T3 {
   class D { tabstract void tm(); int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){ j = k;} }
}

// --------------------------------------------------------------------------------------------------------------------------
// Pakke P9, instansierer T3 og konkretiserer metoden tm i utvidelsen av D 
// -----------------------
package P9 {
   inst T3;
   
  class D adds { void tm() {i = 2; } }
}
