Get-ChildItem -Filter "openapi-v*.json" | ForEach-Object {
    $file = $_.FullName
    Write-Host "Processing $file..."

    $json = Get-Content $file -Raw | ConvertFrom-Json

    # Extract version from file name or servers.url field
    $versionFound = $false
    $version = $null

    # Check the file name
    if ($file -match "-v(\d+)\.json$") {
        $version = "/v$($Matches[1])"
        $versionFound = $true
#         Write-Host "→ Version found in filename: $version"
    }

    # If not found in filename, look in servers.url
    if (-not $versionFound -and $json.servers) {
        foreach ($server in $json.servers) {
            if ($server.url -match "/v(\d+)(?:/|$)") {
                $version = "/v$($Matches[1])"
                $versionFound = $true
#                 Write-Host "→ Version found in servers.url: $version"
                break
            }
        }
    }

    # Ensure servers.url includes the version
    if ($json.servers) {
        foreach ($server in $json.servers) {
            if (-not ($server.url -like "*$version")) {
                $oldUrl = $server.url
                $server.url = $server.url.TrimEnd("/") + $version
#                 Write-Host "→ Updated server URL from '$oldUrl' to '$($server.url)'"
            }
        }
    }

    # Simplest approach: directly remove version from paths using replace
    $pathsModified = $false
    $newPaths = @{}
    $versionToRemove = "/v\d+"  # Version pattern to remove (/v1, /v2, etc.)

#     Write-Host "Removing '$versionToRemove' version from paths"

    foreach ($pathKey in $json.paths.PSObject.Properties.Name) {
#         Write-Host "→ Processing path: $pathKey"

        # First remove the /api prefix if present
        $newPathKey = $pathKey -replace "^$apiPrefix", ""

        # Then remove the version pattern /vX if present
        $newPathKey = $newPathKey -replace "$versionToRemove", ""

        if ($newPathKey -ne $pathKey) {
            $pathsModified = $true
#             Write-Host "  ✓ Path modified: $pathKey -> $newPathKey"
        } else {
#             Write-Host "  ✓ Path unchanged: $pathKey"
        }

        # Copy all operations and details from original path
        $newPaths[$newPathKey] = $json.paths.$pathKey
    }

    # Update paths only if modified
    if ($pathsModified) {
        $json.paths = $newPaths
#         Write-Host "→ Paths updated with new keys"
    } else {
#         Write-Host "→ No path modifications needed"
    }

    # Find the appropriate output directory (static/openapi) in the project
    $outputDir = $null

    # Try to find the static/openapi directory in standard Spring structure
    $possiblePaths = @(
        "src/main/resources/static/openapi"
    )

    foreach ($path in $possiblePaths) {
        $fullPath = Join-Path (Split-Path -Parent $PSScriptRoot) $path
        if (Test-Path $fullPath) {
            $outputDir = $fullPath
            break
        }

        # Also check if the path exists relative to current directory
        $relPath = Join-Path $PSScriptRoot $path
        if (Test-Path $relPath) {
            $outputDir = $relPath
            break
        }
    }

    # If we couldn't find the static/openapi directory, create it
    if (-not $outputDir) {
        $outputDir = Join-Path $PSScriptRoot "src/main/resources/static/openapi"

        if (-not (Test-Path $outputDir)) {
            New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
            Write-Host "→ Created directory: $outputDir"
        }
    }

    # Generate output filename
    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($file)
    $outFile = Join-Path $outputDir "$($fileName).json"

    # Fix JSON formatting issue - Convert to string first and then save to file
    $jsonString = ($json | ConvertTo-Json -Depth 100)
    [System.IO.File]::WriteAllText($outFile, $jsonString, [System.Text.UTF8Encoding]::new($false))
#     Write-Host "→ File saved: $outFile"
}

Write-Host "OpenAPI files processed successfully."
Write-Host "Saved in src/main/resources/static/openapi"
