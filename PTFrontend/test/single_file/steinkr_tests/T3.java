// --------------------------------------------------------------------------------------------------
// Templat T3.  Brukes diverse ganger under.  Nesten lik T2, men med en ”tabstract tm();” i D. Skal være OK
// --------------------------
template T3 {
   class D { tabstract void tm(); int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){ j = k;} }
}
