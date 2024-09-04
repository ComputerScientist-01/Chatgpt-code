If you're running into limitations with the Jenkins Pipeline and want to convert it to a Freestyle project, here’s how you can approach the conversion:

### 1. **Creating a Freestyle Project in Jenkins**
   - Go to Jenkins' main dashboard.
   - Click on "New Item."
   - Enter a name for your Freestyle project.
   - Select "Freestyle project" and click "OK."

### 2. **Configure the Freestyle Project**
   After creating the project, you'll need to configure it to perform the same tasks your Pipeline was handling.

#### **2.1 Parameters**
   - In the Freestyle project, you can add parameters by checking the "This project is parameterized" box in the **General** section.
   - Add the same parameters you had in the Pipeline:
     - `Patch_Level` (String Parameter)
     - `Patch_Mode` (String Parameter)
     - `Change_Request` (String Parameter)
     - `Server_List` (Multi-line String Parameter)
     - `Patch_Username` (String Parameter)
     - `User_Password` (Password Parameter)

#### **2.2 Build Steps**
   In Freestyle projects, you handle most of the logic through build steps.

   - **Inject Environment Variables**:
     - Use the "Execute Shell" build step to set environment variables:
     ```bash
     export PATCH_USERNAME="${Patch_Username}"
     export PATCH_PASSWORD="${User_Password}"
     ```

   - **Validate Server and Trigger Patching Jobs**:
     - You can use "Execute Shell" build steps to perform the server validation and trigger other jobs. Here’s an example of how to script this in a Freestyle project:

     ```bash
     # List of production servers
     prodServers=("prod-server1" "prod-server2" "prod-server3")

     # Split Server_List into an array
     IFS=$'\n' read -r -d '' -a servers <<< "$Server_List"

     for server_id in "${servers[@]}"; do
         echo "Processing server: ${server_id}"

         if [[ " ${prodServers[@]} " =~ " ${server_id} " ]]; then
             echo "Server ${server_id} is a production server. Validating CR..."

             # Call your Python script for CR validation
             python3 /path/to/validate_cr.py "$Change_Request" "$cr_start_time" "$cr_end_time"
             cr_valid=$?

             if [ $cr_valid -ne 0 ]; then
                 echo "CR validation failed for server: ${server_id}. Aborting job."
                 exit 1
             else
                 echo "CR validation passed for server: ${server_id}. Proceeding with patching."
             fi
         else
             echo "Server ${server_id} is a development server. Skipping CR validation..."
         fi

         # Trigger the downstream patching job (if you need to trigger another job)
         curl -X POST \
              --user $JENKINS_USER:$JENKINS_API_TOKEN \
              "$JENKINS_URL/job/ServerPatchingJob/buildWithParameters?Patch_Level=$Patch_Level&Patch_Mode=$Patch_Mode&Change_Request=$Change_Request&Server_ID=$server_id&Patch_Username=$PATCH_USERNAME&User_Password=$PATCH_PASSWORD"
     done
     ```

   - **Triggering Downstream Jobs**:
     - To trigger downstream jobs, you can use the "Trigger/call builds on other projects" build step or use a shell script like the one above.

### 3. **Testing the Freestyle Project**
   - Save the project configuration and run a build.
   - Monitor the console output to ensure that the CR validation and server processing work as expected.

### 4. **Considerations**
   - Freestyle projects are less flexible than Pipelines for complex workflows, but they can be simpler to set up and use for straightforward tasks.
   - Ensure your shell scripts are well-tested and handle errors appropriately since Freestyle projects do not have the same built-in error handling features as Pipelines.

By setting up the Freestyle project this way, you can replicate much of the functionality you had in your Pipeline while working within the constraints of the Freestyle project structure.


To use the "Trigger/call builds on other projects" feature in a Jenkins Freestyle project to trigger downstream jobs, follow these steps:

### 1. **Navigate to the Freestyle Project**
   - Go to your Freestyle project's configuration page in Jenkins.

### 2. **Add the "Trigger/call builds on other projects" Build Step**
   - Scroll down to the **Build** section.
   - Click on **Add build step** and select **Trigger/call builds on other projects** from the dropdown.

### 3. **Configure the Trigger**
   - **Projects to build**: Enter the name(s) of the downstream job(s) you want to trigger. If you have multiple jobs, separate their names with commas.
   - **Block until the triggered projects finish building**: If you want the current job to wait until the triggered jobs complete before continuing, check this option.
   - **Trigger only if build is stable**: You can choose to trigger the downstream job only if the current build is successful.

### 4. **Pass Parameters to the Downstream Job**
   - Click on **Add Parameters** under the "Trigger/call builds on other projects" section.
   - You can select various types of parameters to pass to the downstream job:
     - **Predefined parameters**: Allows you to pass specific key-value pairs. Example: `Patch_Level=${Patch_Level}`
     - **Current build parameters**: This will pass all the parameters from the current job to the downstream job.
     - **Node parameters**: Passes information about the node where the current job is running.
     - **Properties file**: You can pass parameters from a properties file located in the workspace.

### 5. **Example Configuration**

   Here’s how you might configure it if you want to trigger a job called `ServerPatchingJob` with specific parameters:

   - **Projects to build**: `ServerPatchingJob`
   - **Block until the triggered projects finish building**: Checked (if you want the current job to wait)
   - **Add Parameters**: 
     - **Predefined parameters**:
       ```text
       Patch_Level=${Patch_Level}
       Patch_Mode=${Patch_Mode}
       Change_Request=${Change_Request}
       Server_ID=${server_id}
       Patch_Username=${PATCH_USERNAME}
       User_Password=${PATCH_PASSWORD}
       ```

### 6. **Save and Test**
   - Save the project configuration.
   - Trigger a build of your Freestyle project.
   - Check if the downstream job (`ServerPatchingJob`) gets triggered with the correct parameters.

### 7. **Monitor the Execution**
   - After triggering the build, monitor the console output of both the Freestyle project and the downstream job to ensure that the parameters are passed correctly and that the downstream job runs as expected.

This setup will allow you to trigger other Jenkins jobs as part of your Freestyle project, passing along necessary parameters to control the downstream build's behavior.