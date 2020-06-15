[![DOI](https://zenodo.org/badge/134830691.svg)](https://zenodo.org/badge/latestdoi/134830691)

This repository contains the source code implementation of CrossRec, the dataset as well as the experimental results for the following paper:

<b>CrossRec: Supporting Software Developers by Recommending Third-party Libraries</b>

Phuong T. Nguyen, Juri Di Rocco, Davide Di Ruscio<sup>(1)</sup>, Massimiliano Di Penta<sup>(2)</sup>

<sup>(1)</sup> Università degli Studi dell'Aquila, Italy

<sup>(2)</sup> Università degli Studi del Sannio Benevento, Italy

The paper has been accepted for publication by Journal of Systems and Software ([link](https://www.sciencedirect.com/science/article/pii/S0164121219302341))


and the following paper that has been published at the Proceedings of the 9th Italian Information Retrieval Workshop, Rome, Italy, May, 28-30, 2018

<b>Mining Software Repositories to Support OSS Developers: A Recommender	Systems Approach</b> ([link](http://ceur-ws.org/Vol-2140/paper9.pdf))

Phuong T. Nguyen, Juri Di Rocco, Davide Di Ruscio

Department of Information Engineering, Computer Science and Mathematics,
Università degli Studi dell'Aquila

## CrossRec: A Recommender System for Providing Third-party Libraries

CrossRec is a framework that exploits cross projects relationships in open source software repositories to build a recommender system. It aims at supporting software developers who have already included some libraries in the new projects being developed, and expect to get recommendations on which additional libraries should be further incorporated. The CrossRec architecture is as follows:

<p align="center">
<img src="https://github.com/crossminer/CrossRec/blob/master/images/CrossRec.png" width="500">
</p>

The developer interacts with the system by sending a request for recommendations. The request contains a list of libraries that are already included in the project the developer is working on. The <b>Data Encoder</b> collects <i> background data </i> from OSS repositories, represents them in a <i>mathematically computable format</i>, which is then used as a base for other components of CrossRec. <!-- We use a Knowledge Graph to model the relationships among projects and libraries as given below.-->

<!--  <p align="center"> -->
<!--  <img src="https://github.com/crossminer/CrossRec/blob/master/images/Graph.png" width="450"> -->
<!--  </p> -->

The <b> Similarity Calculator</b> module computes similarities among projects to find the most similar ones to the given project. The <b>Recommendation Engine</b> gets the list of <i> top-k</i> similar projects and executes recommendation techniques to generate a ranked list of <i> top-N </i> libraries. Finally, the recommendations are sent back to the developer. Background data can be collected from different OSS platforms like GitHub, Eclipse, BitBucket. The current version of CrossRec supports data extraction from GitHub, even though the support for additional platforms is already under development.

We performed an empirical evaluation of the proposed approach on a dataset of 1,200 GitHub Java projects using different quality metrics, i.e., success rate, accuracy, sales diversity and novelty. The evaluation demonstrated that our approach outperforms the state-of-the-art approach [LibRec](http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=6671293), a well-known system for library recommendation with regards to various quality indicators. Our findings pave the way for further development towards a multi-purpose recommender system that can supply to developers several types of recommendations.

## Structure
There are the following main folders:

1. <b>experimental\_results</b> contains all the results obtained from the experiments with LibRec and CrossRec.

2. <b>tool</b>: Following the instruction provided in the folder, you can setup and run the source code using the dataset mentioned in the paper.



## How to cite
If you use the tool or dataset in your research, please cite our work using the following BibTex entries:


```
@article{NGUYEN2019110460,
title = "CrossRec: Supporting Software Developers by Recommending Third-party Libraries",
journal = "Journal of Systems and Software",
pages = "110460",
year = "2019",
issn = "0164-1212",
doi = "https://doi.org/10.1016/j.jss.2019.110460",
url = "http://www.sciencedirect.com/science/article/pii/S0164121219302341",
author = "Phuong T. Nguyen and Juri Di Rocco and Davide Di Ruscio and Massimiliano Di Penta"
}

```
and

```
@inproceedings{DBLP:conf/iir/NguyenRR18,
  author    = {Phuong T. Nguyen and Juri Di Rocco and Davide Di Ruscio},
  title     = {Mining Software Repositories to Support {OSS} Developers: {A} Recommender Systems Approach},
  booktitle = {Proceedings of the 9th Italian Information Retrieval Workshop, Rome, Italy, May, 28-30, 2018.},
  year      = {2018},
  crossref  = {DBLP:conf/iir/2018},
  url       = {http://ceur-ws.org/Vol-2140/paper9.pdf},
  timestamp = {Tue, 24 Jul 2018 12:47:23 +0200},
  biburl    = {https://dblp.org/rec/bib/conf/iir/NguyenRR18},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}

```

## Troubleshooting

If you encounter any difficulties in working with the tool or the datasets, please do not hesitate to contact us at one of the following emails: phuong.nguyen@univaq.it, juri.dirocco@univaq.it. We will try our best to answer you as soon as possible.
