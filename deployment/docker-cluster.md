# docker cluster

> [!NOTE]\
> I believe the easiest way to have a flink cluster is to have cluster on docker!\
> There is a docker-compose example file for you in this directory

![evening_work_setup Picture from www.stockcake.com](../images/evening_work_setup.jpg)


## Tips:
- Each node is a docker container.
- In this approach we have a jobmanager node and one or multiple taskmanager nodes
- Your job jar file is only with jobmanager node.

## Requirements:
- having official docker image of flink.
- having docker installed on your machines.

## How to do it then?
I suppose you will use docker-compose for setting up your nodes!\
so only things you need are two services; one for jobmanager and another for taskmanager.

- ### jobmanager service:
```yaml
  jobmanager:
    image: flink:latest
    container_name: jobmanager
    ports:
      - "8081:8081"
    command: jobmanager
    volumes:
      - ./Your-Jar-File.jar:/opt/flink/usrlib/Your-Jar-File.jar
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
```shell
docker-compose up -d --scale taskmanager=3
```
In this example we will have 3 `taskmanagers`!

## Submitting Job
Just get in `jobmanager` node and use this command to submit your job:
```shell
docker exec -it jobmanager flink run -d /path/to/your/jarfile.jar
```

for checking if your job is up use this command:
```shell
docker exec -it jobmanager flink list -rs
```