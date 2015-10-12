# See <https://docs.vagrantup.com/v2/networking/public_network.html>
NETWORK_INTERFACES = [
    "en3: Thunderbolt Ethernet",
]

Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/vivid64"
    config.vm.synced_folder ".", "/vagrant", disabled: true

    config.vm.define "topside" do |topside|
        topside.ssh.forward_x11 = true
        topside.vm.network "public_network", bridge: NETWORK_INTERFACES
        topside.vm.synced_folder ".", "/home/vagrant/ROV"
        topside.vm.provision "shell", path: "env/provision", privileged: false, args: "topside"
        topside.vm.provision "file", source: "env/gradle.properties", destination: "~/.gradle/gradle.properties"
        topside.vm.provision "file", source: "env/inputrc", destination: "~/.inputrc"
        topside.vm.provision "file", source: "env/profile", destination: "~/.bash_profile"
        topside.vm.provider "virtualbox" do |virtualbox|
            virtualbox.customize ["modifyvm", :id, "--usb", "on"]
        end
    end

    config.vm.define "rov" do |rov|
        rov.vm.network "public_network", bridge: NETWORK_INTERFACES
    end

    config.vm.define "camera" do |camera|
        camera.vm.network "public_network", bridge: NETWORK_INTERFACES
    end
end
