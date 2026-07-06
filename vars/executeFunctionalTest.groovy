def call(Map options) {

    node {

        withCredentials([
            usernamePassword(
                credentialsId: options.credentialId,
                usernameVariable: 'WM_USERNAME',
                passwordVariable: 'WM_PASSWORD'
            )
        ]) {

            powershell '''
            $tokenResponse = Invoke-RestMethod `
              -Method POST `
              -Uri "https://wm-sandbox-auth-1.watermelon.us/realms/watermelon/protocol/openid-connect/token" `
              -ContentType "application/x-www-form-urlencoded" `
              -Body @{
                client_id="web_app"
                username=$env:WM_USERNAME
                password=$env:WM_PASSWORD
                grant_type="password"
              }

            Write-Host "TOKEN RECEIVED"
            Write-Host $tokenResponse.access_token.Substring(0,20)
            '''
        }
    }
}
