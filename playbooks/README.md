Ansible Playbooks
=================

Resources:

- [Ansible Documentation](http://docs.ansible.com/ansible/)
    - [Module Index](http://docs.ansible.com/ansible/modules_by_category.html)
    - [Playbooks](http://docs.ansible.com/ansible/playbooks.html)
    - [YAML Syntax](http://docs.ansible.com/ansible/YAMLSyntax.html)

Purpose
-------

Ansible, for our purposes, serves as a way to execute commands in parallel across each of the machines in the ROV network. It is intended to replace, for example, `scp`, `rsync`, or using SSH to log into a machine and run a command.

Quick start
-----------

1. Clone the repository
2. `git update-index --skip-worktree playbooks/hosts`
3. Update the [hosts file](hosts) (IP addresses, `ansible_user`, and `ansible_ssh_pass`)
4. `vagrant up captain`
5. `vagrant ssh captain`
6. Execute a playbook using the `ansible-playbook` command (see [the wiki](https://github.com/EasternEdgeRobotics/2016/wiki/Ansible-playbooks) for an example)
