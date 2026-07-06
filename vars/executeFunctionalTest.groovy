def call(Map options) {

    node {

        withCredentials([
            usernamePassword(
                credentialsId: options.credentialId,
                usernameVariable: 'WM_USERNAME',
                passwordVariable: 'WM_PASSWORD'
            )
        ]) {

            powershell """
            \$tokenResponse = Invoke-RestMethod `
              -Method POST `
              -Uri "https://wm-sandbox-auth-1.watermelon.us/realms/watermelon/protocol/openid-connect/token" `
              -ContentType "application/x-www-form-urlencoded" `
              -Body @{
                client_id="web_app"
                username=\$env:WM_USERNAME
                password=\$env:WM_PASSWORD
                grant_type="password"
              }

            \$token = \$tokenResponse.access_token

            Write-Host "Authentication successful"

            \$body = @{
                packId = ${options.packId}
                runNow = \$true
                environmentId = ${options.environmentId}
                buildId = ${options.buildId}
                iteration = 1
                headlessExecution = \$false
                webAutId = ${options.webAutId}
            } | ConvertTo-Json

            \$response = Invoke-RestMethod `
              -Method PUT `
              -Uri "https://wm-sandbox-1.watermelon.us/services/wmuitestcontroller/api/test-packs/execute_v2" `
              -Headers @{
                 Authorization = "Bearer \$token"
              } `
              -ContentType "application/json" `
              -Body \$body

            \$runId = \$response.id

            Write-Host "RUN ID: \$runId"

            do {

                Start-Sleep -Seconds 10

                \$result = Invoke-RestMethod `
                  -Method GET `
                  -Uri "https://wm-sandbox-1.watermelon.us/services/wmuitestcontroller/api/test-runs/\$runId" `
                  -Headers @{
                     Authorization = "Bearer \$token"
                  }

                Write-Host "STATE: \$((\$result).state)"
                Write-Host "STATUS: \$((\$result).status)"

            } while (\$result.state -ne "COMPLETED")

            if (\$result.status -eq "FAIL") {
                throw "Watermelon execution failed"
            }

            Write-Host "Watermelon execution completed successfully"

            """
        }
    }
}
