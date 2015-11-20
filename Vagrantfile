# See <https://docs.vagrantup.com/v2/networking/public_network.html>
NETWORK_INTERFACES = [
    "en3: Thunderbolt Ethernet",
]

def interface_name(interface)
    interface.split("\n").each do |line|
        if line =~ /^Name:\s+(.+?)$/
            return $1.to_s
        end
    end
    nil
end

def read_available_bridged_interfaces
    available_interfaces = %x(VBoxManage list bridgedifs)
    available_interfaces.split("\n\n").map { |i| interface_name i }
end

def network_interfaces_available?
    (read_available_bridged_interfaces & NETWORK_INTERFACES).any?
end

def network(config)
    if network_interfaces_available?
        config.vm.network "public_network", bridge: NETWORK_INTERFACES
    else
        config.vm.network "private_network", type: "dhcp"
    end
end

Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/vivid64"
    config.vm.synced_folder ".", "/vagrant", disabled: true

    config.vm.define "rasprime", autostart: false do |rasprime|
        network rasprime
        rasprime.vm.hostname = "rasprime"
    end

    config.vm.define "topside" do |topside|
        topside.ssh.forward_x11 = true
        topside.vm.hostname = "topside"
        if network_interfaces_available?
            topside.vm.network "public_network", bridge: NETWORK_INTERFACES, ip: "192.168.88.2"
        else
            topside.vm.network "private_network", type: "dhcp"
        end
        topside.vm.synced_folder ".", "/home/vagrant/workspace"
        topside.vm.provision "shell", path: "env/provision", privileged: false
        topside.vm.provision "file", source: "env/gradle.properties", destination: "~/.gradle/gradle.properties"
        topside.vm.provision "file", source: "env/inputrc", destination: "~/.inputrc"
        topside.vm.provision "file", source: "env/profile", destination: "~/.bash_profile"
        topside.vm.provision "file", source: "env/dhcpd.conf", destination: "/tmp/dhcpd.conf"
        topside.vm.provision "file", source: "env/squid.conf", destination: "/tmp/squid.conf"
        topside.vm.provision "shell", path: "env/dhcpd", privileged: true
        topside.vm.provision "shell", path: "env/squid", privileged: true
        topside.vm.provider "virtualbox" do |virtualbox|
            virtualbox.customize ["modifyvm", :id, "--usb", "on"]
            virtualbox.customize [
                "usbfilter", "add", "0",
                "--target", :id,
                "--name", "Logitech Extreme 3D Pro Joystick",
                "--vendorid", "0x046d",
                "--productid", "0xc215"
            ]
        end
    end

    (1..4).each do |i|
        config.vm.define "picamera#{i}", autostart: false do |camera|
            network camera
            camera.vm.hostname = "picamera#{i}"
            camera.vm.provider "virtualbox" do |virtualbox|
                virtualbox.memory = "256"
            end
        end
    end
end
