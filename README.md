# Bayesian Rule Learning
Bayesian Rule Learning (BRL) is a rule learning algorithm that searches over a space of Bayesian networks, and infers explicit propositional rules from the optimal Bayesian network. For explanation of the algorithm please read references [1,2,3].

## Usage
Please see test files to understand how to use BRL classifiers for your data. For example, org.probe.rls.test.algo.TestBayesianRuleLearning.java shows how to use BRL with local structure search [2]. To understand how to learn Ensemble Bayesian Rule Learning algorithms, please see test file: org.probe.rls.test.algo.TestEnsembleBayesianRuleLearning.java

## Reference
1.  Balasubramanian JB, Gopalakrishnan V. Tunable structure priors for Bayesian rule learning for knowledge integrated biomarker discovery. World journal of clinical oncology. 2018 Sep 14;9(5):98.
2.  Lustgarten J, Balasubramanian J, Visweswaran S, Gopalakrishnan V. Learning parsimonious classification rules from gene expression data using bayesian networks with local structure. Data. 2017 Mar;2(1):5.
3.  Gopalakrishnan V, Lustgarten JL, Visweswaran S, Cooper GF. Bayesian rule learning for biomedical data mining. Bioinformatics. 2010 Mar 1;26(5):668-75.
