# Competing Polling Consumers
#### Camel, CDI, Java SE, and a fleet of Rasperry Pis on Resin.io

This is an example of combining two EIPs (Enterprise Integration Patterns) in Camel:

 * Competing Consumers: message from a queue goes to one of the consumers.
 * Polling Consumers: the consumers pull a message to work on when they are ready to do so.

To make this more interesting, this also demos
how to integrate Apache Camel and CDI (Contexts and Dependency Injection) in a Java SE application.
Those technologies are typically used in Java EE environment.

Furthermore, we're going to deploy this to a cluster of Rasperry Pis, using Resin.io.

:)

## Setting up the demo message broker

Download ActiveMQ from apache.org.
Then extract it and start it.

    $ mkdir -p ~/demo
    $ cd demo
    $ wget http://archive.apache.org/dist/activemq/5.14.1/apache-activemq-5.14.1-bin.tar.gz
    $ tar xzvf apache-activemq-5.14.1-bin.tar.gz
    $ cd apache-activemq-5.14.1/bin
    $ ./activemq console

Now open the ActiveMQ web console at http://127.0.0.1:8161/admin/ (the credentials are admin:admin).

## Running the app locally

First, clone the source code:

    $ git clone https://github.com/vmj/skew-consumer.git
    $ cd skew-consumer

You can try the app locally (replace `./gradlew` with `gradlew.bat` if you're on Windows):

    $ ./gradlew installDist
    $ CONSUMER_REQUESTS_ENDPOINT_URI="stomp:queue:consumer.requests" \
      CONSUMER_RESPONSES_ENDPOINT_URI="stomp:queue:consumer.responses" \
      ./skew-consumer-app/build/install/skew-consumer-app/bin/skew-consumer-app

I know its a long name for a script :)

## Sending a message from ActiveMQ console

Now send messages using the ActiveMQ web console.
The brokerUrl in the stomp component defaults to localhost,
so the app and ActiveMQ should be talking to each other.

 * With one instance of consumer running, switch to ActiveMQ web console.
 * Click `Queues` and you should see `consumer.requests` queue listed,
   with one consumer.
 * Click the `Send To` link on that row.
 * Type "Hello" to the `Message body` text area, and click `Send`.
 * You should now see that there's one message enqueued and one message
   dequeued.
 * Refresh the page after a few seconds (the consumer sleeps for a while to imitate work)
 * You should now see `consumer.responses` queue listed, with one pending message.
 * Click the queue name (or the `Browse` link), then click the message ID of the message.
 * You should see the same "Hello" in the message details.

## Running the app in Docker

If you have docker installed, you can, alternatively, run the app in
docker:

    $ ./gradlew dockerResources
    $ cd skew-consumer-app/build/docker
    $ docker build --rm --tag=vmj0/skew-consumer-app:latest .

Note that you will still need to pass those environment variables for
the app to work.

    $ docker run --rm --read-only \
      -e CONSUMER_REQUESTS_ENDPOINT_URI="stomp:queue:consumer.requests?brokerURL=tcp://192.168.43.118:61613" \
      -e CONSUMER_RESPONSES_ENDPOINT_URI="stomp:queue:consumer.responses?brokerURL=tcp://192.168.43.118:61613" \
      vmj0/skew-consumer-app:latest

Naturally, change the IP to match the ActiveMQ server address.

## Running the app on your Resin app fleet


### Hardware

I've got two of the following:

  * Rasperry Pi 3 Model B
  * Transcend microSDHC class 10 32GB (Premium 400x UHS-I)
  * USB A - Micro B power cable

I'm powering both of them with:

  * Fuj:tech 7 port USB hub with 4A power supply

To prepare the SD cards:

  * A linux laptop with SD card reader (needs a microSD adapter)


### Sign up to Resin.io

Sign up or login to resin.io and create a new app.  Then download the
ResinOS .img for the app, specifying the WiFi SSID and password (if it is
an open network, just an empty password).

### Preparing the SD card

On the laptop, stick the SD card (using the adapter) to the reader.

Then, as root:

    $ fdisk -l
    ...
    Disk /dev/mmcblk0: 32.2GB, ...
    ...
      Device       Boot ... System
    $ dd if=/tmp/resin-myapp-x.x.x-y.y.y-abcdef.img of=/dev/mmcblk0 bs=65536
    ...
    $ fdisk -l
    ...
    Dist /dev/mmcblk0: 32.2GB, ...
    ...
      Device       Boot ... System
    /dev/mmcblk0p1   *      W95 FAT16 (LBA)
    /dev/mmcblk0p2          Linux
    /dev/mmcblk0p3          Linux
    /dev/mmcblk0p4          W95 Ext'd (LBA)
    /dev/mmcblk0p5          W95 FAT16 (LBA)
    /dev/mmcblk0p6          Linux
    $ sync

Above, `x.x.x` is the ResinOS version number, `y.y.y` is the supervisor version number,
and `abcdef` is your app hash I guess.

You can now take the card out and do the same with the next card.

### Plug the hardware

Put the SD cards into the RPi3s and plug the power cords.

Finally, go the the resin.io application dashboard and marvel as the devices come up :)

### Installing the app on your Resin app fleet

Build the required Docker resources (Dockerfile and the app distribution).

    $ ./gradlew -Presin dockerResources

Note the `-Presin` argument.  It causes the build to generate ARMv7hf compliant Dockerfile.

Then set up your Resin.io repository:

    $ mkdir -p ~/my-app
    $ cp skew-consumer-app/build/docker/* ~/my-app
    $ cd ~/my-app
    $ git init .
    $ git add *
    $ git commit -m "Initial commit."
    $ git remote add <your-resin-app-endpoint>
    $ git push resin master

You will now see in Resin app dashboard as the RPis start upgrading.

You will notice from the logs that they fail to initialize, but do not crash.
To fix this, define the app wide env vars in the Resin web console:

    CONSUMER_REQUESTS_ENDPOINT_URI="stomp:queue:consumer.requests?brokerURL=tcp://192.168.43.118:61613"
    CONSUMER_RESPONSES_ENDPOINT_URI="stomp:queue:consumer.responses?brokerURL=tcp://192.168.43.118:61613"

Naturally, replace the IP address with your own where the ActiveMQ is running.

Resin will automatically restart your app containers.

Again, play with the ActiveMQ web console and send messages to the requests queue,
and watch one of the RPis pick it, handle it, and reply to responses queue.
If you still have the local version(s) running, they will be competing for messages, too.
