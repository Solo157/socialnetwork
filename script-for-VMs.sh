#!/bin/bash
-------------------------
# generate keys
ssh-keygen -t rsa -b 2048 -f ~/.ssh/my_otus_id_rsa_cicd_vms -N ""

ZONE=ru-central1-b
EXTERNAL_SUBNET=default-ru-central1-b
SSH_KEY=$(cat ~/.ssh/my_otus_id_rsa_cicd_vms.pub)
SG_ID=$(yc vpc security-group get default-sg-enpii3i3t1ln2t0i8c5k --format json | jq -r .id)

# SSH password for the ubuntu user (login by password, not by key)
VM_USER_PASSWD='otus'

# create static public IP for DEVELOP VM
if yc vpc address list | grep -q develop-public-ip; then
  echo "Public IP already exists, skip creation"
else
  echo "create static public IP"
  yc vpc address create --name develop-public-ip --external-ipv4 zone=$ZONE
fi

# create virt machine for DEVELOP VM
export DEVELOP_HOST_IP=$(yc vpc address get develop-public-ip --format json | jq -r '.external_ipv4_address.address')
yc compute instance create \
  --name develop-host \
  --zone "$ZONE" \
  --preemptible \
  --core-fraction 20 \
  --metadata-from-file user-data=<(cat <<EOF
#cloud-config
datasource:
  Ec2:
    strict_id: false
ssh_pwauth: true
users:
  - name: ubuntu
    groups: sudo
    shell: /bin/bash
    sudo: 'ALL=(ALL) NOPASSWD:ALL'
chpasswd:
  list: |
    ubuntu:$VM_USER_PASSWD
  expire: false
EOF
) \
  --create-boot-disk image-id=fd8dcjve5vsdhbqs6nqj \
  --network-interface subnet-name="$EXTERNAL_SUBNET",nat-ip-version=ipv4,nat-address="$DEVELOP_HOST_IP",security-group-ids="$SG_ID" \
  --hostname develop-host


# create static public IP for PROD VM
if yc vpc address list | grep -q prod-public-ip; then
  echo "Public IP already exists, skip creation"
else
  echo "create static public IP"
  yc vpc address create --name prod-public-ip --external-ipv4 zone=$ZONE
fi

# create virt machine for PROD VM
export PROD_HOST_IP=$(yc vpc address get prod-public-ip --format json | jq -r '.external_ipv4_address.address')

yc compute instance create \
  --name prod-host \
  --zone $ZONE \
  --preemptible \
  --core-fraction 20 \
  --metadata-from-file user-data=<(cat <<EOF
#cloud-config
datasource:
  Ec2:
    strict_id: false
ssh_pwauth: true
users:
  - name: ubuntu
    groups: sudo
    shell: /bin/bash
    sudo: 'ALL=(ALL) NOPASSWD:ALL'
chpasswd:
  list: |
    ubuntu:$VM_USER_PASSWD
  expire: false
EOF
) \
  --create-boot-disk image-id=fd8dcjve5vsdhbqs6nqj \
  --network-interface subnet-name=$EXTERNAL_SUBNET,nat-ip-version=ipv4,nat-address=$PROD_HOST_IP,security-group-ids=$SG_ID \
  --hostname prod-host

waiting.. until VMs are ready
sleep 30

command -v sshpass >/dev/null || { echo "sshpass is required (apt-get install sshpass)"; exit 1; }

DEVELOP_HOST_USER=$(sshpass -p "$VM_USER_PASSWD" ssh -o StrictHostKeyChecking=no ubuntu@$DEVELOP_HOST_IP "whoami")
if [ "$DEVELOP_HOST_USER" = "ubuntu" ]; then
  echo "OK: password login works on DEVELOP_HOST"
else
  echo "ERROR: password login failed on DEVELOP_HOST"
fi

PROD_HOST_USER=$(sshpass -p "$VM_USER_PASSWD" ssh -o StrictHostKeyChecking=no ubuntu@$PROD_HOST_IP "whoami")
if [ "$PROD_HOST_USER" = "ubuntu" ]; then
  echo "OK: password login works on PROD_HOST"
else
  echo "ERROR: password login failed on PROD_HOST"
fi