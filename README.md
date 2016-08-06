# newsfeed

## Intro

Newsfeed 서버는 사용자가 news를 포스팅하고, 다른 사용자와 친구를 맺어 친구들이 올린 news를 feed 받는 서비스이다.

다음과 같은 기능을 제공한다.

* create user
* follow user
* post news
* feed news

## How to build

```
$ mvn clean package assembly:single
$ ls -l core/target/newsfeed-core-${verson}-distro.tar.gz
```

## How to install

Untar the disto tar such as newsfeed-core-{version}-distro.tar.gz

NEWSFEED_HOME will be newsfeed-core-0.9-SNAPSHOT (or Symlink as newsfeed)
```
$ tar xzvf newsfeed-core-0.9-SNAPSHOT-distro.tar.gz
$ ln -s newsfeed-core-0.9-SNAPSHOT newsfeed
$ cd newsfeed
$ bin/newsfeed.sh start (|stop)
```

## Setup the config

* bin/env.sh 파일을 수정
* conf/newsfeed.conf 파일을 수정

### bin/env.sh
```
$ vi bin/env.sh

NEWSFEED_HOME을 환경에 맞게 설정.
## NEWSFEED_HOME
NEWSFEED_HOME=/Users/seoeun/libs/newsfeed
```

### conf/newsfeed.conf

사용할 DB에서 database(newsfeed) 를 생성. mysql이라면,
```
mysql> create database newsfeed;
```

```
$ vi conf/newsfeed.conf
JDBC 정보를 환경에 맞게 설정
newsfeed.jdbc.driver=com.mysql.jdbc.Driver
newsfeed.jdbc.url=jdbc:mysql://localhost/newsfeed?useUnicode=true&characterEncoding=UTF-8
newsfeed.jdbc.username=sa
newsfeed.jdbc.password=sa
```

## HTTP EndPoint

baseURL = http://localhost:19191

### users

| Method    | Path             | Description         | Parameters          | Return Object |
| --------- | ---------------- | ------------------- | ------------------  |---------------|
| GET       | /users/{id}      | userID로 조회         |                     | User          |
| GET       | /users           | 모든 user 조회         |                     | a list of User  |
| POST      | /users           | 새로운 user 생성       | name, email         | User          |
| PUT       | /users/{id}      | user의 lastviewTime 수정 | lastviewTime     | User          |

```
$ curl -X POST http://localhost:19191/users -d 'email=seoeun25@gmail.com&name=seoeun'

$ curl -X GET http://localhost:19191/users

$ curl -X GET http://localhost:19191/users/1

$ curl -X PUT http://localhost:19191/users/1 -d 'lastviewTime=1470466137000'
```

User
* id - the id
* email - eamil
* name - name
* lastviewTime - 마지막 본 newsfeed의 timestamp. milliseconds.
* createTime - user 생성 시간.  milliseconds.

### followings

| Method    | Path             | Description         | Parameters          | Return Object |
| --------- | ---------------- | ------------------- | ------------------  |---------------|
| GET       | /followings/{userId} | user가 following하는 user들 조회  |      | a list of userId to follow |
| POST      | /followings          | user가 다른 사용자를 follow        | userId, followingId | Friend |

```
$ curl -X POST http://localhost:19191/followings -d 'userId=1&followingId=2'
$ curl -X GET http://localhost:19191/followings/1
```
Friend
* userId- the id of user
* followingId  - the userId of following
* createTime - Friend 생성 시간.  milliseconds.

### activities

| Method    | Path             | Description         | Parameters          | Return Object |
| --------- | ---------------- | ------------------- | ------------------  |---------------|
| POST      | /activities      | post message        | userId, message     | Activity      |

```
$ curl -X POST http://localhost:19191/activities -d 'userId=4&message=this is by 4'
```
Activity
* id - the activity id
* userId- the id of user
* message  - posting 내용
* createTime - Activity 생성 시간.  milliseconds.

### feeds

| Method    | Path             | Description         | Parameters          | Return Object |
| --------- | ---------------- | ------------------- | ------------------  |---------------|
| GET       | /feeds/          | 모든 activity list   | ( basetime, maxResult, asc) | a list of Activity |
| GET       | /feeds/{userId}  | user의 구독 list      | ( basetime, forward, maxResult, asc) | a list of Activity |

Parameters
* userId - Newsfeed를 받는 user의 id
* basetime - basetime(times in milliseconds) 이후에 전송된 activities를 조회. optional.
이 값이 없으면 User의 lastviewTime이후를 기준.
이것도 없으면 (아마 최초 retreive) 현재 시간 보다 24시간 전을 기준.
0이면 현재 시간 보다 24시간 전을 기준
* forward - true | false. true면 basetime 기준으로 이후의 feed들. false면 이전의 feed들.
* maxResult - 리턴할 activities의 최대 갯수. default는 newsfeed configuration에서 설정.
* asc - true | false. true이면 asc 정렬. 오래된 posting이 제일 앞에.
```
$ curl -X GET http://localhost:19191/feeds/1
$ curl -X GET http://localhost:19191/feeds

```

### errorsObject

Http Request를 보냈을 때 error 가 발생하면 errorObject를 json형식으로 리턴한다.

errorObject
* status - status
* message - message




