// --------------------------------------------------------------------------------------------------------------------------
// Pakke P7, merger T1 og T2.  Skifter navn på variable, men ikke på metodene.  Går ikke bra?
// -----------------------
package P5 {
   inst T1 with A => X, B => Y;
   inst T2 with D => X (i->ii); E => Y (j -> jj);
   class Y adds {
      void m(){ rX = new Y();}  // Om det over går bra, så må vel denne også med
      // overrider IKKE  void m().  Skulle hvertfall gå galt, selv om det over går bra
   }
}
