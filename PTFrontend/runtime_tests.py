#!/usr/bin/env python
import optparse
import os
import sys
import subprocess
import filecmp
import shutil

class RuntimeTest(object):
    default_generate_name = 'src_output'
    default_test_ext = '.expected_output'
    default_actual_ext = '.acutal_output'
    default_jar_loc = 'src_output/build/jar/src_output.jar'

    def __init__(self,path,generateCmd):
        self.name = os.path.basename(path)
        self.basePath = path
        self.generateCmd = generateCmd
        self.withErrors = []
        self.total = 0

    def generate(self):
        retval = subprocess.call(self.generateCmd,cwd=self.basePath,stdout=open(os.devnull,'w'),stderr=subprocess.STDOUT)
        assert retval == 0

    def compile(self):
        retval = subprocess.call('ant',cwd=os.path.join(self.basePath,self.default_generate_name), stdout=open(os.devnull,'w'),stderr=subprocess.STDOUT)

    @property
    def referenceOutputFiles(self):
        return [x for x in os.listdir(self.basePath)
                if x.endswith(self.default_test_ext)]

    @property
    def classesWithMain(self):
        return [x.split('.')[0] for x in self.referenceOutputFiles]

    def asOutputName(self,name):
        return os.path.join(self.basePath,name + self.default_actual_ext)

    def asInputName(self,name):
        return os.path.join(self.basePath,name + self.default_test_ext)

    def runProgram(self):
        for x in self.classesWithMain:
            self.total += 1
            cmd = '%s.%s' % (self.name,x)
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
        print (('\t%s.%s' % (self.name,basename)).ljust(20) + ':'),
        if filecmp.cmp(inputname,outputname):
            print 'ok.'
        else:
            self.withErrors.append((self.basePath,basename))
            print 'failed...'

class RunTest(object):

    def __init__(self):
        self.total = 0
        self.withErrors = []

    def __call__(self,path,x):
        test = RuntimeTest(os.path.join(path,x),generateCmd=run_jpt)
        test.generate()
        test.compile()
        test.runProgram()
        self.total += test.total
        self.withErrors.extend(test.withErrors)

    def printSummary(self):
        print '*'*10,'Summary','*'*10
        print 'total number of tests:'.ljust(30),self.total
        print 'passed:'.ljust(30),(self.total - len(self.withErrors))
        print 'failed:'.ljust(30),len(self.withErrors)
        print
        if len(self.withErrors):
            print 'see the following files for more information on the failed tests:'
            out = [os.path.join(bp,'{%s%s,%s%s}' % (name,RuntimeTest.default_test_ext,
                                                    name,RuntimeTest.default_actual_ext))
                   for (bp,name) in self.withErrors]
            print '\n'.join('\t' + x for x in out)
        

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

for x in os.listdir(path):
    f(path,x)
f.printSummary()    