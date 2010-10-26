// --------------------------------------------------------------------------------------------------
// Templat T2.  Brukes diverse ganger under.  Nesten lik T1. Skal være OK
// --------------------------
template T2 {
   class D {  int i; void m() { i = 4;} }
   class E extends D {int j; void n(int k){this.j = k;} }
}
