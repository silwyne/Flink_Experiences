# Flink Deployment

## Explanation
This is a simple guide of how to deploy our flink job to a flink cluster.


### Making Job jar file

One of my challenges in data engineer at the beginning was to make my first job jar file.
well there are two ways that I know.

### Ways
1. #### make a jar file which includes all dependencies
2. #### make a jar file which exclude all dependencies

> [!IMPORTANT]\
> I think the first way is better, because in the second way you must add all other dependencies to your flink cluster.
> With a class path or something to tell flink to load its dependencies from that directory which seams messy.

### making a jar file which includes all dependencies

>[!NOTE]\
> I suppose you are basically using Maven for your project.


- #### PRE-STEP: Install maven
  - Just download it from its official website.
  - and set MAVEN_HOME environment variable pointing to extracted directory of maven.
  - That's it you have installed maven!


- ##### STEP 1: add this to your pom.xml file
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.1.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <filters>
                            <filter>
                                <!-- Do not copy the signatures in the META-INF folder.
                                Otherwise, this might cause SecurityExceptions when using the JAR. -->
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <!-- Replace this with the main class of your job -->
                                <mainClass>YourPackage.YourMainClass</mainClass>
                            </transformer>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

>[!TIP]\
> If you are confused look at my pom.xml file, well there is no main class here because there are many example jobs here.\
> But if you have your main class in package `com.packages` and your main class name is `MyJob` your mainClass is this: `com.packages.MyJob`

- ##### STEP 2: Run these maven commands to make your jar file
  1. first compile your java classes.
     ```shell
      mvn clean compile
     ```
  2. then package them up
      ```shell
      mvn clean package
      ```
  3. Now you have your jar file in `./target/` directory.
    

# Docker cluster

> [!NOTE]\
> I believe the easiest way to have a flink cluster is to have cluster on docker!\
> There is a docker-compose example file for you in this directory

![evening_work_setup Picture from www.stockcake.com](../images/evening_work_setup.jpg)


## Tips:
- Each node is a docker container.
- In this approach we have a jobmanager node and one or multiple taskmanager nodes

## Requirements:
- having official docker image of flink.
- having docker installed on your machines.

## How to do it then?
I suppose you will use docker-compose for setting up your nodes!\
so only things you need are two services, one for jobmanager and another for taskmanager.

- ### jobmanager service:
```yaml
  jobmanager:
    image: flink:latest
    container_name: jobmanager
    ports:
      - "8081:8081"
    command: jobmanager
    environment:
      - |
        FLINK_PROPERTIES=
        jobmanager.rpc.address: jobmanager
        jobmanager.memory.process.size: 2g
        jobmanager.cpu.cores: 2
    networks:
      - flink-network

networks:
  flink-network:
    driver: bridge
```

>[!NOTE]\
> As you saw the only volume we set for service was `Your-Job-Jar` file!\
> You do not need to do this for taskmanager nodes too!


- ### taskmanager service:
```yaml
  taskmanager:
    image: flink:latest
    command: taskmanager
    deploy:
      replicas: 3
    environment:
      - |
        FLINK_PROPERTIES=
        jobmanager.rpc.address: jobmanager
        taskmanager.numberOfTaskSlots: 8
        taskmanager.memory.process.size: 6g
        taskmanager.cpu.cores: 3
    networks:
      - flink-network

networks:
  flink-network:
    driver: bridge
```

> [!NOTE]\
> `deploy.replicas` is the number of your task managers.\
> If your docker version doesn't understand `deploy.replicas` you can use this command to bring up multiple `taskmanagers`:
> ```shell
> docker-compose up -d --scale taskmanager=3
> ```
In this example we will have 3 `taskmanagers`!

## Submitting Job
- **Manually**
  1. ###### for submitting your job to cluster :\
  Just get in `jobmanager` node and use this command to submit your job:
    ```shell
    docker exec -it jobmanager flink run -d /path/to/your/jarfile.jar
    ```
  2. ###### for monitoring jobs :
   for checking if your job is up use this command:
     ```shell
     docker exec -it jobmanager flink list -rs
     ```
- **Using Web Ui**
  1. ###### for submitting your job to cluster :
     1. access ui from `localhost:8081`
     2. go to `submit New Job` and click on `Add New` and upload your jar file
     3. from `Uploaded Jars` click on your uploaded jar and click on your jar file.
     4. set the desired parallelism which must be less than or equal to available task slots.
     5. submit the job!
  2. ###### monitoring jobs: 
    Just use the Web Ui ...