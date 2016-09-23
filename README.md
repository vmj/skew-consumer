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


## Hardware

I've got two of the following:

  * Rasperry Pi 3 Model B
  * Transcend microSDHC class 10 32GB (Premium 400x UHS-I)
  * USB A - Micro B power cable

I'm powering both of them with:

  * Fuj:tech 7 port USB hub with 4A power supply

To prepare the SD cards:

  * A linux laptop with SD card reader (needs a microSD adapter)


## Sign up to Resin.io

Sign up or login to resin.io and create a new app.  Then download the
ResinOS .img for the app, specifying the WiFi SSID and password (if it is
an open network, just an empty password).

## Preparing the SD card

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

## Plug the hardware

Put the SD cards into the RPi3s and plug the power cords.

Finally, go the the application dashboard and marvel as the devices come up :)

## Setting up the demo server

On the laptop, download ActiveMQ from apache.org.  Then extract it and start it.

    $ mkdir -p ~/demo
    $ cd demo
    $ wget http://www.apache.org/.../apache-activemq-5.14.0-bin.tar.gz
    $ tar zxvf activemq-x.x.x-bin.tar.gz
    $ cd apache-activemq-x.x.x/bin
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

Then send messages using the ActiveMQ web console.
The brokerUrl in the stomp component defaults to localhost,
so the app and ActiveMQ should be talking to each other.

If you have docker installed, you can, alternatively, run the app in
docker:

    $ ./gradlew dockerRun
    # or './gradlew dockerImage' if you want to 'docker run' manually.

Note that you will still need to pass those environment variables for
the app to work.


## Installing the app on your Resin app fleet

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
