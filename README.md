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

JASTADD LICENSE
===============

The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
by the modified BSD License. You should have received a copy of the
modified BSD license with this compiler.

Copyright (c) 2005-2008, Torbjorn Ekman
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

   3. The name of the author may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
