Control Software
================

This repository contains the control software for the Eastern Edge Robotics team's 2016 underwater ROV.

Vagrant environment
-------------------

Before working with Vagrant, make sure you have the following installed:

- [VirtualBox](https://www.virtualbox.org/manual/ch01.html#intro-installing)
- [Vagrant](https://docs.vagrantup.com/v2/installation/index.html)

You need to have both installed before running any of the command below will work.

To familiarise yourself with Vagrant, have a read through the [Getting Started](https://docs.vagrantup.com/v2/getting-started/) section from the Vagrant website. Vagrant has a lot of features, but note that only a subset of them are used in this project.

The project includes a [Vagrant multi-machine environment][Vagrant] to model the topology of the ROV. As such, the `Vagrantfile` defines six virtual machines (VMs):

- `rasprime`
- `topside`
- `picamera1`
- `picamera2`
- `picamera3`
- `picamera4`

You can see the list of VMs defined and their current status by running:

```
vagrant status
```

The topside VM is the primary (default) VM, and will be the VM that is controlled when a machine name is not specified. To create and start the topside VM, you can run:

```
vagrant up
```

To [SSH](https://en.wikipedia.org/wiki/Secure_Shell#Usage) into the topside VM, you can run:

```
vagrant ssh
```

When you want to power off the topside VM, you can run:

```
vagrant halt
```

For more about the topside VM and why it is the primary VM, [see the Wiki][Vagrant wiki]. As for the other VMs, you can control them similarly by specifying the name of the machine you want to control in the command. For example:

```
vagrant up picamera1
```

```
vagrant ssh picamera1
```

```
vagrant halt picamera1
```

For more information about the other VMs and what they have installed, [see the Wiki][Vagrant wiki].

  [Vagrant]:https://docs.vagrantup.com/v2/multi-machine/index.html
  [Vagrant wiki]:https://github.com/EasternEdgeRobotics/2016/wiki/Vagrant-environment
