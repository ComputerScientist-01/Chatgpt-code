pipeline {
    agent any
    parameters {
        string(name: 'Patch_Level', defaultValue: '1.0', description: 'Patch Level')
        string(name: 'Patch_Mode', defaultValue: 'auto', description: 'Patch Mode')
        string(name: 'Change_Request', defaultValue: 'CR12345', description: 'Change Request ID')
        text(name: 'Server_List', defaultValue: 'server1\nserver2\nserver3', description: 'List of Server IDs (one per line)')
        string(name: 'Patch_Username', defaultValue: 'admin', description: 'Username for patching')
        password(name: 'User_Password', defaultValue: '', description: 'Password for patching') // Concealed password field
    }
    stages {
        stage('Inject Environment Variables') {
            steps {
                script {
                    // Injecting the Patch_Username and User_Password as environment variables
                    withEnv(["PATCH_USERNAME=${params.Patch_Username}", "PATCH_PASSWORD=${params.User_Password}"]) {
                        echo "Injected environment variables: PATCH_USERNAME and PATCH_PASSWORD"
                    }
                }
            }
        }
        stage('Validate Server and Trigger Patching Jobs') {
            steps {
                script {
                    // List of production servers
                    def prodServers = ['prod-server1', 'prod-server2', 'prod-server3']

                    def servers = params.Server_List.split('\n')

                    servers.each { server_id ->
                        echo "Processing server: ${server_id}"
                        
                        if (prodServers.contains(server_id)) {
                            echo "Server ${server_id} is a production server. Validating CR..."

                            // Embed the Python script
                            def cr_valid = sh(script: """
                                python3 -c '
import datetime

def cr_window_check(cr_start_time, cr_end_time):
    if int(cr_end_time) - int(cr_start_time) < 1800:
        return False
    current_time = int(datetime.datetime.now().timestamp())
    if int(cr_start_time) <= current_time <= int(cr_end_time):
        return True
    else:
        return False

# Validate the CR window
import sys
cr_start_time = sys.argv[1]
cr_end_time = sys.argv[2]
if cr_window_check(cr_start_time, cr_end_time):
    sys.exit(0)  # Success
else:
    sys.exit(1)  # Failure
' ${params.cr_start_time} ${params.cr_end_time}
                            """, returnStatus: true)
                            
                            if (cr_valid != 0) {
                                error("CR validation failed for server: ${server_id}. Aborting job.")
                            } else {
                                echo "CR validation passed for server: ${server_id}. Proceeding with patching."
                            }
                        } else {
                            echo "Server ${server_id} is a development server. Skipping CR validation..."
                        }

                        // Trigger the downstream patching job
                        build job: 'ServerPatchingJob', parameters: [
                            string(name: 'Patch_Level', value: params.Patch_Level),
                            string(name: 'Patch_Mode', value: params.Patch_Mode),
                            string(name: 'Change_Request', value: params.Change_Request),
                            string(name: 'Server_ID', value: server_id),
                            string(name: 'Patch_Username', value: env.PATCH_USERNAME), // Use the injected env variable
                            password(name: 'User_Password', value: env.PATCH_PASSWORD)  // Use the injected env variable
                        ]
                    }
                }
            }
        }
    }
}
