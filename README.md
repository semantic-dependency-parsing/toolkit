# Semantic Dependency Parsing Toolkit

This repository contains a Java toolkit for semantic dependency parsing. It has been developed in connection with two shared tasks:

* [SemEval-2014 Task on Broad-Coverage Semantic Dependency Parsing](http://alt.qcri.org/semeval2014/task8/)
* [SemEval-2015 Task on Broad-Coverage Semantic Dependency Parsing](http://alt.qcri.org/semeval2015/task18/)

Detailed information about the tasks can be found at the respective websites.

## Downloading

The project is currently only available via Git.

## Building

After checking out the project from the repository, you should be able to build it using [Gradle](http://www.gradle.org/).

	$ cd toolkit
	$ gradle build

This will create a file `build/libs/sdp.jar` with the compiled classes. The jar can then be added to your classpath, whereby you will be able to use the provided classes in your own project. To see what is there, build the documentation:

	$ gradle javadoc

The entry page for the documentation is `build/docs/javadoc/index.html`.

## Command-line tools

Some of the tools implemented in the project can be called from the command line. For this there is a convenience shell script called `run.sh`, which you call with the name of the tool and any command-line arguments. The most revelant example is the `Scorer` tool, which is run as follows:

	$ sh run.sh Scorer gold.sdp system.sdp representation=DM

This will evaluate the parser output in the file `system.sdp` based on the gold-standard analyses in the file `gold.sdp` based on the assumption that the data is given in the `DM` representation; other possible representations are `PAS` and `PSD`. The evaluation metrics used are defined on the [Evaluation page](http://alt.qcri.org/semeval2015/task18/index.php?id=evaluation).

Abbreviations:

	LP: labeled precision
	LR: labeled recall
	LF: labeled F1
	LM: labeled exact match
	
	UP: unlabeled precision
	UR: unlabeled recall
	UF: unlabeled F1
	UM: unlabeled exact match
	
	SFP: precision with respect to semantic frames
	SFR: recall with respect to semantic frames
	SFF: F1 with respect to semantic frames
	
	CPP: precision with respect to complete predications
	CPR: recall with respect to complete predications
	CPF: F1 with respect to complete predications
