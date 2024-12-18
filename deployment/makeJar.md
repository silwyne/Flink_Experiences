# Making Job jar file

One of my challenges in data engineer at the beginning was to make my first job jar file.
well there are two ways that I know.

### Ways
- #### 1. make a jar file which includes all dependencies
- #### 2. make a jar file which exclude all dependencies

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
  - first compile your java classes.
     ```shell
      mvn clean compile
     ```
  - then package them up
     ```shell
      mvn clean package
    ```

- ##### Done !
Now you have your jar file in ./target directory !