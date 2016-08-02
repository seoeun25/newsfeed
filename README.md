#### newsfeed

# Intro

Newsfeed 서버는 사용자가 news를 포스팅하고, 다른 사용자와 친구를 맺어 친구들이 올린 news 를feed 받는 서비스이다.

다음과 같은 기능을 제공한다.

* create user
* follow user
* post news
* feed news

# How to build

```
$ mvn clean package assembly:single
$ ls -l newsfeed/target/newsfeed-core-{version}-distro.tar.gz
```

# How to install

Untar the disto tar such as schema-registry-repo-{version}-distro.tar.gz

REPO_HOME will be repo-0.9-SNAPSHOT
```
$ tar xzvf schema-registry-repo-0.9-SNAPSHOT-distro.tar.gz
$ cd repo-0.9-SNAPSHOT
$ bin/repo.sh start (|stop)
```

# Setup the config

* bin/env.sh 파일을 수정
* conf/newsfeed.conf 파일을 수정

# HTTP EndPoint

## users

| Method    | Path             | Description         | Parameters          | Return Object |
| --------- | ---------------- | ------------------- | ------------------  |---------------|
| GET       | /users/{id}      | userID로 조회         |                     | User          |
| POST      | /users           | 새로운 user 생성       | name, email         | User          |


User
* id - the id
* email - eamil
* name - name
* lastViewTime - 마지막 본 newsfeed의 timestamp. milliseconds.
* createTime - user 생성 시간.  milliseconds.
