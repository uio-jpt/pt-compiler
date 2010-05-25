#!/usr/bin/env python
import sys
import os
import subprocess

basedir = 'BASEDIR'
jarFolder = "build/jar"
lib = 'utils'
jarfilename = 'PTCompiler.jar'
txtFolder = "txt"
mainclass = "testutils.PTToJavaPackage"
genericBuildfile = os.path.join(basedir,txtFolder, "generic-build.xml")
dependencies = [os.path.join(basedir,lib,x) for x in
                ('jargs.jar','commons-io-1.4.jar')]

def usage():
    print "$ %s [-v] PT_input_folder [Java_output_folder]" % sys.argv[0]
    print
    print "input folder must exist, output folder may be created"
    sys.exit(1)

def error(msg):
    print "Error: %s." % msg
    usage()

args = sys.argv[1:]

verbose = "false"

try:
    if args[0] == '-v':
        verbose = "true"
        args.pop()
except IndexError:
    usage()


try:
    inputfolder = args[0]
except IndexError:
    usage()

try:
    outputfolder = args[1]
except IndexError:
    outputfolder = os.path.basename(inputfolder) + "_output"
    print  "Warning: No output folder specified. Using %s" % outputfolder

if not os.path.exists(inputfolder):
    error("input folder must be real")

if not os.path.exists(os.path.dirname(outputfolder) or "."):
    error("folder containing output folder must be real")

libFolder = os.path.join(basedir,lib)

jarname = os.path.join(basedir,'build','jar',jarfilename)
sourceOption = "--sourceFolder=%s" % inputfolder
outputOption = "--outputFolder=%s" % outputfolder
buildOption = "--buildXMLPath=%s" % genericBuildfile
verboseOption = "--verbose=%s" % verbose
classpathOptions = "%s" % (os.pathsep.join(dependencies + [jarname]))
callList = ["java","-cp",classpathOptions,mainclass,verboseOption,buildOption,sourceOption, outputOption]

subprocess.call(callList)
