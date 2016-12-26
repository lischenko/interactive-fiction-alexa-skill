# Interactive Fiction Alexa Skill

A [Z-Machine](https://en.wikipedia.org/wiki/Z-machine) for Alexa.

## Usage

This skill has been deployed to Alexa Skill Store; just say "Alexa, enable Interactive Fiction". If you don't have an Alexa device, you can try the skill out on https://echosim.io/ but it does not support sessions, making it seriously harder to use than a real Echo.

[The official page of the skill](https://www.amazon.com/Vitaly-Lishchenko-Interactive-Fiction/dp/B01IVEANGM/) on Amazon.com.

## Installation

If you want to build it from scratch:

 - Get, build and install [ZAX](https://github.com/mattkimmel/zax) into local Maven repository:
```bash
#!/bin/bash

pushd ~/tmp/
git clone https://github.com/mattkimmel/zax.git
cd zax
rm -f zax.jar
ant -f zax.xml all
echo -e "Manifest-Version: 1.0\nCreated-By: 1.8.0_45 (Oracle Corporation)\nMain-Class: com.zaxsoft.apps.zax.Zax\n" > /tmp/manifest
jar cvmf /tmp/manifest zax.jar -C out/production/Zax/ com

mvn install:install-file -Dfile=zax.jar -DgroupId=github-mattkimmel -DartifactId=zax -Dversion=0.91 -Dpackaging=jar
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

Copyright © 2016 Vitaly Lishchenko

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version. 