node{
    stage('GIT_CHECKOUT'){
        git 'https://github.com/sapnarsy2612/Jenkins-Docker-Project.git'
    }
    stage('BUILD_DOCKER_IMAGE'){
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image build -t $JOB_SMALL:v1.$BUILD_ID .
        '''//we need to maintain multiple versions of docker image 
//to push docker image to dockerhub , image should be tagged to our dockerhub username . 
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:v1.$BUILD_ID
        ''' //tag image to dockerhub profile name 
//our webapp should always take latest image so we have to maintain version . 
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:latest
        ''' //tag image as latest
    }
    stage('PUSH_DOCKER_IMAGE'){
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image build -t $JOB_SMALL:v1.$BUILD_ID .
        '''//we need to maintain multiple versions of docker image 
//to push docker image to dockerhub , image should be tagged to our dockerhub username . 
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:v1.$BUILD_ID
        ''' //tag image to dockerhub profile name 
//our webapp should always take latest image so we have to maintain version . 
        sh '''
        JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
        docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:latest
        ''' //tag image as latest
    }
    stage('PUSH_IMAGE_TO_DOCKERHUB'){
        withCredentials([string(credentialsId: 'DOCKERHUBNEW', variable: 'DOCKERHUB')]) {
            sh "docker login -u sravtar -p ${DOCKERHUB}"
            sh '''
            JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
            docker image push sravtar/$JOB_SMALL:v1.$BUILD_ID
            docker image push sravtar/$JOB_SMALL:latest
            docker image rm sravtar/$JOB_SMALL:latest sravtar/$JOB_SMALL:v1.$BUILD_ID $JOB_SMALL:v1.$BUILD_ID
            '''
        }
    }
    stage('DOCKER_CONTAINER_DEPLOYMENT'){
        //defining variable 
        def docker_run = 'docker run -itd --name scriptedcontainer -p 9000:80 sravtar/cicd_docker_build_webapp' //since we have added any version tag it will pull latest image 
        //we hav to delete container also like we delete image 
        def docker_rmv_container = 'docker rm -f scriptedcontainer'
        def docker_rmi = 'docker rmi -f sravtar/cicd_docker_build_webapp'
        sshagent(['WEBAPP']) {
            //remove old images and containers and then we create new container with latest image 
            sh "ssh -o StrictHostKeyChecking=no ubuntu@13.233.214.193 ${docker_rmv_container}"
            sh "ssh -o StrictHostKeyChecking=no ubuntu@13.233.214.193 ${docker_rmi}"
            //pull docker image
            sh "ssh -o StrictHostKeyChecking=no ubuntu@13.233.214.193 ${docker_run}"  
        }
    }
}
