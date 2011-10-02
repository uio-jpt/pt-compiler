/* NB: Remember to clean/rebuild. The parser will behave strange otherwise.
*/
<YYINITIAL> {
  "template"                       { return sym(Terminals.TEMPLATE); }
  "adds"                       { return sym(Terminals.ADDS); }
  "inst"                       { return sym(Terminals.INST); }
  "with"                       { return sym(Terminals.WITH); }
  "=>"                       { return sym(Terminals.RIGHTARROW); }   
  "->"                       { return sym(Terminals.SIMPLERIGHTARROW); }
  // jastaddj has a general rule about *. therefore needs something more speci
  "(\*)"                     { return sym(Terminals.ASTERISK_PAR); }
  "tsuper"                        { return sym(Terminals.TSUPER); }
  "tabstract"                { return sym(Terminals.TABSTRACT); }
  "external"                 { return sym(Terminals.EXTERNAL); }
  "assumed"                 { return sym(Terminals.ASSUMED); }
}
