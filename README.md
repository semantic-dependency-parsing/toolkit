# SemEval 2014 Task on Broad-Coverage Semantic Dependency Parsing

This repository contains a basic Java toolkit intended to be used in connection with the SemEval 2014 Task on Broad-Coverage Semantic Dependency Parsing. Detailed information about the task can be found at this website:

[http://alt.qcri.org/semeval2014/task8/](http://alt.qcri.org/semeval2014/task8/)

## Downloading

The project is currently only available via Git. We plan to make a download available when the software has stabilized.

## Building

After checking out the project from the repository, you should be able to build it using `ant`. (You need at least version 1.8, and your Java version should be at least 1.6.)

	$ ant

This will create a file `dist/sdp.jar` with the compiled classes. The jar can then be added to your classpath, whereby you will be able to use the provided classes in your own project. To see what is there, consult the Javadoc documentation in `dist/javadoc`.

## Command-line tools

Some of the tools implemented in the project can be called from the command line. For this there is a convenience shell script called `run.sh`, which you call with the name of the tool and any command-line arguments. The most revelant example is the evaluator (scorer) tool:

	$ sh run.sh Evaluator gold.sdp system.sdp

This will evaluate the parser output in the file `system.sdp` based on the gold-standard analyses in the file `gold.sdp`. The central figures are LP (labeled precision), LR (labeled recall), and LF (labeled F1), as defined on the [http://alt.qcri.org/semeval2014/task8/index.php?id=evaluation](Evaluation page).

Here is a sample output:

	# Evaluation
	
	Gold standard file: baseline/dm.gold.sdp
	System output file: baseline/dm.mate.sdp
	
	## Scores including virtual dependencies to top nodes
	
	Number of edges in gold standard: 27778
	Number of edges in system output: 13598
	Number of edges in common, labeled: 11313
	Number of edges in common, unlabeled: 11596
	
	LP: 0.831961
	LR: 0.407265
	LF: 0.546839
	
	## Scores excluding virtual dependencies to top nodes
	
	Number of edges in gold standard: 26165
	Number of edges in system output: 13598
	Number of edges in common, labeled: 11313
	Number of edges in common, unlabeled: 11596
	
	LP: 0.831961
	LR: 0.432371
	LF: 0.569021
