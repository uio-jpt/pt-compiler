PT HOWTO
========

The actual implementation is located in the directory `PTFrontend`.

    $ git clone git@github.com:uio-jpt/pt-compiler.git
    $ cd pt-compiler/PTFrontend


### Building the compiler

    $ ant jar

The jar file is created in the directory `PTFrontend/build/jar`.


### Running tests

    $ ant testall       # semantic tests
    $ ant testcompile   # test compiling valid tests


### Running the compiler

    $ java -jar build/jar/JPT.jar <inputfolder> [-o <folder_name>] [-v]

Output folder defaults to `{inputfolder}_output`.


  
## Usage Examples

In the following, input folder is `resources/sample_projects/test-pt`;
Output is `resources/sample_projects/test-pt_output`.

    $ java -jar build/jar/PTCompiler.jar resources/sample_projects/test-pt

Same as above, with verbose flag on:

    java -jar build/jar/PTCompiler.jar -v resources/sample_projects/test-pt

Outputfolder specified:

    $ java -jar build/jar/PTCompiler.jar resources/sample_projects/test-pt -o /tmp/pt-out



## Launcher script

There is also a bash script `run` that can be used instead of typing
`java -jar build/jar/JPT.jar`.

This script will pr default set output directory to `./output`.
For more information about this, use

    $ ./run -h


LICENSE
=======

Copyright 2012 © The SWAT project, OMS Group, Department of Informatics, 
University of Oslo.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.