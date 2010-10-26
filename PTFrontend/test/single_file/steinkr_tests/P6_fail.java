// --------------------------------------------------------------------------------------------------------------------------
// Pakke P6, merger T1 og T2.  Skifter ikke navn på variable i D og E.  Skulle gi kollisjon
// -----------------------
package P6 {
   inst T1 with A => X, B => Y;
   inst T2 with D => X ( void m() -> mm); E => Y ( void n() -> nn);
   class Y adds {
      void m(){ rY = new Y();} 
   }
}
