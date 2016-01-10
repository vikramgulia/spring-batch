# spring-batch
This is a spring batch demo application using the `org.springframework.boot:spring-boot-starter-batch`. The batch is pulling records from a database, process them and insert the processed records back into a new database table.

## Description
The project aims to provide a simple example of how to use spring batch to read records from a database table,
process them and insert the results into another database table. We will use [gradle](https://gradle.org) as our build tool,
[MySQL](http://www.mysql.com) as our database.

### High-Level Design
1. _Input for the batch_ - the batch will read records from `reader` table - `id`, `firstName`, `lastName` and `random_num`.
2. _Processing_ - the batch will process input and join the `firstName` and `lastName` to generate the `full_name` attribute.
3. _Output of the batch_ - the batch will then output and write the data into `writer` table with attributes - `id`, `full_name` and `random_num` (generated at run time).


## Getting Started
You can clone this project `git clone `, run it and expand it further. Or read the tutorial below to start from scratch.

### Setting the build tool
we will set up the `build.gradle` with plugin `java` and `spring-boot`. Also, we will add following dependencies -
1. `spring-boot-starter-batch` - to pull in spring batch classes.
2. `spring-boot-starter-data-jpa` - to handle the datasource and pull in `spring-jdbc` dependencies.
3. `mysql-connector-java` - java connector for MySQL database.

Here is what our build.gradle looks like -

```groovy
    group 'com.barley.batch'
    version '1.0.0'

    apply plugin: 'java'
    apply plugin: 'spring-boot'

    sourceCompatibility = 1.8
    targetCompatibility = 1.8

    jar {
        baseName = 'spring-batch'
    }

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath('org.springframework.boot:spring-boot-gradle-plugin:1.3.1.RELEASE')
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        compile('org.springframework.boot:spring-boot-starter-batch')
        compile('org.springframework.boot:spring-boot-starter-data-jpa') {
            exclude group: "org.slf4j", module: "log4j-over-slf4j"
            exclude group: "ch.qos.logback", module: "logback-classic"
        }
        compile('mysql:mysql-connector-java:5.1.38')
        testCompile('junit:junit')
    }

    task wrapper(type: Wrapper) {
        gradleVersion = '2.3'
    }
```

### Model
We will add 2 classes to handle our model i.e. input and output data. Here, our input is `RecordSO` and output is `WriterSO`. See classes below -

#### RecordSO

```java
    package com.barley.batch.model;

    public class RecordSO {

        private long id;
        private String firstName;
        private String lastName;
        private String randomNum;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getRandomNum() {
            return randomNum;
        }

        public void setRandomNum(String randomNum) {
            this.randomNum = randomNum;
        }
    }
```

#### WriterSO

```java
    package com.barley.batch.model;

    public class WriterSO {

        private long id;
        private String fullName;
        private String randomNum;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getRandomNum() {
            return randomNum;
        }

        public void setRandomNum(String randomNum) {
            this.randomNum = randomNum;
        }
    }
```

### Processor

Now we will set up the processor class which will implement spring interface `ItemProcessor<I, O>` - 

```java
    package com.barley.batch.processor;

    import com.barley.batch.model.RecordSO;
    import com.barley.batch.model.WriterSO;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.batch.item.ItemProcessor;

    public class RecordProcessor implements ItemProcessor<RecordSO, WriterSO> {

        private static final Logger LOGGER = LoggerFactory.getLogger(RecordProcessor.class);

        @Override
        public WriterSO process(RecordSO item) throws Exception {
            LOGGER.info("Processing Record: {}", item);
            WriterSO writerSo = new WriterSO();
            writerSo.setId(item.getId());
            writerSo.setFullName(item.getFirstName() + " " + item.getLastName());
            writerSo.setRandomNum(String.valueOf(Math.random()).substring(3, 8));
            LOGGER.info("Processed Writer: {}", writerSo);
            return writerSo;
        }
    }
```

