<YYINITIAL> {
  "template"                       { return sym(Terminals.TEMPLATE); }
  "adds"                       { return sym(Terminals.ADDS); }
  "inst"                       { return sym(Terminals.INST); }
  "with"                       { return sym(Terminals.WITH); }
  "=>"                       { return sym(Terminals.RIGHTARROW); }   
  "->"                       { return sym(Terminals.SIMPLERIGHTARROW); }
  // jastaddj has a general rule about *. therefore needs something more specific.
  "(\*)"                     { return sym(Terminals.ASTERISK_PAR); } 
}
