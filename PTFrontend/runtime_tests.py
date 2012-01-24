#!/usr/bin/env python
import optparse
import os
import sys
import subprocess
import filecmp
import shutil

class GenerationFailedException(Exception):
    def __init__(self):
        Exception.__init__(self, "generation failed")

class CompileFailedException(Exception):
    def __init__(self):
        Exception.__init__(self, "compilation failed")

class SubprocException(Exception):
    def __init__(self,name,msg):
        self.name = name
        super(SubprocException,self).__init__(msg)

class RuntimeTest(object):
    default_generate_name = 'src_output'
    default_test_ext = '.expected_output'
    default_actual_ext = '.actual_output'
    default_jar_loc = 'src_output/build/jar/src_output.jar'

    def __init__(self,path,generateCmd):
        self.name = os.path.basename(path)
        self.basePath = path
        self.generateCmd = generateCmd
        self.withErrors = []
        self.total = 0

    def generate(self):
        if not os.path.exists(self.basePath + '/src_output'):
            os.mkdir(self.basePath + '/src_output')
        proc = subprocess.Popen(self.generateCmd,cwd=self.basePath,
                                  stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
        output,error = proc.communicate()
        hasError = proc.wait()
        #ajj, JPT skriver ikke til stderr eller returner noen feilverdi (?)
        if hasError or not os.path.exists(self.basePath + '/src_output/build.xml'):
            print '\t'.ljust(40) + ': compilation error (JPT.jar).'
            raise SubprocException(self.name,error)

    def compile(self):
        retval = subprocess.call('ant',cwd=os.path.join(self.basePath,self.default_generate_name), stdout=open(os.devnull,'w'),stderr=subprocess.STDOUT)
        if retval != 0:
            print '\t'.ljust(40) + ': compilation error (javac).'
            raise CompileFailedException()

    @property
    def referenceOutputFiles(self):
        return [x for x in os.listdir(self.basePath)
                if x.endswith(self.default_test_ext)]

    @property
    def classesWithMain(self):
        return [x.rsplit('.',1)[0] for x in self.referenceOutputFiles]

    def asOutputName(self,name):
        return os.path.join(self.basePath,name + self.default_actual_ext)

    def asInputName(self,name):
        return os.path.join(self.basePath,name + self.default_test_ext)

    def runProgram(self):
        for x in self.classesWithMain:
            self.total += 1
            cmd = x
            jar = os.path.join(self.basePath,self.default_jar_loc)
            test_output_name = self.asOutputName(x)
            f = open(test_output_name,'w')
            rval = subprocess.call(['java','-cp',jar,cmd],stdout=f)
            assert rval == 0
            f.close()
            self.compare(x)

    def compare(self,basename):
        inputname = self.asInputName(basename)
        outputname = self.asOutputName(basename)
        print (('\t%s' % basename).ljust(40) + ':'),
        if filecmp.cmp(inputname,outputname):
            print 'ok.'
        else:
            self.withErrors.append((self.basePath,"Wrong output from " + basename))
            print 'failed...'

class RunTest(object):

    def __init__(self):
        self.total = 0
        self.withErrors = []

    def __call__(self,path,x):
        print '%s:' % x
        test = RuntimeTest(os.path.join(path,x),generateCmd=run_jpt)
        try:
            test.generate()
            test.compile()
            test.runProgram()
            self.withErrors.extend(test.withErrors)
        except (GenerationFailedException, SubprocException):
            self.withErrors.extend([ (test.basePath,"Error reported by JPT.jar") ])
        except OSError, e:
            self.withErrors.extend([ (test.basePath,e.strerror) ] )
        except CompileFailedException:
            self.withErrors.extend([ (test.basePath,"Error reported by javac") ] )
        self.total += test.total
    def printSummary(self):
        print '*'*10,'Summary','*'*10
        print 'total number of tests:'.ljust(30),self.total
        print 'passed:'.ljust(30),(self.total - len(self.withErrors))
        print 'failed:'.ljust(30),len(self.withErrors)
        print
        if len(self.withErrors):
            print 'The following tests failed:'
            for t in self.withErrors:
                print t[0] + ':'
                print '\t' + t[1]
        

class Cleanup(object):

    def __call__(self,path,x): 
        p = os.path.join(path,x)
        output_paths = [os.path.join(p,x)
                       for x in os.listdir(p)
                       if x.endswith(RuntimeTest.default_actual_ext)]
        output_src_path = os.path.join(p,'src_output')
        for x in output_paths:
            os.remove(x)
            print 'removed',x
        if os.path.exists(output_src_path):
            shutil.rmtree(output_src_path)
            print 'removed',output_src_path            

    def printSummary(self):
        print 'Done.'

parser = optparse.OptionParser(description='Compile and run tests in test/runtime_tests/.')
parser.add_option('--cleanup', dest='cleanup', action='store_true',default=False,

                   help='Perform Cleanup from previous tests')
parser.add_option("-s", "--single", default=False, dest="singleFile",help="Only test a single folder")

(options,args) = parser.parse_args()

if options.cleanup:
    f = Cleanup()
else:
    f = RunTest()

# Compute relative path to runtime tests
bpath = os.path.dirname(sys.argv[0])
path = os.path.join(bpath,'test','runtime_tests')
jpt_path = os.path.abspath(os.path.join(bpath,'build','jar','JPT.jar'))
run_jpt = ['java','-jar',jpt_path,'src']

if options.singleFile:
    assert os.path.isdir(os.path.join(path,options.singleFile))
    files = [options.singleFile]
else:
    files = os.listdir(path)

try:
    for x in files:
        f(path,x)
    f.printSummary()    
except SubprocException, e:
    print '%sError Message for: %s %s' % ('*'*10,e.name,'*'*10)
    print
    print e
    print
    print '%sEnd Error Message for: %s %s' % ('*'*10,e.name,'*'*10)
    print 'aborting'
    import sys
    sys.exit(1)

