PT HOWTO
--------

Implementasjonen ligger i mappen PTFrontend. 

$ git clone git@github.com:eivindgl/pt-compiler.git
$ cd pt-compiler/PTFrontend

# Kjøring av tester:
$ ant testall # semantikksjekk
$ ant testcompile # test kompilering av gyldige passerte tester

# Bygging av kompilator:
$ ant jar
# jar filen opprettes i mappen PTFrontend/build/jar/

# Kjøring av kompilator
# generelt slik:
$ java -jar build/jar/JPT.jar <inputfolder> [-o <folder_name>] [-v]
  
# Usage Examples:
#
$ cd pt-compiler/PTFrontend
#
# outputfolder defaults to {inputfolder}_output
# in this case it's resources/sample_projects/test-pt_output
$ java -jar build/jar/PTCompiler.jar resources/sample_projects/test-pt
#
# same as above, with verbose flag on.
$ java -jar build/jar/PTCompiler.jar -v resources/sample_projects/test-pt
#
# outputfolder specified
$ java -jar build/jar/PTCompiler.jar resources/sample_projects/test-pt -o /tmp/pt-out

