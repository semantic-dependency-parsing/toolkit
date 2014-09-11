# Semantic Dependency Parsing Toolkit

This repository contains a Java toolkit for semantic dependency parsing. It has been developed in connection with two shared tasks:

* [SemEval-2014 Task on Broad-Coverage Semantic Dependency Parsing](http://alt.qcri.org/semeval2014/task8/)
* [SemEval-2015 Task on Broad-Coverage Semantic Dependency Parsing](http://alt.qcri.org/semeval2015/task18/)

Detailed information about the tasks can be found at the respective websites.

## Downloading

The project is currently only available via Git.

## Building

After checking out the project from the repository, you should be able to build it using `ant`. (You need at least version 1.8, and your Java version should be at least 1.6.)

	$ ant

This will create a file `dist/sdp.jar` with the compiled classes. The jar can then be added to your classpath, whereby you will be able to use the provided classes in your own project. To see what is there, consult the Javadoc documentation in `dist/javadoc`.

## Command-line tools

Some of the tools implemented in the project can be called from the command line. For this there is a convenience shell script called `run.sh`, which you call with the name of the tool and any command-line arguments. The most revelant example is the `Scorer` tool, which is run as follows:

	$ sh run.sh Scorer gold.sdp system.sdp

This will evaluate the parser output in the file `system.sdp` based on the gold-standard analyses in the file `gold.sdp`. The evaluation metrics used are defined on the [Evaluation page](http://alt.qcri.org/semeval2014/task8/index.php?id=evaluation).

Abbreviations:

	LP: labeled precision
	LR: labeled recall
	LF: labeled F1
	LM: labeled exact match
	
	UP: unlabeled precision
	UR: unlabeled recall
	UF: unlabeled F1
	UM: unlabeled exact match
