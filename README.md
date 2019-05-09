# Web Crawler
An Internet bot that knows to browse www to the predefine depth using Breadth-first search (BFS) approach.
The bot is designed generic enough which could be easily be adjusted to the scalable distributed system. 
## System Requirements
* JDK 8 or later

## Quick start
Let's see how easily and quickly you can build and run Web Crawler

* [Design](#design)
* [Sctructure](#modules)  
* [Build](#build)
* [Run Tests](#tests)
* [Usage](#usage)
* [JavaDoc](#javadoc)


## <a name="design"></a>Crawler Design Overview
   
 The Crawler browsing process behaves as a stateful process which is dictated by the following Finite-state machine.
 
## <a name="build"></a>Build Crawler

### Overview

 Airlock SDK could be build as a JAR file which later might be usage in any java based system 

#### 1. Clone and build a jar file

Clone the repository using git tool, develop branch has a latest code

#### 2. Build configuration
   - Set snapshot flag 
   Open build.gradle and set ext.SNAPSHOT be true.
  
   - Set development build version
   Open gradle.properties file and set *devBuildNumber* value. 
   
   
#### 3. Run Build from CMD   

```bash
$ ./gradlew build
```

The artifact will be located in `/build/libs` folder

## <a name="tests"></a>Tests

#### 4. Run Tests

### Overview


  The tests are localed on the airlock-sdk-common, 
  each platform has its own tests suite to run them.
  To run test you can either use Intejii IDE or run gradle cmd command.
 

```bash
$ ./gradlew clean test
```


## <a name="model"></a>Mutli Airlock Product Model
#### Motivation:


## <a name="usage"></a>Usage

 The usage section will be based on Airlock Rest API Client auto-generated through `swagger-gen` tool
 Demonstrates the initial step to get started with Airlock SDK for Java.  
 
 [Airlock SDK sample code](https://github.com/TheWeatherCompany/airlock-sdk-java/blob/develop/src/test/java/runner/TestDriver.java)
     
     
