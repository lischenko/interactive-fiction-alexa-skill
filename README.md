# Interactive Fiction Alexa Skill

A [Z-Machine](https://en.wikipedia.org/wiki/Z-machine) for Alexa.

## See it in action
[![Playing Galatea on Alexa](http://img.youtube.com/vi/_QHdZCfx8Fk/0.jpg)](http://www.youtube.com/watch?v=_QHdZCfx8Fk)

## Usage

This skill has been deployed to Alexa Skill Store; just say "Alexa, enable Interactive Fiction". If you don't have an Alexa device, you can try the skill out on https://echosim.io/ but it does not support sessions, making it seriously harder to use than a real Echo.

[The official page of the skill](https://www.amazon.com/Vitaly-Lishchenko-Interactive-Fiction/dp/B01IVEANGM/) on Amazon.com.

## Installation
*(Pre-requisites: install [Maven](https://github.com/apache/maven) and [Leiningen](https://github.com/technomancy/leiningen))*

If you want to build it from scratch:

 - Get, build and install [ZAX](https://github.com/mattkimmel/zax) into local Maven repository:


```bash
#!/bin/bash

pushd ~/tmp/
git clone https://github.com/mattkimmel/zax.git
cd zax
git checkout -f 3113c4a74140628cfd0209c4756057f129ae2215 # tested with this version

patch --ignore-whitespace build.gradle << EOF
32a33,37
> 
> java {
>     sourceCompatibility = JavaVersion.VERSION_1_8
>     targetCompatibility = JavaVersion.VERSION_1_8
> }
\ No newline at end of file
EOF

rm -f zax.jar
./gradlew clean build

jar cvmf build/tmp/jar/MANIFEST.MF zax.jar -C build/classes/java/main/ com

mvn install:install-file -Dfile=zax.jar -DgroupId=github-mattkimmel -DartifactId=zax -Dversion=0.92-SNAPSHOT-5 -Dpackaging=jar
popd
```
 - Clone this project and build an uberjar with [Leiningen](https://github.com/technomancy/leiningen):
```bash
git clone https://github.com/lischenko/interactive-fiction-alexa-skill
cd interactive-fiction-alexa-skill/
lein uberjar
```
The resulting jar is ready to be [uploaded](https://docs.aws.amazon.com/lambda/latest/dg/lambda-app.html#lambda-app-upload-deployment-pkg) to an AWS Lambda function. 
 - Set your Lambda function handler to `ifas.ZLambdaFunction::handleLambda`
 - Create DynamoDB tables (using [aws cli](https://aws.amazon.com/cli/) in this example):
```bash
#!/bin/bash

aws dynamodb create-table --table-name IfasStoryLinks --attribute-definitions AttributeName=name,AttributeType=S --key-schema AttributeName=name,KeyType=HASH --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1

aws dynamodb create-table --table-name IfasStoryDumps --attribute-definitions AttributeName=customerId,AttributeType=S AttributeName=url,AttributeType=S --key-schema AttributeName=customerId,KeyType=HASH AttributeName=url,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1
```

The `IfasStoryLinks` table contains mappings from story names to URLs (should be pre-configured), and `IfasStoryDumps` keeps track of user state and is managed by the skill.

## License

Copyright © 2016–2019 Vitaly Lishchenko

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version. 
