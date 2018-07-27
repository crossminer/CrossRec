[![DOI](https://zenodo.org/badge/134830691.svg)](https://zenodo.org/badge/latestdoi/134830691)

This repository contains the source code implementation of CrossRec, the dataset as well as the experimental results for the following papers:

<b>Mining Software Repositories to Support OSS Developers: A Recommender	Systems Approach</b>

Juri Di Rocco, Phuong T. Nguyen, Davide Di Ruscio

Department of Information Engineering, Computer Science and Mathematics,
Università degli Studi dell'Aquila

Proceedings of the 9th Italian Information Retrieval Workshop, Rome, Italy, May, 28-30, 2018

and:

<b>"A collaborative-filtering approach to third-party library recommendation"</b>

Juri Di Rocco, Phuong T. Nguyen, Davide Di Ruscio

Department of Information Engineering, Computer Science and Mathematics,
Università degli Studi dell'Aquila

submitted to the Information and Software Technology journal.


CrossRec is a framework that exploits cross projects relationships in open source software repositories to build a recommender system. It aims at supporting software developers who have already included some libraries in the new projects being developed, and expect to get recommendations on which additional libraries should be further incorporated. The CrossRec architecture is as follows:

<p align="center">
<img src="https://github.com/crossminer/CrossRec/blob/master/images/CrossRec.png" width="450">
</p>

The developer interacts with the system by sending a request for recommendations. The request contains a list of libraries that are already included in the project the developer is working on. The <b>Data Encoder</b> collects <i> background data </i> from OSS repositories, represents them in a <i>mathematically computable format</i>, which is then used as a base for other components of CrossRec. The graph representation is depicted in the following figure.

<p align="center">
<img src="https://github.com/crossminer/CrossRec/blob/master/images/Graph.png" width="450">
</p>

The <b> Similarity Calculator</b> module computes similarities among projects to find the most similar ones to the given project. The <b>Recommendation Engine</b> gets the list of <i> top-k</i> similar projects and executes recommendation techniques to generate a ranked list of <i> top-N </i> libraries. Finally, the recommendations are sent back to the developer. Background data can be collected from different OSS platforms like GitHub, Eclipse, BitBucket. The current version of CrossRec supports data extraction from GitHub, even though the support for additional platforms is already under development.

CrossRec has been evaluated by considering different quality metrics and a dataset consisting of 1.200 GitHub Java projects. The performed evaluation demonstrated that our approach outperforms [LibRec](http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=6671293), a well-known system for library recommendation with regards to various quality indicators. To the best of our knowledge, our work is the first one that employs graphs to represent the relationships among software projects so as to effectively compute similarity and eventually to recommend libraries. 



There are the following main folders:

1. <b>experimental\_results</b> contains all the results obtained from the experiments with LibRec and CrossRec.

2. <b>tool</b>: Following the instruction provided in the folder, you can setup and run the source code using the dataset mentioned in the paper.



## How to cite
If you use our tool in your research, please cite it using the following BibTex entry:

```
@inproceedings{DBLP:conf/iir/NguyenRR18,
  author    = {Phuong T. Nguyen and
               Juri Di Rocco and
               Davide Di Ruscio},
  title     = {Mining Software Repositories to Support {OSS} Developers: {A} Recommender
               Systems Approach},
  booktitle = {Proceedings of the 9th Italian Information Retrieval Workshop, Rome,
               Italy, May, 28-30, 2018.},
  year      = {2018},
  crossref  = {DBLP:conf/iir/2018},
  url       = {http://ceur-ws.org/Vol-2140/paper9.pdf},
  timestamp = {Tue, 24 Jul 2018 12:47:23 +0200},
  biburl    = {https://dblp.org/rec/bib/conf/iir/NguyenRR18},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}

```

