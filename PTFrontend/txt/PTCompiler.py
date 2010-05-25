import sys
import os
import subprocess

basedir = '/home/eivindgl/code/pt-compiler/PTFrontend'
jarFolder = "build/jar"
lib = 'utils'
jarfilename = 'PTCompiler.jar'
txtFolder = "txt"
mainclass = "testutils.PTToJavaPackage"
genericBuildfile = os.path.join(basedir,txtFolder, "generic-build.xml")
dependencies = [os.path.join(basedir,lib,x) for x in
                ('jargs.jar','commons-io-1.4.jar')]

def usage():
    print "%s PT_input_folder Java_output_folder"
    print "input folder must exist, output folder may be created"
    sys.exit(1)

def error(msg):
    print "Error: %s." % msg
    usage()

try:
    inputfolder = sys.argv[1]
except IndexError:
    usage()

try:
    outputfolder = sys.argv[2]
except IndexError:
    outputfolder = inputfolder + "_output"
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
classpathOptions = "%s" % (os.pathsep.join(dependencies + [jarname]))

subprocess.call(["java","-cp",classpathOptions,mainclass,buildOption,sourceOption, outputOption])
