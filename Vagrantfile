# See <https://docs.vagrantup.com/v2/networking/public_network.html>
NETWORK_INTERFACES = [
    "en3: Thunderbolt Ethernet",
    "en4: Thunderbolt Ethernet",
    "en0: Ethernet",
    "Intel(R) 82579V Gigabit Network Connection",
    "Killer e2200 Gigabit Ethernet Controller",
    "Realtek PCIe GBE Family Controller"
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
    if Vagrant::Util::Platform.windows?
        available_interfaces = %x("C:/Program Files/Oracle/VirtualBox/VBoxManage.exe" list bridgedifs)
    else
        available_interfaces = %x(VBoxManage list bridgedifs)
    end
    available_interfaces.split("\n\n").map { |i| interface_name i }
end

def network_interfaces_available?
    (read_available_bridged_interfaces & NETWORK_INTERFACES).any?
end

def network(config, ip: nil)
    if network_interfaces_available?
        config.vm.network "public_network", bridge: NETWORK_INTERFACES, ip: ip
    else
        config.vm.network "private_network", type: "dhcp"
    end
end

Vagrant.configure(2) do |config|
    config.vm.define "topside", autostart: false do |topside|
        network topside, ip: "192.168.88.2"

        topside.ssh.forward_x11 = true
    end

    config.vm.define "captain", autostart: false do |captain|
        network captain, ip: "192.168.88.3"

        config.vm.provision "shell" do |shell|
            shell.name = "Update Default Password",
            shell.inline = "echo 'ubuntu:eedge' | chpasswd"
        end

        config.vm.provision "shell" do |shell|
            shell.name = "Install Ansible"
            shell.inline = "\
                apt-get -y install cowsay;\
                apt-add-repository -y ppa:ansible/ansible;\
                apt-get update;\
                apt-get -y install ansible;"
        end

        captain.vm.provider "virtualbox" do |virtualbox|
            virtualbox.memory = "2048"
        end
    end

    config.vm.define "rasprime", autostart: false do |rasprime|
        network rasprime
    end

    (1..2).each do |i|
        config.vm.define "picamera#{i}", autostart: false do |camera|
            network camera

            camera.vm.provider "virtualbox" do |virtualbox|
                virtualbox.memory = "256"
            end
        end
    end

    # All machines
    config.vm.synced_folder ".", "/home/ubuntu/workspace"
    config.vm.box = "ubuntu/xenial64"
end
