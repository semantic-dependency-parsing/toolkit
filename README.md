# SemEval 2014 Task on Broad-Coverage Semantic Dependency Parsing

This repository contains a basic Java toolkit intended to be used in connection with the SemEval 2014 Task on Broad-Coverage Semantic Dependency Parsing. Detailed information about the task can be found at this website:

[http://alt.qcri.org/semeval2014/task8/](http://alt.qcri.org/semeval2014/task8/)

## Downloading

The project is currently only available via Git. We plan to make a download available when the software has stabilized.

## Building

After checking out the project from the repository, you should be able to build it using ``ant''. (You need at least version 1.8, and your Java version should be at least 1.6.)

```
$ ant
'''

This will create a file ``dist/sdp.jar'' with the compiled classes. The jar can then be added to your classpath, whereby you will be able to use the provided classes in your own project. To see what is there, consult the Javadoc documentation in ``dist/javadoc''.

## Command-line tools

Some of the tools implemented in the project can be called from the command line. For this there is a convenience shell script called ``run.sh'', which you call with the name of the tool and any command-line arguments. Example:

```
$ sh run.sh Analyzer train/dm.sdp
'''
