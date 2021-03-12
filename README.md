# simplesearch
Search engine used for factseek.org.

This short description illustrates how to compile the code and test it. 

First, add your encyclopedia to encyclopedias.json. The existing entries 
show the syntax of this file. Then compile it as:

```
ant
```
This creates the file "encyclosearch.jar".
You need the ant-build tool and a recent JDK (javac).  Test this program as:


```
./A_SEARCH_STATIC  0 20 0 elephant

```
(or simply run "A_TEST"). It returns the HTML text with the search results. 
This command searches for the word "elephant" starting from the position 0, and allowing for 20 results from each encyclopedia.
Next 0 indicates searches in titles (1 means "fulltext" search). 

If you want to debug a specific encyclopedia, correct encyclopedias.json by keeping only encyclopedia you are interested in.

The code is based on https://gitlab.com/ks_found/encyclosearch by H.Sanger.

S.Chekanov 
