// --------------------------------------------------------------------------------------------------------------------------
// Pakke P5, merger T1 og T2.  Skulle gå helt bra, pga navneforandringer
// -----------------------
package P5 {
   inst T1 with A => X, B => Y;
   inst T2 with D => X (i->ii, void m() -> mm); E => Y (j -> jj; void n() -> nn);
   class Y adds {
      void m(){ rY = new Y();}  // Denne skulle bare override den def. i A
   }
}
