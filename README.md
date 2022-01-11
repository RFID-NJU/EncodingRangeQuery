# Introduction

This project is the code for paper: Encoding based Range Detection in Commodity
RFID Systems (INFOCOM2022).



# File Info

- **Data** : Store the experimental results. (*.csv files in this directory are just examples)
  - **RQ** : Store the results of RQ.
  - **ERQ** : Store the results of ERQ.
  - **EncodingRQ** : Store the results of EnRQ.
  - **TestData** : Store the test data.
- **src** : Core code.
  - **AlienUtil.java** : Initialize the reader.
  - **Utils.java** : Useful parameters and methods.
  - **WriteNormalTags.java** : Write "normal" user data to tags.
  - **WriteAbnormalTags.java** : Write "abnormal" user data to tags.
  - **Data_Generate.java** : Generate the test data and save the data to files.
  - **RQ.java** : Run URQ of RQ protocol with the given test data array list to get the average time efficiency of RQ.
  - **EncodingRQ16.java** : Run URQ of EnRQ protocol with the given test data array list to get the average time efficiency of EnRQ when the cardinal number is 16.
  - **Test_Parameters.java**:  Test parameters in each experiment.
  - **Real_Test_EncodingRQ.java** : Get the time efficiency of protocols (RQ/EnRQ).



# How to conduct an experiment

## Tools

- IntelliJ IDEA 2020.2.1 (Ultimate Edition)

## Dependencies

- Alien_Java_SDK_v2.3.5 (https://www.alientechnology.com/products/files-welcome/)
- junit.4.12

## 1. Prepare the tags

We provide **WriteNormalTags.java** and **WriteAbnormalTags.java** for you to write normal tags and target tags.

For **WriteNormalTags.java**, we write tags in batches. Each execution will write 5 tags. If the number of  tags is more than or less than 5, you will get an error.

For **WriteAbnormalTags.java**, there is no restriction for the number of tags.

## 2. Initialize the reader

Before conduct the experiments, we suggest to run **AlienUtil.java** first to initialize the reader.

## 3. Get the test data

We randomly get 100 numbers in the given range to get the average time efficiency of RQ and EnRQ. So before the experiments, we need to generate test data using **Data_Generate.java**.

## 4. Start the experiments

You can execute the protocols with **Real_Test_EncodingRQ.java**.

Moreover, you can set the parameters we have discussed in papers, i.e., length of data, the number of tags, the number of target tags.