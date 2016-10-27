DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CLUSTER_NAME=$2
EXTRA_SSH_PARAMS=$3

echo $DIR

get_dns_names() {
    echo "Finding servers in cluster: ${CLUSTER_NAME}"
    dns_names_str=$(aws ec2 describe-instances --filters "Name=instance-state-name,Values=running" "Name=tag-key,Values=Name" "Name=tag-value,Values=$CLUSTER_NAME" --query "Reservations[*].Instances[*].PublicDnsName | [][]" --output text)
    echo $dns_names_str
    IFS='	' read -r -a dns_names <<< "$dns_names_str"
    echo "Found ${#dns_names[@]} servers"
}

get_module_file() {
    module=$1    
    module_file=$(ls ${DIR}/${module}/build/distributions/*.zip | head -n1 | tr "/" "\n" | tail -n1)
}

build(){
    module=$1
    module_name=$(echo "${module}" | sed s/://g)
    echo "Building ${module_name}"
    gradle -b ${DIR}/build.gradle ${module}clean ${module}build ${module}prepare
}

upload(){
    module=$1
    for i in "${dns_names[@]}"; do
        echo "Uploading ${module} to ${i}"
        scp ${EXTRA_SSH_PARAMS} ${DIR}/${module}/build/distributions/*.zip ubuntu@${i}:./
    done
}

install() {
    module=$1
    get_module_file $module
    for i in "${dns_names[@]}"; do
        echo "Installing ${module} on ${i}"
        ssh ${EXTRA_SSH_PARAMS} ubuntu@${i} << EOF
sudo /usr/share/elasticsearch/bin/plugin install -b file:///home/ubuntu/${module_file}
sudo service elasticsearch restart
EOF
    done
}

case "$1" in
    all)
        build
        get_dns_names
        upload "euclideandistance"
        upload "hammingdistance" 
        install "euclideandistance"
        install "hammingdistance" 
        ;;
    euclideandistance)
        build ":euclideandistance:"
        get_dns_names
        upload "euclideandistance"
        install "euclideandistance"
        ;;
    hammingdistance)
        build ":hammingdistance:"
        get_dns_names
        upload "hammingdistance"
        install "hammingdistance" 
        ;;
    *)
        echo $"

  This script allows you to build, upload and install elasticsearch plugins.

  Usage: $0 {all|euclideandistance|hammingdistance} ES-CLUSTER-NAME [EXTRA-SSH-COMMANDS]

  Options:
    all               - build, upload and install both euclideandistance and hammingdistance plugins
    euclideandistance - build, upload and install just the euclideandistance scoring script plugin
    hammingdistance   - build, upload and install just the hammingdistance scoring script plugin
  
  Params:
    ES-CLUSTER-NAME    - the name of the elasticsearch cluster (eg: dev-cluster-bob-geldoff)
    EXTRA-SSH-COMMANDS - additional parameters to include with the scp and ssh commands (eg:-i my_key_file.pem) (Optional)
"
        exit 1
esac